package org.openmetadata.nesstar.task;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmetadata.nesstar.HarvesterOptions;

public class CatalogRetainer {

	private HarvesterOptions options;

	public CatalogRetainer(HarvesterOptions options){
		this.options = options;
	}

	public void retainCatalogs(){

		//if we have retention and it doesnt = 0 we will look at what to do.
		if(options.getRetention() != null && !options.getRetention().equals("0")){

			//First we will find all the Catalog files		
			File outputFolder = new File(options.getOutputFolder());

			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File file, String fileName) {
					return fileName.startsWith("Catalog");
				}
			};

			File[] files = outputFolder.listFiles(filter);

			//We will then sort the files into different languages.
			Map<String, ArrayList<File>> catalogs = new LinkedHashMap<String, ArrayList<File>>(); 

			for(File file : files){
				String fileName = file.getName();

				String lang = fileName.replaceFirst("Catalog\\.", "");
				lang = lang.substring(0, lang.indexOf('.'));

				ArrayList<File> catFiles = catalogs.get(lang);
				if(catFiles == null){
					catFiles = new ArrayList<File>();
				}
				catFiles.add(file);
				catalogs.put(lang, catFiles);
			}

			//Now that we have sorted the catalogs into their languages we can go through each list and apply the retention

			String retention = options.getRetention();

			String retentionRegex = "[0-9]+";
			Pattern p = Pattern.compile(retentionRegex);
			Matcher m = p.matcher(retention);

			if(!m.matches()){
				Matcher m2 = p.matcher(retention);
				if(m2.find()){
					//split the retentionTime and find units
					Integer num = Integer.parseInt(m2.group(0));
					String unit = retention.replaceFirst(num.toString(), "");

					//If retention is a time limit, we will just evaluate every file
					for(ArrayList<File> cats : catalogs.values()){
						for(File file : cats){

							long purgeTime = 0;

							if(unit.equalsIgnoreCase("h")){
								Calendar cal = Calendar.getInstance();  
								cal.add(Calendar.HOUR_OF_DAY, num * -1);  
								purgeTime = cal.getTimeInMillis();   
							}
							else if(unit.equalsIgnoreCase("w")){
								Calendar cal = Calendar.getInstance();  
								cal.add(Calendar.WEEK_OF_YEAR, num * -1);  
								purgeTime = cal.getTimeInMillis();   
							}
							else if(unit.equalsIgnoreCase("d")){
								Calendar cal = Calendar.getInstance();  
								cal.add(Calendar.DAY_OF_YEAR, num * -1);  
								purgeTime = cal.getTimeInMillis();   
							}
							else if(unit.equalsIgnoreCase("m")){
								Calendar cal = Calendar.getInstance();  
								cal.add(Calendar.MONTH, num * -1);  
								purgeTime = cal.getTimeInMillis();   
							}		

							if(purgeTime != 0){
								if(file.lastModified() < purgeTime){
									System.out.println("Deleting catalog: "+file.getName() +" because it does not meet the time restrictions.");
									file.delete();
								}
							}
						}
					}
				}
			}
			else{
				//if retention is a count, we must sort the lists by time and than remove the files after the count. 
				for(ArrayList<File> cats : catalogs.values()){
					int retentionSize = Integer.parseInt(retention);

					if(cats.size() > retentionSize){
						File[] sortFiles = new File[cats.size()];
						int fileIndex = 0;
						for(File catFile : cats){
							sortFiles[fileIndex] = catFile;
							fileIndex++;
						}

						Arrays.sort(sortFiles, new Comparator<File>(){
							public int compare(File f1, File f2)
							{
								return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
							} 
						}
								);
						
						for(int i = 0; i < (sortFiles.length-retentionSize); i++){
							File deletable = sortFiles[i];
							System.out.println("Deleting catalog: "+deletable.getName() +".");
							deletable.delete();
						}
					}
				}

			}
		}
	}
}
