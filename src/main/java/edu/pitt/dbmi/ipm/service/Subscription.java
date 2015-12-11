package edu.pitt.dbmi.ipm.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.UUID;

import javax.ws.rs.Path;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.pitt.dbmi.ipm.service.storage.RDFStorage;
import edu.pitt.dbmi.ipm.service.storage.Storage;
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
 * Manages user subscription for notification about new or updated disease and /
 * or analysis types.
 * 
 * @author opm1
 * @version 1
 * @since Dec 8, 2015
 * 
 */

@Path("/subscription/")
public class Subscription {
	private static Log log = LogFactory.getLog(Subscription.class);

	/**
	 * @return diseases from TCGA reports
	 */
	public static JSONObject getAllDiseases() throws QueryException {
		try {
			return StorageFactory.getStorage().getJSONResult(
					StorageFactory.getStorage().getSparqlURL(),
					DataSelection.all_diseases_Q);
		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * @return analysis (date) types from TCGA reports
	 */
	public static JSONObject getAllDatatypes() throws QueryException {
		try {
			return StorageFactory.getStorage().getJSONResult(
					StorageFactory.getStorage().getSparqlURL(),
					DataSelection.all_datatypes_Q);
		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * Return diseases and data types the user is subscribed for
	 * 
	 * @param email
	 * @return JSONObject
	 * @throws QueryException
	 */
	public static JSONObject getSubscription(String email)
			throws QueryException {
		try {
			Storage storage = StorageFactory.getStorage();
			String query = DataSelection.get_subsc_Q.replace("<email>",
					storage.literal(email));
			return storage.getJSONResult(storage.getSparqlURL(), query);
		} catch (QueryException e) {
			log.error("In getSubscription:", e);
			throw e;
		}
	}

	/**
	 * 
	 * @param email
	 *            - user email
	 * @param disAbbr
	 *            - list of disease abbrebiations
	 * @param dataType
	 *            - corresponding list for dataTypes. Should have the same size
	 *            as the disAbbr array.
	 * @throws QueryException
	 */
	public static void subscribe(String email, String[] disAbbr,
			String[] dataType) throws QueryException {

		try {
			insertDeletePairs(email, disAbbr, dataType, true);

		} catch (QueryException e) {
			log.error("In insertDeletePairs:", e);
			throw e;
		}
	}

	/**
	 * unsubscribe from a specific disease-dataType pairs
	 * 
	 * @param email
	 * @param disAbbr
	 * @param dataType
	 * @throws QueryException
	 */
	public static void unsubscribe(String email, String[] disAbbr,
			String[] dataType) throws QueryException {

		try {
			insertDeletePairs(email, disAbbr, dataType, false);

		} catch (QueryException e) {
			log.error("In unsubscribe:", e);
			throw e;
		}
	}

	/**
	 * unsubscribe completely and remove email from the store
	 * 
	 * @param email
	 */
	public static void unsubscribeCompletely(String email)
			throws QueryException {
		try {
			StorageFactory.getStorage().unsubscribeCompletely(email);
		} catch (QueryException e) {
			log.error("In unSubscribeCompletely:", e);
			throw e;
		}

	}

	private static void insertDeletePairs(String email, String[] disAbbr,
			String[] dataType, boolean doInsert) throws QueryException {
		try {
			StorageFactory.getStorage().insertDeletePairs(email, disAbbr,
					dataType, doInsert);

		} catch (QueryException e) {
			log.error("In insertDeletePairs:", e);
			throw e;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean hasConnectorParams = StorageFactory.getStorage()
				.initParametersLocal();
		boolean hasQueryParams = DataSelection.initParametersLocal();

		try {
			// test
			String[] disAbbr = { "blca" };
			String[] datTypes = { "Protected_Mutations" };
			subscribe("fake@example.com", disAbbr, datTypes);
			System.out.println("getSubscription: "
					+ getSubscription("fake@example.com"));

			unsubscribe("fake@example.com", disAbbr, datTypes);
			System.out.println("getSubscription: "
					+ getSubscription("fake@example.com"));

		} catch (QueryException e) {
			e.printStackTrace();
		}
		System.out.println("DONE");

	}

}
