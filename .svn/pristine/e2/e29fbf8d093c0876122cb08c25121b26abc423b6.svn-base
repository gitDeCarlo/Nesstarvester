package org.openmetadata.nesstar.task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.ddialliance.ddi_2_5.xml.xmlbeans.CitationType;
import org.ddialliance.ddi_2_5.xml.xmlbeans.CodeBookDocument;
import org.ddialliance.ddi_2_5.xml.xmlbeans.CodeBookType;
import org.ddialliance.ddi_2_5.xml.xmlbeans.FileDscrType;
import org.ddialliance.ddi_2_5.xml.xmlbeans.FileTxtType;
import org.ddialliance.ddi_2_5.xml.xmlbeans.IDNoType;
import org.ddialliance.ddi_2_5.xml.xmlbeans.StdyDscrType;
import org.ddialliance.ddi_2_5.xml.xmlbeans.TitlStmtType;


public class PatchCodebook25 {

	/**
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void addNameSpace(File file) throws IOException{

		try{
			CodeBookDocument codebookDoc = CodeBookDocument.Factory.parse(file);
		}
		catch(Exception e){
	
			File temp = File.createTempFile("temp", ".xml");
			FileReader fr = new FileReader(file); 
			BufferedReader br = new BufferedReader(fr); 
			Writer output = new BufferedWriter(new FileWriter(temp));
			String part="";
			while((part = br.readLine()) != null) { 

				if(part.contains("<!DOCTYPE")){
					part = part.replaceFirst("<!DOCTYPE[^>]*>", "");
				}
				if(part.contains("<codeBook")){
					if(!(part.contains("http://www.icpsr.umich.edu/DDI"))) {
						// add namespace declaration
						part = part.replaceFirst("<codeBook ", "<codeBook xmlns=\"http://www.icpsr.umich.edu/DDI\"  ");
					}
					if(!(part.contains("version=\"1.2.2\""))) {
						// add namespace declaration
						part = part.replaceFirst("<codeBook ", "<codeBook version=\"1.2.2\" ");
					}
				}
				output.write(part);
			} 
			fr.close();
			output.close();

			String fileDestination = file.getPath();
			file.delete();
			temp.renameTo(new File(fileDestination));
		}
	}

	/**
	 * 
	 * @param file
	 */
	public void patchCodebook25Doc(File file){
		try{
			CodeBookDocument codebookDoc = CodeBookDocument.Factory.parse(file);
			CodeBookType codebook = codebookDoc.getCodeBook();

			codebook = patchFileDescription(codebook);
			codebook = removeEmptyFileId(codebook);
			codebook = patchCodebookId(codebook, file);
			

			XmlOptions ops = new XmlOptions();
			ops.setSavePrettyPrint();
			ops.setSaveOuter();
			codebook.save(file, ops);

		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param codebook
	 * @return
	 */
	public CodeBookType patchFileDescription(CodeBookType codebook){

		List<FileDscrType> fileDescriptions = codebook.getFileDscrList();
		int fileNumber = 0;
		
		for(FileDscrType fileDescription : fileDescriptions){
			fileNumber++;
			if(fileDescription.getID()==null || fileDescription.getID().equals("")){
				String id = "";
				for(FileTxtType fileText : fileDescription.getFileTxtList()){
					id = fileText.getFileNameArray(0).getID();
					fileText.getFileNameArray(0).setID(null);
				}
				fileDescription.setID(id);
				if(fileDescription.getID().equals("")||fileDescription.getID()==null){
					fileDescription.setID("F"+fileNumber);
				}
			}
		}
		return codebook;
	}

	/**
	 * 
	 * @param codebook
	 * @return
	 */
	public CodeBookType removeEmptyFileId(CodeBookType codebook){

		List<FileDscrType> fileDescriptions = codebook.getFileDscrList();
		if(fileDescriptions != null){
			for(FileDscrType fileDescription : fileDescriptions){
				for(FileTxtType fileText : fileDescription.getFileTxtList()){
					if(fileText.getFileNameArray(0).getID() == null){
						try{
							fileText.getFileNameArray(0).unsetID();
						}
						catch(Exception e){
							
						}
					}
					else if(fileText.getFileNameArray(0).getID().equals("")){
						try{
							fileText.getFileNameArray(0).unsetID();
						}
						catch(Exception e){
							
						}
					}
				}
			}
		}
		return codebook;
	}

	/**
	 * 
	 * @param codebook
	 * @return
	 */
	public CodeBookType patchCodebookId(CodeBookType codebook, File file){

		if(codebook.getID() == null){
			List<StdyDscrType> studyDescrips = codebook.getStdyDscrList();
			for(StdyDscrType studyDescrip : studyDescrips){
				List<CitationType> citations = studyDescrip.getCitationList();
				for(CitationType citation : citations){
					TitlStmtType titleStmt = citation.getTitlStmt();
					List<IDNoType> idNos = titleStmt.getIDNoList();
					for(IDNoType idNo : idNos){
						String id = idNo.newCursor().getTextValue();
						String agency ="";
						if(idNo.getAgency() != null){
							agency = idNo.getAgency();
						}
						if(id != null){
							if(agency.equals("ICPSR")){
								codebook.setID("ICPSR"+id);
							}
							else codebook.setID(id);
						}
						else codebook.setID("_"+file.getName().replace(".xml", "").replace(" ", ""));
					}
				}
			}
		}

		return codebook;
	}

	/**
	 * Set the text value of an XmlObject using a XmlCursor
	 * 
	 * @param object
	 * @param text
	 */
	private void setTextValue(XmlObject object, String text) {
		XmlCursor cursor = object.newCursor();
		cursor.setTextValue(text);
		cursor.dispose();
	}

}
