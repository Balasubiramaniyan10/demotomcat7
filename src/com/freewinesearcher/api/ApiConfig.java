package com.freewinesearcher.api;

import java.io.FileInputStream;
import java.util.Properties;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;

public class ApiConfig {
	private static Properties clients;
	static {fetch();}
	

	private static void fetch() {
		String file="";
		try {
			file = System.getProperty("file.separator") + "workspace"
					+ System.getProperty("file.separator");
			if (System.getProperty("file.separator").equals("\\"))
				file = "C:" + file;
			file += "apiclients.properties";
			FileInputStream input = new FileInputStream(file);
			setClients(new Properties());
			getClients().load(input);
		} catch (Exception e) {
			Dbutil.logger.error("Problem loading API properties file "+file, e);
		}
	}


	public static Properties getClients() {
		return clients;
	}


	private static void setClients(Properties clients) {
		ApiConfig.clients = clients;
	}

}
    
