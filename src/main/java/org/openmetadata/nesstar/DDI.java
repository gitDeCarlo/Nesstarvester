package org.openmetadata.nesstar;


import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

/**
 * Simple representation of DDI XML and the corresponding Nesstar Webview bookmark.
 * @author Ricco F. Madsen, Norsk samfunnsvitenskapelig datatjeneste
 */
public class DDI {
	/**
	 * The DDI XML
	 */
	protected String xml;
	/**
	 * URL pointing to the server that holds the data.
	 */
	protected URI server;
	/**
	 * URL for the Study.
	 */
	protected URI study;
	/**
	 * The mode is used to tell WebView how to show the requested data.
	 */
	protected String mode;
	/**
	 * URL for the Cube.
	 */
	protected URL cube;

	/**
	 * 
	 */
	protected Date CreationDate;
	
	/**
	 * Id of the dataset ddi belongs to. 
	 */
	protected String internalId;

	/**
	 * Convenience attribute for building the bookmark
	 */
	protected String urlSep = "?";

	/**
	 * Empty constructor.
	 */
	public DDI() {

	}

	/**
	 * Creates a DDI containing the DDI XML.
	 * @param _xml DDI XML.
	 */
	public DDI(String _xml) {
		xml = _xml;
	}

	/**
	 * Builds an HTTP URL that points directly to the Nesstar Webview that
	 * represents this object.
	 * @return URL as String.
	 */
	public String getBookmark() {
		StringBuilder sb = new StringBuilder();
		try {
			//sb.append(server.getProtocol());
			sb.append("://");
			sb.append(server.getHost());
			sb.append(":");
			sb.append(server.getPort());
			sb.append("/webview/");
			if (study != null) {
				sb.append(urlSep());
				sb.append("study=");
				sb.append(URLEncoder.encode(study.toString(), "UTF-8"));
			}
			if (cube != null) {
				sb.append(urlSep());
				sb.append("cube=");
				sb.append(URLEncoder.encode(cube.toString(), "UTF-8"));
			}
			sb.append(urlSep());
			sb.append("mode=");
			sb.append(mode);
			sb.append(urlSep());
			sb.append("top=yes");
		} catch (UnsupportedEncodingException uee) {
			System.err.println(uee.getMessage());
		}
		return sb.toString();
	}

	/**
	 * Get the bookmark as a URL object.
	 * @return URL representation of the bookmark.
	 */
	public URL getBookmarkURL() {
		String urlString = getBookmark();
		URL url = null;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException mue) {
			System.err.println(mue.getMessage());
		}
		return url;
	}

	/**
	 * Convenience method for building URLs. Always returns the correct character for separating URLs.
	 * @return Either '?' or '&' depending on how far the URL building is.
	 */
	protected String urlSep() {
		if ("?".equals(urlSep)) {
			urlSep = "&";
			return "?";
		} else {
			return urlSep;
		}
	}

	/**
	 * Get the DDI XML
	 * @return String of XML.
	 */
	public String getXml() {
		return xml;
	}

	/**
	 * Set the DDI XML. The XML is not validated in any way.
	 * @param xml XML to set.
	 */
	public void setXml(String xml) {
		this.xml = xml;
	}

	/**
	 * Get the server holding this data.
	 * @return URL of the server.
	 */
	public URI getServer() {
		return server;
	}

	/**
	 * Set the server that holds the data.
	 * @param server URL of the server.
	 */
	public void setServer(URI server) {
		this.server = server;
	}

	/**
	 * Get the URL of the Study that this DDI belongs to.
	 * @return URL of the Study.
	 */
	public URI getStudy() {
		return study;
	}

	/**
	 * Set the URL of the Study holding the data.
	 * @param study URL of the Study.
	 */
	public void setStudy(URI study) {
		this.study = study;
	}

	/**
	 * Get the mode for the bookmark.
	 * @return String mode.
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * Set the mode of the bookmark. Tells the WebView how to show the data.
	 * @param mode The mode as a String. Can be either "cube" or "documentation".
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * Get the URL pointing to the Cube representing the data.
	 * @return URL of the Cube.
	 */
	public URL getCube() {
		return cube;
	}

	/**
	 * Set the URL of the Cube representing the data.
	 * @param cube URL pointing to the Cube.
	 */
	public void setCube(URL cube) {
		this.cube = cube;
	}

	/**
	 * Get a String representation of this object. Since the XML is
	 * huge this method will return the URL as a String.
	 * @return String representation of this object (URL).
	 */
	public String toString() {
		return getBookmark();
	}

	public String getInternalId() {
		return internalId;
	}
	
	public void setInternalId(String internalId) {
		this.internalId = internalId;
	}
	
	public Date getCreationDate() {
		return CreationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		CreationDate = creationDate;
	}
}
