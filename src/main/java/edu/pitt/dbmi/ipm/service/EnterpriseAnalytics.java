package edu.pitt.dbmi.ipm.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import edu.pitt.dbmi.ipm.service.storage.StorageFactory;

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
 * Returns analytical data requested be web server.
 * 
 * @author opm1
 * @version 1
 * @since Dec 11, 2015
 *
 */

public class EnterpriseAnalytics {
	private static Log log = LogFactory.getLog(EnterpriseAnalytics.class);

	private static String count_patients_by_tss_Q = null;
	private static boolean hasParams = false;

	/**
	 * Initializes parameters from *.conf file based on selected storage
	 * parameter 'useVitOrPostgre' in jQueryPostgres.conf file
	 */
	private static void initParams() {
		if (!hasParams) {
			hasParams = true;
			try {
				StorageFactory.getStorage().initParameters();
				hasParams = true;
				Properties params = new Properties();
				params.load(new FileInputStream(System.getProperty("user.dir")
						+ File.separator + "resources" + File.separator
						+ StorageFactory.getStorage().getConfFileName()));

				String prefix_name = params.getProperty("prefix_name");
				String prefix = params.getProperty("prefix");

				count_patients_by_tss_Q = DataSelection.queryInitReplace(
						params.getProperty("count_patients_by_tss"),
						prefix_name, prefix);
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
	}

	/**
	 * 
	 * @param diseaseAbbr
	 *            - example: "brca", "ov", "hnsc"
	 * @param tssName
	 *            - name of tissue source site for ex.
	 *            "University of Pittsburgh"
	 * @return number of patients for a particular disease and tss as JsonObject
	 * @throws QueryException
	 */
	public static JSONObject countPatientsByTSS(String diseaseAbbr,
			String tssName) throws QueryException {
		initParams();

		String q = count_patients_by_tss_Q.replace("<diseaseAbbr>",
				diseaseAbbr.toLowerCase());
		q = q.replace("<tssName>", tssName);
		try {
			return StorageFactory.getStorage().getJSONResult(
					StorageFactory.getStorage().getSparqlURL(), q);
		} catch (QueryException e) {
			throw e;
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			EnterpriseAnalytics.initParams();
			// number of UPMC patients with brca cancer
			System.out.println("number of Patients: "
					+ EnterpriseAnalytics.countPatientsByTSS("brca",
							"University of Pittsburgh"));
		} catch (QueryException e) {
			e.printStackTrace();
		}

	}

}
