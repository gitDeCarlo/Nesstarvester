package org.openmetadata.nesstar;

import java.net.URI;
import java.util.Locale;

import com.nesstar.api.NesstarDB;
import com.nesstar.api.NesstarDBFactory;
import com.nesstar.api.Server;


public class NesstarServer {
	Server server;

	public NesstarServer(HarvesterOptions options) throws Exception {
		NesstarDB nesstarDB = NesstarDBFactory.getInstance();
		URI uri = new URI(options.getServerUrl()+":"+options.getPortNumber());
		this.server = nesstarDB.getServer(uri);
		if(options.getPassword()!= null && options.getUserName() != null){
			this.server.login(options.getUserName(),options.getPassword());
		}
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

}