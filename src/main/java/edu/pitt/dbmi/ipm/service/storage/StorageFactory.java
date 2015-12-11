package edu.pitt.dbmi.ipm.service.storage;

import java.util.Properties;

import edu.pitt.dbmi.ipm.service.TomcatHelper;
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
 * Returns Postgres or Virtuoso storage based on a 'useVitOrPostgre' parameter setting in jQueryPostgres.conf file
 * 
 * @author opm1
 * @version 1
 * @since Dec 11, 2015
 *
 */
public class StorageFactory {
	
	private static Storage storage = null;
	
	public static Storage getStorage(){
		if(storage == null){
			storage = getStorage( TomcatHelper.getProperties("jQueryPostgres.conf").getProperty("useVitOrPostgre").toLowerCase());
		}
		return storage;
	}
	
	private static Storage getStorage(String storageName){
		Storage locStorage = null;
		switch(storageName){
		case "postgres":
			locStorage = PostgreStorage.getInstance();
			break;
		case "virtuoso":
			locStorage = RDFStorage.getInstance();
			break;
			
		default:
			System.out.println("No such storage: "+storageName);
			System.exit(0);
		
		}
		return locStorage;
		
	}

}
