package org.openmetadata.harvester;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmetadata.nesstar.HarvesterOptions;
import org.openmetadata.nesstar.ServerConfiguration;
import org.openmetadata.nesstar.task.CatalogRetainer;
import org.openmetadata.nesstar.task.FullUpdate;
import org.openmetadata.nesstar.task.IncrementalUpdate;

public class Harvester {

	private static Log log = LogFactory.getLog(Harvester.class);
	private static Properties applicationProperties;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		System.out.println("     __              _                            _            ");
		System.out.println("  /\\ \\ \\___  ___ ___| |_ __ _ _ ____   _____  ___| |_ ___ _ __ ");
		System.out.println(" /  \\/ / _ \\/ __/ __| __/ _` | '__\\ \\ / / _ \\/ __| __/ _ \\ '__|");
		System.out.println("/ /\\  /  __/\\__ \\__ \\ || (_| | |   \\ V /  __/\\__ \\ ||  __/ |   ");
		System.out.println("\\_\\ \\/ \\___||___/___/\\__\\__,_|_|    \\_/ \\___||___/\\__\\___|_|   ");
		System.out.println();
		System.out.println();
		log.info("Starting Nesstarvester");
		//PropertyConfigurator.configure("src/main/resources/log4j.properties");

		//Retrieves the file containing the description of what servers to harvest from. 
		String propertiesFile = args[args.length - 1];

		log.info("Application context loaded succesfully.");

		//Creates an array of all the HarvesterOptions that are in the Properties file
		HarvesterOptions options = getHarvesterOptionsFromPropertiesFile(propertiesFile);

		//Logging and messages for the start of a harvest
		String startMessage = "Starting DDI harvest on server: "+options.getServerID();		
		System.out.println(startMessage);
		System.out.println();
		log.info(startMessage);

		harvestServer(options);

		//Logging and messages for the end of a harvest
		String endMessage = "Harvest of "+options.getServerID()+" completed.";
		System.out.println(endMessage);
		System.out.println();
		log.info(endMessage);


		//Final message
		String completedMessage = "All harvest operations have been completed.\nThank you for using the OpenMetadata Nesstarvester.";
		System.out.println(completedMessage);
		System.out.println();
		log.info(completedMessage);

		applicationProperties.setProperty("outputFolder", options.getOutputFolder());

		String updatesFile = propertiesFile.substring(0, propertiesFile.lastIndexOf('.'))+".updates";

