package org.openmetadata.nesstar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.nesstar.api.FolderTreeNode;
import com.nesstar.api.IllegalTreeOperationException;
import com.nesstar.api.NesstarTreeNode;
import com.nesstar.api.NesstarTreeObject;
import com.nesstar.api.NotAuthorizedException;
import com.nesstar.api.ResourceLinkTreeNode;
import com.nesstar.api.Server;
import com.nesstar.api.Study;

public class NesstarUtils {

	private ServerConfiguration serverConfig = new ServerConfiguration();
	private String serverUrl;
	private String directoryPath;
	private Integer portNumber;
	private String extensionPrefix;
	private HarvesterOptions options;

	Logger logger = Logger.getLogger(this.getClass().getName());

	public NesstarUtils(){

	}

	/**
	 * Creates the Utilities for a specific Nesstar Server
	 * @param directoryPath
	 * @param serverUrl
	 * @param portNumber
	 * @param extensionPrefix
	 */
	public NesstarUtils(HarvesterOptions options){
		this.options = options;
		this.directoryPath = options.getOutputFolder();
		this.serverUrl = options.getServerUrl();
		this.portNumber = options.getPortNumber();
		this.extensionPrefix = options.getExtensionPrefix();
	}

	/**
	 * Creates a catalog xml file and
	 * 	1. Saves it to a file for updates to look at in the future
	 * 	2. Returns a string of it to be put into the BaseX DB
	 * @param studyList
	 * @param indexName
	 * @throws Exception
	 */
	public String buildCatalogXml(Collection<Study> studyList, String lang) throws Exception{
		Collection<Study> removals = new ArrayList<Study>();
		Collection<String> removalIds = new ArrayList<String>();

		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
		Document doc = docBuilder.newDocument();

		Element catalog = doc.createElement("Catalog");

		Attr server = doc.createAttribute("server");
		Attr catTime = doc.createAttribute("timestamp");

		server.setValue(options.getServerID());

		catTime.setValue(this.toIso8106(new Timestamp(new Date().getTime())));

		catalog.setAttributeNode(server);
		catalog.setAttributeNode(catTime);

		doc.appendChild(catalog);

		for(Study study : studyList){
			//If duplicate studies are found only the first one will be added to the catalog. 
			if(!removalIds.contains(study.getId())){
				Element studyEl = doc.createElement("Study");

				Attr id = doc.createAttribute("id");
				Attr timeStamp = doc.createAttribute("timestamp");

				id.setValue(study.getId());
				timeStamp.setValue(this.toIso8106(new Timestamp(study.getTimeStamp().getTime())).toString());

				studyEl.setAttributeNode(id);
				studyEl.setAttributeNode(timeStamp);

				catalog.appendChild(studyEl);
				
				removalIds.add(study.getId());
			}
			//if duplicates are found they must be removed to be efficient
			else{
				logger.info("Removing duplicate study with ID: "+study.getId());
				removals.add(study);
			}
		}
		
		//Removes the studys that were found to be duplicates. 
		for(Study study : removals){
			studyList.remove(study);
		}

		TransformerFactory transfac = TransformerFactory.newInstance();
		//Saves Xml to file
		Transformer transFile = transfac.newTransformer();
		transFile.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transFile.setOutputProperty(OutputKeys.INDENT, "yes");

		File catalogXml = new File(directoryPath+"/Catalog."+lang+"."+getCurrentDate()+".xml");

		//create File from xml tree
		StreamResult path = new StreamResult(catalogXml);
		DOMSource source = new DOMSource(doc);

		transFile.transform(source, path);

		//Saves Xml to string for BaseX DB
		Transformer transStr = transfac.newTransformer();
		transStr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transStr.setOutputProperty(OutputKeys.INDENT, "yes");

		StringWriter stringWriter = new StringWriter();

		//create File from xml tree
		StreamResult pathStr = new StreamResult(stringWriter);
		DOMSource sourceStr = new DOMSource(doc);

		transStr.transform(sourceStr, pathStr);
		return stringWriter.toString();
	}

