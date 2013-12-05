package org.openmetadata.nesstar.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmetadata.harvester.CatalogEntry;
import org.openmetadata.nesstar.HarvesterOptions;
import org.openmetadata.nesstar.NesstarHarvester;
import org.openmetadata.nesstar.NesstarUtils;

import com.nesstar.api.NesstarDB;
import com.nesstar.api.NesstarDBFactory;
import com.nesstar.api.NotAuthorizedException;
import com.nesstar.api.Study;

public class IncrementalUpdate {


	private HarvesterOptions options;
	private NesstarUtils utils;
	private NesstarHarvester nesstarvester;
	private String updatesString;
	private File fileToDelete;

	Log log = LogFactory.getLog(this.getClass());

	public IncrementalUpdate(HarvesterOptions options){
		this.options = options;
	}

	/**
	 * Performs an incremental update on the database by getting the catalog from the nesstar server and comparing
	 * it to the one that we already hold in the baseX database
	 * @return
	 * @throws Exception
	 */
	public String runUpdate(Properties applicationProperties) throws Exception{
		String catalogXml="";

		this.utils =  new NesstarUtils(options);
		//Creates the Nesstar Harvester
		this.nesstarvester = utils.getNesstarHarvester();
		Collection<Study> studies = null;

		ArrayList<Locale> locales = new ArrayList<Locale>();

		if(options.getLanguageList() != null){
			for(String lang : options.getLanguageList()){
				locales.add(new Locale(lang));
			}
		}
		else{
			locales = new ArrayList<Locale>(Arrays.asList(utils.getServerLanguages()));
		}

		//Start loop for languages
		for(Locale locale : locales){
			log.info("Running harvest on files of the language:"+locale.getLanguage());
			NesstarDB nesstarDb = NesstarDBFactory.getInstance();
			nesstarDb.setPreferredLanguages(locale);
			String lang = locale.getLanguage();

			if(options.getCatalogList() != null && options.getCatalogList().size() > 0){
				log.info("Retrieving studies from the catalogs");
				//Collects the list of studys that will be downloaded
				studies = utils.getStudies(options.getCatalogList());
			}
			else{
				try{
					log.info("Retrieving flat list of studies.");
					studies = utils.getServerConfig().getServers().get(0).getBank(Study.class).getAll();
				}
				catch(Exception e){
					log.info("ERROR: "+e.getMessage());
					log.info("No Files were found");
					System.exit(0);
				}
			}

			catalogXml = utils.buildCatalogXml(studies,lang);

			//creates a list of updates based on the DDI's InternalID?
			log.info("Identifying needed updates");
			compareCatalogs(locale.getLanguage());

			log.info("Performing update");
			if(!updatesString.isEmpty()){
				performUpdate(studies, options.getRawFolder(), lang);
			}
			
			//fileToDelete.delete();
		}

		applicationProperties.setProperty("updates",updatesString);
		
		return updatesString;
	}

	private void compareCatalogs(String lang) throws Exception{
		updatesString = "";
		File[] catalogs = getLatestCatalogs(utils.getDirectoryPath(), lang);

		String format = "yyyy-MM-dd'T'HH:mm:ss'Z'";
		File newCat = catalogs[0];
		File prevCat = catalogs[1];

		//Get catalog Entries to compare
		ArrayList<CatalogEntry> newEntries = parseCatalogXml(newCat);
		ArrayList<CatalogEntry> previousEntries = parseCatalogXml(prevCat);
		
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
					Date dTwo = sdf2.parse(prevEntry.getTimeStamp());
					log.info("New time: "+dOne+" | Old time: "+dTwo);
					
					if(dOne.after(dTwo)){
						updatesString+=(curEntry.getId()+":changed,");
					}
				}

			}

			if(!foundId){
				updatesString+=(prevEntry.getId()+":deleted,");
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
				updatesString+=(curEntry.getId()+":new,");
			}
		}

		//We always add a comma at the end, so get rid of the trailing comma
		if(!updatesString.isEmpty()){
			updatesString = updatesString.substring(0, updatesString.length()-1);
		}

		fileToDelete = prevCat;
	}

	/**
	 * Uses an array list of InternalId's and an update file to update the database.
	 * @param updates
	 * @throws IOException 
	 * @throws NotAuthorizedException 
	 */
	private void performUpdate(Collection<Study> studies, String serverName, String lang) throws IOException, NotAuthorizedException{
		log.debug("performing update");
		ArrayList<Study> downloads = new ArrayList<Study>();
		ArrayList<String> deletes = new ArrayList<String>();

		String[] updates = updatesString.split(",");

		for(String update : updates){

			String fileName = update.substring(0, update.indexOf(':'));
			if(!fileName.isEmpty() && fileName.charAt(0)==' '){
				fileName = fileName.substring(1,fileName.length());
			}

			String procedure = update.substring(update.indexOf(':')+1, update.length());
			if(procedure.equals("new") || procedure.equals("changed")){
				for(Study study : studies){
					if(study.getId().equals(fileName)){
						downloads.add(study);
					}
				}
			}
			else if(procedure.equals("deleted")){
				deletes.add(fileName);
			}
		}

		this.nesstarvester.harvestAndSave(downloads, utils, options.getCodebookFolder(), lang);
		utils.deleteFiles(deletes, options.getOutputFolder());
	}

	private static File[] getLatestCatalogs(String dir, final String lang) {
		File[] returnFiles = new File[2];

		File latestFile = null;
		File secondLatestFile = null;

		FilenameFilter catFilter = new FilenameFilter() {
			public boolean accept(File folder, String fileName) {
				return fileName.toLowerCase().startsWith("catalog."+lang);
			}
		};
		
		File fl = new File(dir);
		File[] files = fl.listFiles(catFilter);

		int i = 0;
		for (File file : files) {

			if(i > 1){
				if(file.lastModified() > secondLatestFile.lastModified()) {
					secondLatestFile = file;
				}
				if(file.lastModified() > latestFile.lastModified()) {
					secondLatestFile = latestFile;
					latestFile = file;
				}
			}
			else if(i == 0){
				latestFile = file;
			}
			else{
				if(file.lastModified() > latestFile.lastModified()){
					secondLatestFile = latestFile;
					latestFile = file;
				}
				else{
					secondLatestFile = file;
				}
			}

			i++;
		}

		returnFiles[0] = latestFile;
		returnFiles[1] = secondLatestFile;

		return returnFiles;
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


	//////////////////////////////////////
	//		GETTERS/SETTERS
	//////////////////////////////////////

	public HarvesterOptions getOptions() {
		return options;
	}

	public void setOptions(HarvesterOptions options) {
		this.options = options;
	}
}
