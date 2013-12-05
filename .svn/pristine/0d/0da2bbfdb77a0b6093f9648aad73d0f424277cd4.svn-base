package org.openmetadata.nesstar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ddialliance.ddi_1_2_2.xml.xmlbeans.CodeBookDocument;

import com.nesstar.api.Server;
import com.nesstar.api.Study;
/**
 * This is the class that harvests one or more Nesstar Servers using the Nesstar API. The API is wrapped in a few
 * convenient methods for traversing one or more servers and fetching DDI information from them.
 *
 * Usage:
 * Once the class is instantiated, use the methods addServer or addServers to add servers to the list. Afterwards,
 * call the harvest method to start fetching data. The harvest method will traverse the object tree and add all the
 * DDI XML it finds to a list which in turn is returned.
 * The returned list consists of DDI objects that hold the XML and a corresponding URL to a WebView where the data can
 * be seen.
 * @author Ricco F. Madsen, Norsk samfunnsvitenskapelig datatjeneste
 */
public class NesstarHarvester {
	/**
	 * List of servers to harvest.
	 */
	protected ServerConfiguration serverConfig;

	/**
	 * Pointer to the server currently being harvested.
	 */
	private Server activeServer;

	/**
	 * Initializes the harvester. Also does some legwork to get the Nesstar API going.
	 */
	public NesstarHarvester(ServerConfiguration serverConfig) {
		this.serverConfig = serverConfig;
	}

	/**
	 * Reads all of the servers added to the list. This method is the entry point when traversing the object tree. For
	 * each server in the list it reads the list of Catalogs and recursively traverses them to retrieve the DDIs.
	 * @return An array of Strings each containing a DDI XML.
	 */
	public void harvestAndSave(Collection<Study> allStudies, NesstarUtils utils, String codebookFolder, String lang) {
		try {
			activeServer  = serverConfig.getServers().get(0);
		
			for (Study study : allStudies) {
				DDI ddi = saveStudy(study);
				if (ddi != null){
					utils.saveToDisc(ddi, codebookFolder, lang);
				}
			}

		} catch (Exception e) {
			System.err.println("An unexpected error occurred:");
			e.printStackTrace();
		}

	}

	/**
	 * Harvests ddi that are requested using the updates ArrayList
	 * @param updates
	 * @return
	 */
	public void harvestAndSave(Collection<Study> allStudies, ArrayList<String> updates, NesstarUtils utils, String codebookFolder, String lang) {
		//the list that holds what we find
		ArrayList<DDI> result = new ArrayList<DDI>();
		CodeBookDocument codebookDoc;
		try {
			activeServer  = serverConfig.getServers().get(0);
			//List<Study> allStudies = activeServer.getBank(Study.class).getAll();
			
			for (Study study : allStudies) {
				if(updates.contains(study.getId())){
					DDI ddi = saveStudy(study);
					if (ddi != null)
						utils.saveToDisc(ddi, codebookFolder, lang);
				}else{
				}
			}	
		} catch (Exception e) {
			System.err.println("An unexpected error occurred:");
			e.printStackTrace();
		}
	}

	/**
	 * Runs through a found study and saves the XML and bookmark info into a DDI object.
	 * @param data The found Dataset.
	 * @return Returns a DDI object holding the found data or null if an Exception occurs.
	 */
	protected DDI saveStudy(Study study) {
		try {
			
			//Here's the XML:
			Writer writer = new StringWriter();
			char[] buffer = new char[study.getDDI().available()];
			Reader reader = new BufferedReader(
					new InputStreamReader(study.getDDI(), "UTF-8"));
			int n;

			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
			
			String ddiXML = writer.toString();
			
			//Here is the information around the ddi
			DDI ddi = new DDI();
			ddi.setInternalId(study.getId());
			ddi.setCreationDate(study.getTimeStamp());
			ddi.setXml(ddiXML);
			ddi.setServer(activeServer.toURI());
			ddi.setStudy(study.toURI());
			
			study.getDDI().close();
			writer.close();
			reader.close();
			
			return ddi;
		} catch (Exception e) {
			System.err.println("An error occurred saving data from Dataset:");
			e.printStackTrace();
		}
		return null;
	}

	public ServerConfiguration getServers() {
		return serverConfig;
	}
}