package org.openmetadata.ddipublisher;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmetadata.harvester.Harvester;
import org.openmetadata.nesstar.HarvesterOptions;
import org.openmetadata.nesstar.task.Publisher;

public class DdiPublisher {

	private static Log log = LogFactory.getLog(DdiPublisher.class);

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		System.out.println("   ___       _     _ _     _               ");
		System.out.println("  / _ \\_   _| |__ | (_)___| |__   ___ _ __ ");
		System.out.println(" / /_)/ | | | '_ \\| | / __| '_ \\ / _ \\ '__|");
		System.out.println("/ ___/| |_| | |_) | | \\__ \\ | | |  __/ |   ");
		System.out.println("\\/     \\__,_|_.__/|_|_|___/_| |_|\\___|_|   ");
		System.out.println();
		System.out.println();
		log.info("Starting Publisher");
		//PropertyConfigurator.configure("src/main/resources/log4j.properties");

		//Retrieves the file containing the description of what servers to harvest from. 
		String propertiesFile = args[args.length - 1];

		//Creates an array of all the HarvesterOptions that are in the Properties file
		HarvesterOptions options = getHarvesterOptionsFromPropertiesFile(propertiesFile);

		publishDDI(options);

		//Final message
		System.out.println();
		String completedMessage = "All publish operations have been completed.\nThank you for using the OpenMetadata Publisher.";
		System.out.println(completedMessage);
		System.out.println();
		log.info(completedMessage);
	}

	private static HarvesterOptions getHarvesterOptionsFromPropertiesFile(String propertiesFile) throws Exception{
		HarvesterOptions options = new HarvesterOptions();

		Properties defaultProps = new Properties();
		FileInputStream in = new FileInputStream(propertiesFile);
		defaultProps.load(in);
		in.close();

		// create application properties with default
		Properties applicationProps = new Properties(defaultProps);

		//Check for the serverUrl, this MUST be found or an exception will be thrown
		if(applicationProps.getProperty("db") != null){
			options.setDb(applicationProps.getProperty("db"));
		}
		else{
			throw new RuntimeException("No database name was found in the properties file, this must be set \"db=somedatabase\"");
		}

		//Give the name to the server if none is found, construct one using the server Url
		if(applicationProps.getProperty("dbPath") != null){
			options.setDbPath(applicationProps.getProperty("dbPath"));
		}
		else{
			throw new RuntimeException("No database path was found in the properties file, this must be set \"dbPath=somepath\"");
		}

		//Set the output folder if none is found throw exception
		if(applicationProps.getProperty("outputFolder") != null){
			options.setOutputFolder(applicationProps.getProperty("outputFolder"));
		}
		else{
			throw new RuntimeException("No outputFolder was found in the properties file, this must be set \"/path/to/outputfolder/\"");
		}

		return options;
	}

	private static void publishDDI(HarvesterOptions options) throws Exception{
		
		String publicationInitiationMessage = "Beginning publication of files at: "+options.getOutputFolder();
		log.info(publicationInitiationMessage);
		System.out.println(publicationInitiationMessage);
		
		//Perform publishing
		Publisher publisher = new Publisher(options);
		publisher.publish();
		
		
		String publicationFinalizationMessage = "Publication complete.";
		log.info(publicationFinalizationMessage);
		System.out.println(publicationFinalizationMessage);
	}
}
