package org.openmetadata.nesstar.task;

import java.io.File;
import java.util.ArrayList;

import org.openmetadata.nesstar.NesstarUtils;

import com.nesstar.api.Server;

public class Packager {

	private String directoryPath;
	private String rawFolder;
	private String nadaFolder;


	/**
	 * Constructor. 
	 * @param directoryPath
	 * @param serverUrl
	 * @param portNumber
	 */
	public Packager(String directoryPath, String nadaFolder, String rawFolder){
		this.directoryPath = directoryPath;
		this.nadaFolder = nadaFolder;
		this.rawFolder = rawFolder;
	}
	
	/**
	 * compresses the files that have been downloaded from the server. 
	 * @throws Exception
	 */
	public void packageFiles() throws Exception{
		
		NesstarUtils utils = new NesstarUtils();

		File directory = new File(directoryPath+rawFolder);
		
		utils.compressFiles(directory, directoryPath+nadaFolder);
	}
	
}