		//no saving updates for now
		/*FileOutputStream out = new FileOutputStream(updatesFile);
		applicationProperties.store(out, "---Harvester Options for Nesstarvester---");
		out.close();*/
	}

	private static HarvesterOptions getHarvesterOptionsFromPropertiesFile(String propertiesFile) throws Exception{
		HarvesterOptions options = new HarvesterOptions();

		Properties defaultProps = new Properties();
		FileInputStream in = new FileInputStream(propertiesFile);
		defaultProps.load(in);
		in.close();

		// create application properties with default
		applicationProperties = new Properties(defaultProps);

		//Check for the serverUrl, this MUST be found or an exception will be thrown
		if(applicationProperties.getProperty("serverUrl") != null){
			options.setServerUrl(applicationProperties.getProperty("serverUrl"));
		}
		else{
			throw new RuntimeException("No serverUrl was found in the properties file, this must be set \"serverUrl=some.url\"");
		}

		//Give the name to the server if none is found, construct one using the server Url
		if(applicationProperties.getProperty("serverID") != null){
			options.setServerID(applicationProperties.getProperty("serverID"));
		}
		else{
			options.setServerID("Server: "+applicationProperties.getProperty("serverUrl"));
		}

		//Set the port number, if the port is empty, try 80 as default
		if(applicationProperties.getProperty("portNumber") != null){
			options.setPortNumber(applicationProperties.getProperty("portNumber"));
		}
		else{
			options.setPortNumber(80);
		}

		//Set the output folder if none is found throw exception
		if(applicationProperties.getProperty("outputFolder") != null){
			options.setOutputFolder(applicationProperties.getProperty("outputFolder"));
		}
		else{
			throw new RuntimeException("No outputFolder was found in the properties file, this must be set \"/path/to/outputfolder/\"");
		}

		//Set the codebook folder if found
		if(applicationProperties.getProperty("codebookFolder") != null){
			options.setCodebookFolder(applicationProperties.getProperty("codebookFolder"));
		}

		//Set the raw folder if found
		if(applicationProperties.getProperty("rawFolder") != null){
			options.setRawFolder(applicationProperties.getProperty("rawFolder"));
		}

		//Set the zip folder if found 
		if(applicationProperties.getProperty("zipFolder") != null){
			options.setZipFolder(applicationProperties.getProperty("zipFolder"));
		}

		//Set the catalogRetention
		if(applicationProperties.getProperty("catalogRetention") != null){
			options.setRetention(applicationProperties.getProperty("catalogRetention"));
		}


		//Set the throttle, if none is found set try 500 as default
		if(applicationProperties.getProperty("throttle") != null){
			try{
				options.setThrottle(Long.parseLong(applicationProperties.getProperty("throttle")));
			}
			catch(Exception e){
				options.setThrottle(0L);
			}
		}
		else{
			options.setThrottle(0L);
		}

		//Set the extension prefix if found 
		if(applicationProperties.getProperty("extensionPrefix") != null){
			options.setExtensionPrefix(applicationProperties.getProperty("extensionPrefix"));
		}

		//Set the language list if found
		if(applicationProperties.getProperty("languageList") != null){
			ArrayList<String> languageList = new ArrayList<String>(Arrays.asList(applicationProperties.getProperty("languageList").split(",")));
			options.setLanguageList(languageList);
		}

		//Set the catalog list if found 
		if(applicationProperties.getProperty("catalogList") != null){
			ArrayList<String> catalogList = new ArrayList<String>(Arrays.asList(applicationProperties.getProperty("catalogList").split(",")));
			options.setCatalogList(catalogList);
		}

		return options;
	}

	private static void harvestServer(HarvesterOptions options) throws Exception{

		//Initialize Server Configuration
		log.info("Setting up server configuration for : "+options.getServerUrl()+":"+options.getPortNumber());
		System.out.println("Setting up server configuration for : "+options.getServerUrl()+":"+options.getPortNumber());

		try {
			//Adds the appropriate server URL and the port number to harvest the DDI from. 
			ServerConfiguration serverConfig = new ServerConfiguration();			
			serverConfig.addServer(options);

			log.info("Server configured!");
			System.out.println("Server configured!");
			System.out.println();

		} catch (Exception e) {
			log.error("Server unable to be configured");
			System.out.println("Server unable to be configured");
			System.out.println();
			e.printStackTrace();
		}

		//Decide to do a full harvest or incremental
		boolean performIncrementalUpdate = new File(options.getOutputFolder()).exists();

		if(!performIncrementalUpdate){
			performFullUpdate(options);
		}
		else{
			performIncrementalUpdate(options);
		}
		
		//Evaluates catalogs and deletes any as needed. 
		CatalogRetainer cr = new CatalogRetainer(options);
		cr.retainCatalogs();
	}	


	private static void performFullUpdate(HarvesterOptions options) throws Exception{
		log.info("Full Update Beginning");
		System.out.println("Full Update in Progress...");		

		//The tasklet acts as a wrapper by retrieving the information and passing it onto the FullUpdate class
		FullUpdate fullUpdate = new FullUpdate(options);
		fullUpdate.runUpdate(applicationProperties);

		log.info("Full update complete");
		System.out.println("Full Update Completed.");
		System.out.println();
	}

	private static void performIncrementalUpdate(HarvesterOptions options) throws Exception{
		log.info("Incremental Update Beginning");
		System.out.println("Incremental Update in Progress...");
		System.out.println();

		IncrementalUpdate incrementalUpdate = new IncrementalUpdate(options);
		incrementalUpdate.runUpdate(applicationProperties);

		log.info("Incremental update complete");
		System.out.println("Incremental Update Completed.");	
		System.out.println();
	}
}
