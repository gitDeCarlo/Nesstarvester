package org.openmetadata.nesstar.task;

import java.io.File;
import java.util.ArrayList;

import org.basex.core.Context;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Replace;
import org.ddialliance.ddi_2_5.xml.xmlbeans.CodeBookDocument;
import org.openmetadata.ddi_2_5.util.Ddi25Utils;
import org.openmetadata.nesstar.HarvesterOptions;
import org.openmetadata.nesstar.NesstarUtils;

public class UpgradeToDdi25 {

	private HarvesterOptions options;
	private String updatesFile;
	
	/**
	 * 
	 * @param options
	 */
	public UpgradeToDdi25(HarvesterOptions options, String updatesFile){
		this.options = options;
		this.updatesFile = updatesFile;
	}


	/**
	 * Upgrades ddi to version 2.5 from 1.2.2
	 * @throws Exception
	 */
	public void upgrade() throws Exception{
		NesstarUtils utils = new NesstarUtils(options);
		
		File directory = new File(options.getOutputFolder()+options.getCodebookFolder());
		Context context = new Context();

		new Open(options.getDb()).execute(context);

		ArrayList<File> files = utils.getFilesFromDirectory(directory);

		ArrayList<String> downloads = utils.getDownloads(updatesFile);

		if(!downloads.isEmpty()){ //Incremental Update		
			for(File file : files){
				for(String name : downloads){
					if(file.getName().contains(name)){
						CodeBookDocument codebookDoc = CodeBookDocument.Factory.parse(file);
						Ddi25Utils.writeXmlFile(codebookDoc, file.getPath());		

						new Replace(options.getExtensionPrefix()+"\\"+file.getName(), file.getPath()).execute(context);
						break;
					}
				}
			}
		}else{ // full update
			for(File file : files){

				CodeBookDocument codebookDoc = CodeBookDocument.Factory.parse(file);
				Ddi25Utils.writeXmlFile(codebookDoc, file.getPath());		

				new Replace(options.getExtensionPrefix()+"\\"+file.getName(), file.getPath()).execute(context);
			}
		}

		//System.out.println(new InfoDB().execute(context));
		new Close().execute(context);
	}


	////////////////////////////////
	//		GETTERS/SETTERS
	////////////////////////////////

	public HarvesterOptions getOptions() {
		return options;
	}
	
	public void setOptions(HarvesterOptions options) {
		this.options = options;
	}
	
	public String getUpdatesFile() {
		return updatesFile;
	}
	
	public void setUpdatesFile(String updatesFile) {
		this.updatesFile = updatesFile;
	}
}
