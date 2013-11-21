package org.openmetadata.nesstar.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Replace;
import org.basex.core.cmd.XQuery;
import org.openmetadata.harvester.CatalogEntry;
import org.openmetadata.nesstar.HarvesterOptions;
import org.openmetadata.nesstar.NesstarUtils;

public class Publisher {

	private HarvesterOptions options;

	Log log = LogFactory.getLog(Publisher.class);

	/**
	 * Constructor for publisher, If updateList is null a full update will be performed. 
	 */
	public Publisher(HarvesterOptions options){
		this.options = options;
	}

	/**
	 * Publishes files that have been updated to the BaseXDatabase.
	 * @throws BaseXException
	 */
	public void publish() throws Exception{
		if(options.getDb() != null){

			Context context = new Context();

			if(options.getDbPath()!= null){
				context.mprop.setObject("DBPATH", options.getDbPath());
			}

			NesstarUtils utils = new NesstarUtils(options);

			//Open the database
			try{
				new Open(options.getDb()).execute(context);
			}
			catch(BaseXException e){
				new CreateDB(options.getDb()).execute(context);
				new Open(options.getDb()).execute(context);
			}

			File directory = new File(options.getSyncFolder());

			if(directory.exists()){	

				FilenameFilter catFilter = new FilenameFilter() {
					public boolean accept(File folder, String fileName) {
						return fileName.toLowerCase().startsWith("catalog");
					}
				};

				//Get catalogs of all languages (there should only be one per language)
				File[] catalogs = directory.listFiles(catFilter);

				if(catalogs.length == 0){
					String noCatMessage = "No catalog found in this directory.\n";
					log.info(noCatMessage);
					System.out.println();
					System.out.println(noCatMessage);

				}

				for(File catalog : catalogs){ 

					ArrayList<File> files = utils.getFilesFromDirectory(directory);

					//This will hold two lists, an updates list (index 0) and an deletes list (index 1)
					ArrayList<ArrayList<String>> fileLists = compareCatalogs(catalog, context);

					ArrayList<String> updates = fileLists.get(0);
					ArrayList<String> deletes = fileLists.get(1);

					//Delete files from database
					if(!deletes.isEmpty()){
						for(String delete : deletes){
							delete+=".xml";
							log.info("Deleting file: "+delete);
							try{
								new Delete(delete).execute(context);
							}
							catch(Exception e){
								log.info("Delete failed.");
							}
						}
					}
					else{
						log.info("No files to delete.");
					}

					//Incremental Update
					if(!updates.isEmpty()){ 
						for(File file : files){
							for(String name : updates){
								if(file.getName().contains(name) && file.getName().contains(options.getExtensionPrefix()) && !(file.getName().contains(".zip")) && !(file.getName().contains("Catalog.xml"))){
									log.info("Publishing update: "+file.getName());
									new Replace(file.getName(),file.getPath()).execute(context);
									break;
								}
							}
						}
					}
					else if(updates.isEmpty()){
						log.info("No updates at this point.");
					}
				}
			}
			else{
				String noCatMessage = "No catalog found in this directory.\n";
				log.info(noCatMessage);
				System.out.println();
				System.out.println(noCatMessage);
			}
			//System.out.println(new InfoDB().execute(context));
			new Close().execute(context);	
		}

	}

