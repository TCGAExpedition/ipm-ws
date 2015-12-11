package edu.pitt.dbmi.ipm.service;

import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Copyright (C) 2015  University of Pittsburgh
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * 
 * Utility class for getting access to *.conf files and to temporary saved
 * requested metadata files on server
 * 
 * @author opm1
 * @version 1
 * @since Dec 8, 2015
 * 
 */
public class TomcatHelper

{
	/**
	 * 
	 * @return CATALINA_HOME directory
	 */
	public static String getHomeDirectory() {
		if (System.getProperty("catalina.home") == null)
			return (System.getenv("CATALINA_HOME"));
		else
			return (System.getProperty("catalina.home"));

	}

	/**
	 * Getting path to tempo dir on server
	 * 
	 * @return String
	 */
	public static String getTempDirectory() {
		if (getHomeDirectory() == null)
			return System.getProperty("user.dir" + File.separator);
		else
			return (getHomeDirectory() + File.separator + "temp" + File.separator);
	}

	/**
	 * getting path to conf directory on server
	 * 
	 * @return String
	 */
	public static String getConfDirectory() {
		if (getHomeDirectory() == null)
			return null;
		else
			return (getHomeDirectory() + File.separator + "conf");
	}

	/**
	 * Properties for DataSelection
	 * 
	 * @param fileName
	 * @return Properties
	 */
	public static Properties getProperties(String fileName) {
		Properties params = new Properties();
		try {
			params.load(new FileInputStream(getConfDirectory() + File.separator
					+ fileName));
		} catch (IOException e) {
			// for local
			try {
				params.load(new FileInputStream(System.getProperty("user.dir")
						+ File.separator + "resources" + File.separator
						+ fileName));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return params;
	}

}
