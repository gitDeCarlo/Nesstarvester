package org.openmetadata.nesstar;

import java.io.Serializable;
import java.util.ArrayList;

public class HarvesterOptions implements Serializable{

	private static final long serialVersionUID = 1L;
	private String serverUrl = "";
	private Integer portNumber;
	private String outputFolder;
	private String syncFolder;
	private String rawFolder = "";
	private String codebookFolder;
	private String db;
	private String dbPath;
	private String zipFolder;
	private String extensionPrefix ="";
	private String serverID = "";
	private ArrayList<String> catalogList;
	private ArrayList<String> languageList;
  	private Long throttle;
	private String userName;
	private String password;
	
	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getExtensionPrefix() {
		return extensionPrefix;
	}

	public void setExtensionPrefix(String extensionPrefix) {
		this.extensionPrefix = extensionPrefix;
	}

	public Integer getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(Integer portNumber) {
		this.portNumber = portNumber;
	}
	
	public void setPortNumber(String portNumber) {
		this.portNumber = Integer.parseInt(portNumber);
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		if(rawFolder.isEmpty()){
			this.rawFolder = outputFolder;
		}
		this.outputFolder = outputFolder;
	}

	public String getCodebookFolder() {
		return codebookFolder;
	}

	public void setCodebookFolder(String codebookFolder) {
		this.codebookFolder = codebookFolder;
	}

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public String getDbPath() {
		return dbPath;
	}

	public void setDbPath(String dbPath) {
		this.dbPath = dbPath;
	}

	public String getZipFolder() {
		return zipFolder;
	}

	public void setZipFolder(String nadaFolder) {
		this.zipFolder = nadaFolder;
	}

	public ArrayList<String> getCatalogList() {
		return catalogList;
	}

	public void setCatalogList(ArrayList<String> catalogList) {
		this.catalogList = catalogList;
	}
	
	public ArrayList<String> getLanguageList() {
		return languageList;
	}
	
	public void setLanguageList(ArrayList<String> languageList) {
		this.languageList = languageList;
	}

	public String getRawFolder() {
		return rawFolder;
	}

	public void setRawFolder(String rawFolder) {
		this.rawFolder = rawFolder;
	}
	
	public String getServerID() {
		return serverID;
	}
	
	public void setServerID(String serverID) {
		this.serverID = serverID;
	}

	public Long getThrottle() {
		return throttle;
	}
	
	public void setThrottle(Long throttle) {
		this.throttle = throttle;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	public String getSyncFolder() {
		return syncFolder;
	}
	
	public void setSyncFolder(String syncFolder) {
		this.syncFolder = syncFolder;
	}
}