	/**
	 * Compresses a file. 
	 * @param file
	 * @param serverName
	 * @throws IOException
	 */
	public void compressFile(File file, String path) throws IOException{

		String zipPath = path+".zip";
		File outFolder = new File(zipPath);

		//deletes old files with the same path name. 
		deleteZipFile(zipPath);

		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outFolder)));
		BufferedInputStream in = null;
		byte[] data  = new byte[1000];
		String files[] = file.list();
		for (int i=0; i<files.length; i++)
		{
			in = new BufferedInputStream(new FileInputStream
					(file.getPath() + "/" + files[i]), 1000);  
			out.putNextEntry(new ZipEntry(files[i])); 
			int count;
			while((count = in.read(data,0,1000)) != -1)
			{
				out.write(data, 0, count);
			}
			out.closeEntry();
		}
		out.flush();
		out.close();

	}

	/**
	 * 
	 * @param files
	 * @throws Exception 
	 */
	public void compressFiles(File file, String path) throws Exception{
		try{
			compressFile(file, path);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}


	/**
	 * 
	 * @param date
	 * @return
	 */
	public String configureDate(String date){

		if(date.contains(" "))
			date = date.replaceAll(" ", ".").trim();

		if(date.contains(":"))
			date = date.replaceAll(":", "-").trim();

		return date;
	}

	/**
	 * 
	 * @param url
	 * @return
	 */
	public String configureDirectoryName(String url){

		url = url.replaceAll("http://", "");
		url = url.replaceAll(":", ".");
		for(int i = 0; i < url.length(); i++)
		{
			if(url.charAt(i) == '/'){
				url = url.substring(0, i);
				break;
			}
		}
		return url;
	}

	/**
	 * Creates an arrayList of Strings from a String derived from an ArrayList
	 * @param string
	 * @return
	 */
	public ArrayList<String> createListFromString(String string){
		ArrayList<String> list = new ArrayList<String>();

		string = string.replace("[", " ");
		string = string.replace("]", ",");
		string = string.replaceAll(", ", ",");

		while(!string.isEmpty()){
			int place = 0;
			while(!(string.charAt(place) == ',')){
				place++;
			}
			String item = string.substring(0, place);
			String trimmedItem = item.trim();
			list.add(trimmedItem);
			string = string.replace(item+',',"");
		}

		return list;
	}

	/**
	 * Finds every file in the base directory and deletes it. 
	 * @param deletes
	 * @param indexName
	 * @throws IOException 
	 */
	public void deleteFiles(ArrayList<String> deletes, String directoryPath) throws IOException{
		ArrayList<File> files = getFilesFromDirectory(new File(directoryPath));

		for(File file : files){
			for(String filename : deletes){
				if(!file.getPath().contains(".zip")){
					if(file.getName().contains(filename)) file.delete();
					break;
				}
				else{
					if(file.getName().contains(filename)) deleteZipFile(file.getPath());
					break;
				}
			}
		}
	}

	/**
	 * Deletes the files with the same name of the file that is being updated
	 * @param file
	 * @throws IOException 
	 */
	public void deleteZipFile(String path) throws IOException{

		if(new File(path).exists()){
			ZipFile zFile = new ZipFile(path);
			zFile.close();

			File file = new File(path);
			file.delete();
		}
	}

	/**
	 * Downloads a file from the supplied web address and returns it as a string. 
	 * @param address
	 * @return
	 * @throws IOException 
	 */
	@SuppressWarnings("resource")
	public String downloadXmlFile(String address) throws IOException{
		logger.debug("begginning xmlFile download");

		String downloadedFile ="";

		URL dataURL = new URL(address);

		URLConnection conn = dataURL.openConnection();

		String encoding = conn.getContentEncoding();
		if(encoding == null) encoding = "";

		InputStream is;

		//checks to see if the file is encoded, if it is than it is handled with a GZIPInputSream
		//if not it is handled with the stream from the dataURL
		if(encoding.equals("gzip")){
			is = encoding.equals("gzip")? new GZIPInputStream(conn.getInputStream()) : conn.getInputStream();
		}
		else{
			is = dataURL.openStream();
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(is));

		String inputLine;

		while ((inputLine = in.readLine()) != null){
			downloadedFile += (inputLine);
			//System.out.println(inputLine);
		}

		in.close();	

		return downloadedFile;
	}

	/**
	 * Used for nesstar API 0.1
	 * @param catalogXml
	 * @return
	 */
	public String patchCatalogXmlFile(String catalogXml){
		catalogXml = catalogXml.replaceAll("\"xmlns:", "\" xmlns:");

		return catalogXml;
	}

	/**
	 * InitialPatch for the ddi, makes sure the code book namespace for 
	 * @param ddiXml
	 * @return
	 */
	public String patchDdiDownload(String ddiXml){
		String ddiStart = ddiXml.substring(0,500);

		if(ddiStart.contains("<!DOCTYPE")){
			ddiXml = ddiXml.replaceFirst("<!DOCTYPE[^>]*>", "");
		}

		if(!ddiStart.contains("xmlns=\"http://www.icpsr.umich.edu/DDI\"")) {

			logger.debug("Inserting http://www.icpsr.umich.edu/DDI into ddi file");

			// add namespace declaration
			ddiXml = ddiXml.replaceFirst("<codeBook ", "<codeBook xmlns=\"http://www.icpsr.umich.edu/DDI\" ");
		}
		if(!ddiStart.contains("version=\"1.2.2\"")) {
			// add namespace declaration
			ddiXml = ddiXml.replaceFirst("<codeBook ", "<codeBook version=\"1.2.2\" ");
		}

		return ddiXml;
	}

	/**
	 * Patches the ddi to ddi25 and saves it to a codebook folder. 
	 * @param ddiArray
	 * @param indexName
	 * @param codebookPath
	 * @param fileName
	 * @throws IOException
	 */
	public void patchAndSaveDdi25(DDI ddi, String ddiStr, String codebookFolder, String ddiId) throws IOException{
		boolean needsPatch = false;

		//If the String of ddi does not containe the ddi codebook namespace than the ddiStr will replace version, namespace, and adjust other namespaces found. 
		if(!ddiStr.contains("\"ddi:codebook:2_5\"")) {

			ddiStr = ddiStr.replaceFirst("version=\"1.2.2\"", "version=\"2.5\"");
			ddiStr = ddiStr.replaceFirst("\"http://www.icpsr.umich.edu/DDI\"","\"ddi:codebook:2_5\"");
			ddiStr = ddiStr.replaceFirst(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"","");
			ddiStr = ddiStr.replaceFirst(" xsi:schemaLocation=\"http://www.icpsr.umich.edu/DDI http://www.icpsr.umich.edu/DDI/Version1-2-2.xsd\"", "");

			if(ddiStr.contains("<!DOCTYPE")){
				ddiStr = ddiStr.replaceFirst("<!DOCTYPE[^>]*>", "");
				ddiStr = ddiStr.replaceFirst("version=\"2.0\"", "version=\"2.5\"");
				ddiStr = ddiStr.replaceFirst("<codeBook", "<codeBook xmlns=\"ddi:codebook:2_5\" ID=\""+ddiId+"\"");
				ddiStr = ddiStr.replaceAll(" source=\"[a-zA-Z]+\"", "");
			}

			needsPatch = true;
		}

		//Saves DDI
		if(needsPatch){
			//Creates a timestamp to be used in the name for the ddi file. 
			String timeStamp = ddi.getCreationDate().toString();
			timeStamp = configureDate(timeStamp);

			FileOutputStream fstream = new FileOutputStream(directoryPath+codebookFolder+ddi.getInternalId()+extensionPrefix+".ddi_2_5.xml");
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fstream,"UTF-8"));
			logger.debug("Writing changes to file.");
			writer.write(ddiStr);
			writer.close();
		}
	}

	/**
	 * Iterates over the ddi from the server and writes a file using the directory path, along with the given index and file name				
	 * @param ddiArray
	 * @param indexName
	 * @throws InterruptedException 
	 */
	public void saveToDisc(DDI ddi, String codebookFolder, String lang) throws InterruptedException{
		logger.debug("saving to disc");

		if(options.getThrottle() != null){
			logger.info("Throttled to "+options.getThrottle()+" milliseconds.");
			Thread.sleep(options.getThrottle());
		}

		//Iterates through all the ddi in the catalog. 
		try{
			logger.info("Downloading DDI with ID: "+ddi.getInternalId());

			//Creates a timestamp to be used in the creation of the file name
			String timeStamp = ddi.getCreationDate().toString();
			timeStamp = configureDate(timeStamp);
			FileOutputStream fStream = new FileOutputStream(directoryPath+"/"+ddi.getInternalId()+extensionPrefix+"."+lang+".xml");
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fStream, "UTF-8"));

			String ddiStr = ddi.getXml();

			//Patches the ddiStr to conform to what is needed in codebook version 1.2.2
			ddiStr = patchDdiDownload(ddiStr);

			//writes ddiStr to a the file. 
			writer.write(ddiStr);
			writer.close();

			if(codebookFolder != null && !codebookFolder.isEmpty()){
				//Patches the ddiStr to conform to what is needed to parse the xml into codebook version 2.5 using the xmlbeans2_5
				patchAndSaveDdi25(ddi, ddiStr, codebookFolder, ddi.getInternalId());
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param content
	 * @param path
	 * @param name
	 * @param extension
	 * @throws IOException
	 */
	public void saveTextFileToDisc(String content, String path, String name, String extension) throws IOException{
		FileOutputStream fstream = new FileOutputStream(path+name+extension);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fstream,"UTF-8"));
		out.write(content);
		out.close();
	}

	/**
	 * 
	 * @param timestamp
	 * @return
	 * @throws ParseException 
	 */
	public String toIso8106(java.sql.Timestamp timestamp) {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		return(format.format(timestamp));
	}

	/**
	 * checks to see if the file is valid against the ____ schema
	 * @param catalogXmlFile
	 * @return
	 * @throws Exception
	 */
	public boolean validateXmlFile(File catalogXmlFile) throws Exception{
		// parse an XML document into a DOM tree
		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = parser.parse(catalogXmlFile);

		// create a SchemaFactory capable of understanding WXS schemas
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		// load a WXS schema, represented by a Schema instance
		Source schemaFile = new StreamSource(new File("mySchema.xsd"));
		Schema schema = factory.newSchema(schemaFile);

		// create a Validator instance, which can be used to validate an instance document
		Validator validator = schema.newValidator();

		// validate the DOM tree
		try {
			validator.validate(new DOMSource(document));
			return true;
		} catch (SAXException e) {
			return false;
		}

	}

	//////////////////////////////////////////
	//			GETTERS/SETTERS
	//////////////////////////////////////////

	public void setServerConfig(ServerConfiguration serverConfig) {
		this.serverConfig = serverConfig;
	}

	public String getDirectoryPath() {
		return directoryPath;
	}

	public void setDirectoryPath(String directoryPath) {
		this.directoryPath = directoryPath;
	}

	/**
	 * Returns an ArrayList of files to download
	 * @param updatesFile
	 * @return
	 * @throws IOException
	 */
	public ArrayList<String> getDeletes(String updatesFile) throws IOException{

		ArrayList<String> deletes = new ArrayList<String>();

		try{
			FileInputStream fstream = new FileInputStream(updatesFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String update = "";

			//Deteremines which files to delete or download.
			while((update = br.readLine()) != null){
				String fileName = update.substring(0, update.indexOf(':'));
				if(fileName.charAt(0)==' ') fileName = fileName.substring(1,fileName.length());

				String procedure = update.substring(update.indexOf(':')+1, update.length());

				if(procedure.equals("deleted")) deletes.add(fileName);
			}

			br.close();
		}
		catch(FileNotFoundException f){
			logger.info("No file found, indicating no deletes.");
		}

		return deletes;
	}
	
	/**
	 * Returns an ArrayList of files to download
	 * @param updatesFile
	 * @return
	 * @throws IOException
	 */
	public ArrayList<String> getDownloads(String updatesFile) throws IOException{

		ArrayList<String> downloads = new ArrayList<String>();

		try{
			FileInputStream fstream = new FileInputStream(updatesFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String update = "";

			//Deteremines which files to delete or download.
			while((update = br.readLine()) != null){
				String fileName = update.substring(0, update.indexOf(':'));
				if(fileName.charAt(0)==' ') fileName = fileName.substring(1,fileName.length());

				String procedure = update.substring(update.indexOf(':')+1, update.length());

				if(procedure.equals("new") || procedure.equals("changed")) downloads.add(fileName);
			}

			br.close();
		}
		catch(FileNotFoundException f){
			logger.info("No file found, indicating a full update.");
		}

		return downloads;
	}

	public Locale[] getServerLanguages() throws Exception{
		return serverConfig.getServers().get(0).getLanguages();
	}
	
	public String getServerName(){
		//Creates index name for specific server and its catalog. 
		Server server = serverConfig.getServers().get(0);
		String indexName = server.getId();

		return indexName;
	}

	/**
	 * Creates a Nesstar Harvester based on the serverConfig.txt made in the serverConfig step of the job. 
	 * @return
	 * @throws Exception
	 */
	public NesstarHarvester getNesstarHarvester() throws Exception{

		ServerConfiguration serverConfig = getServerConfig();

		setServerConfig(serverConfig);

		//creates a new nesstar harvester with the newly created server configuration
		NesstarHarvester nesstarvester = new NesstarHarvester(serverConfig);

		return nesstarvester;
	}

	public ArrayList<File> getFilesFromDirectory(File directory){

		logger.debug("Retrieving files from directory");
		ArrayList<File> files = new ArrayList<File>();
		for(File file : directory.listFiles()){
			if(file.isDirectory()){
				ArrayList<File> bunchOfFiles = getFilesFromDirectory(file);
				files.addAll(bunchOfFiles);
			}
			else
				files.add(file);
		}

		return files;
	}

	public ServerConfiguration getServerConfig() throws Exception{
		//creates a serverConfiguation based on the url given in the file
		serverConfig.addServer(this.options);

		return serverConfig;
	}

	/**
	 * 
	 * @return
	 * @throws NotAuthorizedException
	 * @throws IOException
	 * @throws IllegalTreeOperationException 
	 */
	public ArrayList<Study> getStudies(ArrayList<String> catalogs) throws NotAuthorizedException, IOException, IllegalTreeOperationException{

		ArrayList<Study> studies = new ArrayList<Study>();

		for(NesstarTreeNode treenode : this.serverConfig.getServers().get(0).getTreeRoot().getChildren()){

			if(catalogs != null){
				if(catalogs.contains(treenode.getLabel())){
					studies = getChildren(treenode, studies);
				}
				else{
					if(treenode instanceof FolderTreeNode){
						FolderTreeNode folderNode = (FolderTreeNode)treenode;

						for(NesstarTreeNode treeNode2 : folderNode.getChildren()){
							if(catalogs.contains(treeNode2.getLabel().trim())) {								
								studies = getChildren(treeNode2, studies);
							}
						}
					}
				}
			}
			else{
				studies = getChildren(treenode, studies);
			}
		}
		return studies;
	}

	/*	public NesstarList<Study> getStudies() throws NotAuthorizedException, IOException {
		return this.serverConfig.getServers().get(0).getBank(Study.class).getAll();
	}*/

	/**
	 * Finds all the studies that belong in a certain catalog. Adds theses studies to an array related to that catalog
	 * @param treenode
	 * @param studyList
	 * @return
	 * @throws NotAuthorizedException
	 * @throws IOException
	 * @throws IllegalTreeOperationException
	 **/
	public ArrayList<Study> getChildren(NesstarTreeNode treenode, ArrayList<Study> studies) throws NotAuthorizedException, IOException, IllegalTreeOperationException{

		if(treenode instanceof FolderTreeNode) {
			FolderTreeNode folderNode = (FolderTreeNode)treenode;

			for(NesstarTreeNode childnode : folderNode.getChildren()){
				getChildren(childnode, studies);
			}
		}
		else if (treenode instanceof ResourceLinkTreeNode) {
			NesstarTreeObject resource = ((ResourceLinkTreeNode) treenode).getResource();
			if (resource instanceof Study) {
				studies.add((Study)resource);
			}
		}

		return studies;
	}

	public String getCurrentDate() {
		String DATE_FORMAT_NOW = "yyyy-MM-dd-HH-mm-ss";
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}
}
