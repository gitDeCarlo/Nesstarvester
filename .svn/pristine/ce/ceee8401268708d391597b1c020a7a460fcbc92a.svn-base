
package org.openmetadata.nesstar;

import java.util.ArrayList;

import com.nesstar.api.Server;

public class ServerConfiguration {

	private ArrayList<Server> servers;
	
	public ServerConfiguration(){
		this.servers = new ArrayList<Server>();
	}
	
	public ServerConfiguration(ArrayList<NesstarServer> nessServers){
		if(nessServers != null){
			for(NesstarServer serv: nessServers){
				this.servers.add(serv.getServer());
			}
		}
		else this.servers = new ArrayList<Server>();
	}
	
	/**
	 * Adds one server to the list.
	 * @param address IP address of the server to add. A complete URI is required (e.g. http://127.0.0.1 or
	 * http://somehost.com)
	 * @return True or false depending on whether server was added or not.
	 * @throws Exception 
	 */
	public void addServer(HarvesterOptions options) throws Exception {
		NesstarServer s = new NesstarServer(options);
		servers.add(s.getServer());
	}

	/**
	 * Getter
	 * @return
	 */
	public ArrayList<Server> getServers() {
		return servers;
	}
	
	/**
	 * Setter
	 * @param servers
	 */
	public void setServers(ArrayList<Server> servers) {
		this.servers = servers;
	}
}