	private ArrayList<ArrayList<String>> compareCatalogs(File catalog, Context context) throws Exception{

		//Arraylist that will be returned holding both an array for updates and one for deletes
		ArrayList<ArrayList<String>> updatesAndDeletes = new ArrayList<ArrayList<String>>();

		//updates list
		ArrayList<String> updates = new ArrayList<String>();

		//deletes list
		ArrayList<String> deletes = new ArrayList<String>();

		String format = "yyyy-MM-dd'T'HH:mm:ss'Z'";

		//	try{
		InputStream in = getClass().getResourceAsStream("/nesstarvester_catalog.xq");
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String xquery = "";
		String queryLine = "";

		//creates the xquery that in in src/main/resources/nesstarvester_catalog_changes.xq
		while((queryLine = br.readLine()) != null){
			xquery += queryLine;
		}
		br.close();

		String dbCat = (new XQuery(xquery).execute(context));
		if(!dbCat.isEmpty()){
			//Get catalog Entries to compare 
			ArrayList<CatalogEntry> newEntries = parseCatalogXml(catalog);
			ArrayList<CatalogEntry> previousEntries = parseCatalogXml(dbCat);

			for(CatalogEntry prevEntry : previousEntries){
				String prevId = prevEntry.getId();
				boolean foundId = false;

				for(CatalogEntry curEntry : newEntries){
					if(curEntry.getId().equalsIgnoreCase(prevId)){
						foundId = true;
						
						//turn the current timeStamp into a date 
						SimpleDateFormat sdf = new SimpleDateFormat(format);
						Date dOne = sdf.parse(curEntry.getTimeStamp());

						//turn the previous timeStamp into a date 
						SimpleDateFormat sdf2 = new SimpleDateFormat(format);

						Date dTwo = null;
						try{
							dTwo = sdf2.parse(prevEntry.getTimeStamp());
						}
						catch(Exception e){
							String smallerFormat = "yyyy-MM-dd'T'HH";
							SimpleDateFormat sdf3 = new SimpleDateFormat(smallerFormat);
							dTwo = sdf3.parse(prevEntry.getTimeStamp());
						}

						if(dOne.after(dTwo)){
							updates.add(curEntry.getId());
						}
					}

				}
				//If the previous entry was not found in the latest catalog it must have been deleted.
				if(!foundId){
					deletes.add(prevEntry.getId());
				}
			}

			for(CatalogEntry curEntry : newEntries){
				String currId = curEntry.getId();
				boolean foundId = false;

				for(CatalogEntry prevEntry : previousEntries){
					if(prevEntry.getId().equalsIgnoreCase(currId)){
						foundId = true;
					}
				}
				if(!foundId){
					updates.add(curEntry.getId());
				}
			}
		}
		//No Catalog found in the DB indicating the database is new
		else{
			log.info("Creating database: "+options.getDb());
			new Replace(catalog.getName(),catalog.getPath()).execute(context);

			log.info("First push into database.");

			ArrayList<CatalogEntry> newEntries = parseCatalogXml(catalog);

			for(CatalogEntry curEntry : newEntries){
				updates.add(curEntry.getId());
			}
		}

		//	}
		/*	catch(Exception e){
			ArrayList<CatalogEntry> newEntries = parseCatalogXml(catalog);

			for(CatalogEntry curEntry : newEntries){
				updates.add(curEntry.getId());
			}
		}*/

		updatesAndDeletes.add(updates);
		updatesAndDeletes.add(deletes);

		return updatesAndDeletes;
	}

	private ArrayList<CatalogEntry> parseCatalogXml(File catalogFile) throws Exception{

		ArrayList<CatalogEntry> entries = new ArrayList<CatalogEntry>();

		FileReader catalogReader = new FileReader(catalogFile);
		BufferedReader buffReader = new BufferedReader(catalogReader);

		String line = "";

		while((line = buffReader.readLine())!= null){
			line = line.trim();
			if(line.startsWith("<Study")){
				String id = line.substring(line.indexOf('"')+1, line.indexOf("\" t"));
				String timeStamp = line.substring(line.indexOf("amp=\"")+5, line.lastIndexOf('"'));

				CatalogEntry entry = new CatalogEntry(id, timeStamp);
				entries.add(entry);
			}
		}


		return entries;
	}

	private ArrayList<CatalogEntry> parseCatalogXml(String catalog) throws Exception{

		ArrayList<CatalogEntry> entries = new ArrayList<CatalogEntry>();

		StringReader catalogReader = new StringReader(catalog);
		BufferedReader buffReader = new BufferedReader(catalogReader);

		String line = "";

		while((line = buffReader.readLine())!= null){
			String[] entryAttrs = line.split(":");

			String id = entryAttrs[0];
			String timeStamp = entryAttrs[1];

			CatalogEntry entry = new CatalogEntry(id.trim(), timeStamp.trim());
			entries.add(entry);

		}


		return entries;
	}

	/////////////////////////////////////
	//		GETTERS/SETTERS
	/////////////////////////////////////

	public HarvesterOptions getOptions() {
		return options;
	}

	public void setOptions(HarvesterOptions options) {
		this.options = options;
	}
}
