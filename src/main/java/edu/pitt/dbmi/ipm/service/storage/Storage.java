package edu.pitt.dbmi.ipm.service.storage;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.pitt.dbmi.ipm.service.QueryException;

/**
 * 
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
 * Abstract Storage class
 * 
 * @author opm1
 * @version 1
 * @since Dec 8, 2015
 * 
 */
public abstract class Storage {

	private Properties params = null;
	private static SimpleDateFormat df = null;
	private static String TIMESTAMP_MILLI_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public abstract String getConfFileName();

	public abstract boolean initParameters();

	public abstract boolean initParametersLocal();

	public abstract JSONObject getJSONResult(String SPARQL_URL, String query)
			throws QueryException;

	public abstract String nameWithPrefix(String prefix, String n);

	public abstract String nameWithPrefixPorG(String prefix, String value);

	public abstract String nameWithPrefixUUID(String prefix, String value);

	public abstract Response getTextStream(String qUrl, String body,
			String tableFormat) throws QueryException;

	public abstract void update(String statement) throws QueryException;

	public abstract Map<String, String> jsonToMap(String jsonStr,
			boolean removeDoulbeQuotes);

	public abstract String literal(String value);

	public abstract String formatPorG(String str); // TEMPO: assuming it's in
													// Virtuoso format initially

	public abstract String formatNowMetaFile(); // preserving the old format for
												// Metadata.tsv

	public abstract String formatTimeInStorage(String dateTime);

	public abstract String getSparqlURL();

	public abstract JSONObject metadataByDiseaseDateForPatientList(
			String diseaseAbbr, String date, String[] patients)
			throws QueryException;

	public abstract void insertDeletePairs(String email, String[] disAbbr,
			String[] dataType, boolean doInsert) throws QueryException;

	public abstract String getUserID(String email, boolean insert)
			throws QueryException;

	public abstract void unsubscribeCompletely(String email)
			throws QueryException;

	public abstract String getQuerySearchString(String template, String jsonStr);

	public abstract Map<String, String> getLabelMethodMap();

	public abstract void updateProtocol(String statement) throws QueryException;

	/**
	 * Returns a single JSON-formatted string
	 * 
	 * @param res
	 * @param jObj
	 * @param jArr
	 * @param val
	 * @param key
	 */
	public String getOneValueJSONObject(JSONObject res, String jObj,
			String jArr, String val, String key) {
		StringBuilder sb = new StringBuilder("{key:" + "\"" + key + "\"");
		sb.append(",value:[");
		sb.append(jsonAsQuotedStr(res, jObj, jArr, val));
		sb.append("]}");

		return sb.toString();
	}

	/**
	 * Return query result as array of Strings
	 * 
	 * @param res
	 * @param jObj
	 * @param jArr
	 * @param val
	 * @return Strings[]
	 */
	public String[] jsonResAsArray(JSONObject res, String jObj, String jArr,
			String val) {
		try {
			JSONArray bindings = (res.getJSONObject(jObj)).getJSONArray(jArr);
			String userID = null;
			List<String> rr = new ArrayList<String>();
			for (int i = 0; i < bindings.length(); i++) {
				JSONObject c = bindings.getJSONObject(i);
				rr.add(new JSONObject(c.getString(val)).getString("value"));
			}
			Collections.sort(rr, String.CASE_INSENSITIVE_ORDER);
			String[] toret = rr.toArray(new String[rr.size()]);
			rr.clear();
			rr = null;
			return toret;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * Adds quotes to resulting String
	 * 
	 * @param res
	 * @param jObj
	 * @param jArr
	 * @param val
	 * @return String
	 */
	protected String jsonAsQuotedStr(JSONObject res, String jObj, String jArr,
			String val) {
		String[] resArr = jsonResAsArray(res, jObj, jArr, val);
		StringBuilder sb = new StringBuilder();
		for (String s : resArr)
			sb.append("\"" + s + "\",");
		sb.setLength(sb.length() - 1);
		resArr = null;
		return sb.toString();

	}

	/**
	 * Loads parameters from appropriate config file ( based on
	 * 'useVitOrPostgre' parameter setting in jQueryPostgres.conf file)
	 * 
	 * @param confFileName
	 */
	protected void load(String confFileName) {
		try {
			params = new Properties();
			params.load(new FileInputStream(System.getProperty("user.dir")
					+ File.separator + "resources" + File.separator
					+ confFileName));
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	/**
	 * Formatting time
	 * 
	 * @return String
	 */
	protected static String formatNowNoTZ() {
		return getDateTimeFormat().format(new Date()).replace(" ", "T");
	}

	/**
	 * 
	 * @return date format with UTC time zone
	 */
	protected static SimpleDateFormat getDateTimeFormat() {
		if (df == null) {
			df = new SimpleDateFormat(TIMESTAMP_MILLI_FORMAT);
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
		}
		return df;
	}

}
