package edu.pitt.dbmi.ipm.service.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import com.google.gson.Gson;

import edu.pitt.dbmi.ipm.service.DataSelection;
import edu.pitt.dbmi.ipm.service.FileStreamingOutput;
import edu.pitt.dbmi.ipm.service.Protocol;
import edu.pitt.dbmi.ipm.service.QueryException;
import edu.pitt.dbmi.ipm.service.TomcatHelper;

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
 * PostrgeSQL instance of storage
 * 
 * @author opm1
 * @version 1
 * @since Dec 8, 2015
 * 
 */
public class PostgreStorage extends Storage {

	private static Log log = LogFactory.getLog(PostgreStorage.class);

	private String DRIVER = null, URL = null, USER = null, PASS = null;

	private boolean hasParams = false;

	private Connection conn = null;

	private static final Storage INSTANCE = new PostgreStorage();

	private static final String UPSERT = "UPDATE <graphName> SET <col_name>='<col_val>' WHERE uuid='<uuid_val>'; "
			+ "INSERT INTO <graphName> (uuid, <col_name>) "
			+ "SELECT '<uuid_val>', '<col_val>' "
			+ "WHERE NOT EXISTS (SELECT 1 FROM <graphName> WHERE uuid='<uuid_val>');";

	private static final String INSERT = "INSERT INTO <graphName> (uuid, <col_name>) "
			+ "VALUES('<uuid_val>', '<col_val>');";

	private static final String GET_DIS_DATATYPE_ID = "SELECt uuid FROM diseaseDataType_pairs "
			+ "WHERE studyabbreviation = '<disAbbr>' AND datatype = '<dataType>'";

	private static final String DELETE = "UPDATE <graphName> SET <col_name>=NULL WHERE uuid='<uuid_val>';";
	private static final String DELETE_IN_SUBSCRIPTION = "DELETE FROM subscription WHERE uuid='<uuid_val>' AND <col_name>='<col_val>';";

	private static final String dateFilter = " datecreated <= '<dateTime>' AND (datearchived IS NULL OR datearchived > '<dateTime>')";

	private Map<String, String> labelMethodMap = null;
	private Map<String, String> labelFilterNameMap = null;
	private Map<String, String> filterPredicateNameMap = null;

	public static Storage getInstance() {
		return INSTANCE;
	}

	@Override
	public boolean initParameters() {
		if (hasParams)
			return hasParams;
		try {
			hasParams = true;
			Properties params = TomcatHelper
					.getProperties("jQueryPostgres.conf");
			DRIVER = params.getProperty("driver");
			URL = params.getProperty("url");
			USER = params.getProperty("user");
			PASS = params.getProperty("pass");

			labelMethodMap = jsonToMap(params.getProperty("label_method_list"),
					true);
			labelFilterNameMap = jsonToMap(
					params.getProperty("label_filtername_list"), true);
			filterPredicateNameMap = jsonToMap(
					params.getProperty("filter_pedicate_name_list"), true);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return hasParams;

	}

	@Override
	public Map<String, String> getLabelMethodMap() {
		return labelMethodMap;
	}

	/**
	 * Example String jsonReq =
	 * {results:[{key:\"Disease\",value:[\"brca\"]},{key:\"Sample
	 * Type\",value:[\"Primary solid Tumor\",\"Metastatic\"]}, {key:\"Analysis
	 * Type\",value:[\"Protected_Mutations\"]},{key:\"Tissue Source
	 * Site\",value:[\"University of Pittsburgh\"]}, {key:\"Snapshot By
	 * Date\",value:[\"2014-03-21\"]}]}
	 */
	@Override
	public String getQuerySearchString(String template, String jsonStr) {
		String q = template;
		String dateLabel = null;
		String AND = " WHERE ";
		// "\""+dateFormat.format(new Date())+"\"";
		Map<String, String> requestMap = jsonToMap(jsonStr, false);
		String label = null, filterName = null, value = null;
		for (Map.Entry<String, String> entry : labelFilterNameMap.entrySet()) {
			label = entry.getKey();
			value = requestMap.get(label);

			filterName = entry.getValue();

			if (label.equalsIgnoreCase("Snapshot By Date")) {
				if (value != null)
					dateLabel = value;
			} else {

				if (value != null && !value.equalsIgnoreCase("\"ALL\"")) {
					q = q.replace(
							filterName,
							replaceByFilter(filterName, filterPredicateNameMap
									.get(filterName).split(","), value, AND));
					if (AND.equals(" WHERE "))
						AND = " AND ";

				} else
					q = q.replaceAll(filterName, "");

			}
		}

		String rep = "";

		if (dateLabel != null) {
			dateLabel = dateLabel.replaceAll("\"", "");
			rep = AND + dateFilter.replaceAll("<dateTime>", dateLabel);
		}

		q = q.replaceAll("<filter_dateTime>", rep);

		return q;
	}

	/**
	 * Replaces place holders in query string
	 * 
	 * @param filterName
	 * @param nameArr
	 * @param values
	 * @param AND
	 * @return String
	 */
	private String replaceByFilter(String filterName, String[] nameArr,
			String values, String AND) {

		String[] valArr = values.split("\",\"");

		StringBuilder filter = new StringBuilder();
		// multi vallue part is NOT tested!
		if (nameArr.length > 1) {
			String[] temp = null;
			for (String val : valArr) {
				val = val.replaceAll("\"", "");
				temp = val.split(": ");
				filter.append(nameArr[0] + "='" + temp[0] + "' AND "
						+ nameArr[1] + "= '" + temp[1] + "' OR ");

			}
		} else
			filter.append(" " + nameArr[0] + " IN("
					+ values.replaceAll("\"", "'") + ") ");

		return AND + filter;
	}

	@Override
	public boolean initParametersLocal() {
		if (hasParams)
			return hasParams;
		synchronized (PostgreStorage.class) {
			try {

				hasParams = true;
				Properties params = new Properties();
				params.load(new FileInputStream(System.getProperty("user.dir")
						+ File.separator + "resources" + File.separator
						+ "jQueryPostgres.conf"));

				DRIVER = params.getProperty("driver");
				URL = params.getProperty("url");
				USER = params.getProperty("user");
				PASS = params.getProperty("pass");

				labelMethodMap = jsonToMap(
						params.getProperty("label_method_list"), true);
				labelFilterNameMap = jsonToMap(
						params.getProperty("label_filtername_list"), true);
				filterPredicateNameMap = jsonToMap(
						params.getProperty("filter_pedicate_name_list"), true);

			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
		return hasParams;
	}

	@Override
	public JSONObject getJSONResult(String SPARQL_URL, String query)
			throws QueryException {
		JSONObject jsonObj = null;
		List<Map<String, Object>> listOfMaps = null;
		Connection connection = null;
		try {
			connection = getConnection();
			QueryRunner queryRunner = new QueryRunner();
			listOfMaps = queryRunner.query(connection, query,
					new MapListHandler());

			// convert list of maps to listof maps of maps
			List<Map<String, Map<String, Object>>> newList = new LinkedList<Map<String, Map<String, Object>>>();
			Map<String, Map<String, Object>> outerMap = null;
			Map<String, Object> innerMap = null;
			List<String> headerList = new LinkedList<String>();
			Object value = "";
			String key = null;
			for (Map<String, Object> oldMap : listOfMaps) {
				outerMap = new HashMap<String, Map<String, Object>>();
				for (Map.Entry<String, Object> entry : oldMap.entrySet()) {
					key = entry.getKey();
					innerMap = new HashMap<String, Object>();
					value = entry.getValue();
					if (value != null) {
						innerMap.put("value", value);
						outerMap.put(key, innerMap);
					}
					if (!headerList.contains(key))
						headerList.add(key);
				}
				newList.add(outerMap);
			}

			String newJson = "{\"results\":{\"bindings\":"
					+ new Gson().toJson(newList) + "},\"head\":{\"vars\":"
					+ new Gson().toJson(headerList) + "}}";

			listOfMaps.clear();
			listOfMaps = null;
			newList.clear();
			newList = null;

			jsonObj = new JSONObject(newJson);

		} catch (SQLException se) {
			throw new QueryException(
					"PostgreStorage getJSONResult: Couldn't query the database."
							+ se.getMessage());
		} catch (JSONException e) {
			throw new QueryException("PostgreStorage getJSONResult: "
					+ e.getMessage());
		} catch (Exception e) {
			throw new QueryException("PostgreStorage getJSONResult: "
					+ e.getMessage());
		} finally {
			DbUtils.closeQuietly(connection);
		}

		return jsonObj;
	}

	@Override
	public String nameWithPrefix(String prefix, String n) {
		if (n.startsWith(prefix))
			n = n.substring(prefix.length());
		return n;
	}

	@Override
	public String nameWithPrefixPorG(String prefix, String value) {
		return formatPorG(value);
	}

	@Override
	public Response getTextStream(String qUrl, String body, String tableFormat)
			throws QueryException {
		String delim = (tableFormat.equalsIgnoreCase("csv")) ? "," : "\t";

		File temp_file = new File(TomcatHelper.getTempDirectory() + "temp_"
				+ UUID.randomUUID().toString());
		FileWriter fr = null;
		try {
			fr = new FileWriter(temp_file);
			CopyManager copyManagerA = new CopyManager(
					(BaseConnection) getConnection());
			copyManagerA.copyOut("COPY (" + body
					+ ") TO STDOUT WITH DELIMITER AS '" + delim
					+ "' CSV HEADER NULL AS '';", fr);
			fr.close();
			fr = null;

			FileStreamingOutput stream = new FileStreamingOutput(temp_file);

			return Response.ok(stream).build();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
				}
				fr = null;
			}
		}
		return null;
	}

	@Override
	public void update(String statement) {
		Connection connection = null;
		PreparedStatement st = null;
		try {
			connection = getConnection();
			st = connection.prepareStatement(statement);
			st.executeUpdate();
			st.close();
		} catch (Exception ex) {
			// ex.printStackTrace();
			if (ex.getMessage().indexOf(
					"A result was returned when none was expected.") == -1)
				System.err.println("PostgreStatement update: " + ex.toString());

		} finally {
			DbUtils.closeQuietly(st);
			DbUtils.closeQuietly(connection);
		}

	}

	/**
	 * 
	 * @param res
	 * @return JSONArray
	 */
	public JSONArray getBindings(JSONObject res) {
		JSONObject jsonRes;
		try {
			// case
			// {results:[{key:\"Disease\",value:[\"brca\"]},{key:\"Analysis Type\",value:[\"Protected_Mutations\"]}]}"
			if (res.toString().indexOf("results") > -1
					&& res.toString().indexOf("bindings") == -1) {
				return res.getJSONArray("results");
			}
			jsonRes = new JSONObject(res.getString("results"));
			JSONArray bindings = jsonRes.getJSONArray("bindings");
			if (bindings.length() == 0)
				return null;
			return bindings;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("QueryHelper getBindings: " + e.toString());
			return null;
		}
	}

	@Override
	public String formatPorG(String value) {
		return value.toLowerCase().replaceAll("-", "_");
	}

	@Override
	public Map<String, String> jsonToMap(String jsonStr,
			boolean removeDoulbeQuotes) {

		try {
			Map<String, String> map = new LinkedHashMap<String, String>();
			JSONArray res = getBindings(new JSONObject(jsonStr));
			String val = null;
			for (int i = 0; i < res.length(); i++) {
				JSONObject c = res.getJSONObject(i);
				val = c.getString("value");
				val = val.substring(1, val.length() - 1);
				if (removeDoulbeQuotes)
					val = val.replaceAll("\"", "");
				map.put(c.getString("key"), val);

			}
			return map;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	

	/**
	 * Connecting to DB
	 * 
	 * @return Connection
	 * @throws Exception
	 */
	private Connection getConnection() throws Exception {
		try {
			if (conn == null || conn.isClosed()) {
				try {
					Class.forName(DRIVER).newInstance();
					conn = DriverManager.getConnection(URL, USER, PASS);
				} catch (org.postgresql.util.PSQLException e) {
					String mess = e.getMessage();
					if (mess.startsWith("FATAL: database")
							&& mess.endsWith("does not exist"))
						System.err.println("NO DBL " + mess);
					throw new Exception("PostgreStorage getConnection: "
							+ e.getMessage());
				}
			}
		} catch (SQLException e) {
			System.err.println("PostgreStorage getConnection SQLException: "
					+ e.getMessage());
			throw new Exception("PostgreStorage getConnection: "
					+ e.getMessage());
		}
		return conn;
	}

	@Override
	public String nameWithPrefixUUID(String prefix, String value) {
		if (value.startsWith(prefix))
			value = value.substring(prefix.length());
		return value;
	}

	@Override
	public String literal(String value) {
		return value;
	}

	@Override
	public String formatNowMetaFile() {
		return formatNowNoTZ().replace(" ", "T") + "Z";
	}

	@Override
	public String formatTimeInStorage(String dateTime) {
		String localObj = dateTime.replace("T", " ");
		return localObj.replace("Z", "");
	}

	@Override
	public String getSparqlURL() {
		return "";
	}

	@Override
	public String getConfFileName() {
		return "jQueryPostgres.conf";
	}

	/**
	 * 
	 * @param diseaseAbbr
	 * @param date
	 *            format "yyyy-MM-dd"
	 * @param patients
	 *            format: use comma+space as a delimiter. Example: TCGA-A1-A0SB,
	 *            TCGA-A1-A0SD
	 * @return JSONArray
	 * @throws QueryException
	 */
	@Override
	public JSONObject metadataByDiseaseDateForPatientList(String diseaseAbbr,
			String date, String[] patients) throws QueryException {
		String pFilter = "";
		if (patients != null && patients.length > 0) {
			StringBuilder sb = new StringBuilder("AND patientbarcode IN(");
			String prefix = "";
			for (String s : patients) {
				sb.append(prefix);
				sb.append("'" + s + "'");
				prefix = ",";
			}
			sb.append(") ");
			pFilter = sb.toString();
		}
		String q = DataSelection.sample_list_data_Q.replace("<diseaseAbbr>",
				diseaseAbbr.toLowerCase());
		q = q.replace("<patientListFilter>", pFilter);
		q = q.replaceAll("<dateTime>", date);
		try {
			return getJSONResult(getSparqlURL(), q);
		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * If there is no account, the new id will be created
	 * 
	 * @param email
	 * @param insert
	 *            - boolean, TRUE for insert, FALSE for delete
	 * @return subject without prefix
	 * @throws QueryException
	 */
	@Override
	public String getUserID(String email, boolean insert) throws QueryException {
		StringBuilder sb = new StringBuilder(
				"SELECT uuid FROM subscription WHERE email ='");
		sb.append(literal(email));
		sb.append("'");
		try {
			JSONObject res = getJSONResult(getSparqlURL(), sb.toString());

			JSONArray bindings = getBindings(res);
			String userID = null;
			if (bindings == null || bindings.length() == 0) {
				if (insert) {
					userID = UUID.randomUUID().toString();
					// insert new user
					upsert(userID, "email", email, "subscription");
					// insert new user
				}

			} else if (bindings.length() > 0) {
				JSONObject jsonBin = new JSONObject(bindings.getString(0));
				userID = new JSONObject(jsonBin.getString("uuid"))
						.getString("value");

			}
			return userID;
		} catch (QueryException e) {
			log.error("In getUserID: " + e);
			throw e;
		} catch (JSONException e) {
			log.error("In getUserID: " + e);
			throw new QueryException("QueryException: in getUserID: " + e);
		}

	}

	@Override
	public void unsubscribeCompletely(String email) throws QueryException {
		try {
			String id = getUserID(email, false);
			if (id != null) {
				String q = "DELETe FROM subscription  WHERE uuid='<userID>'"
						.replace("<userID>", id);
				update(q);
			}
		} catch (QueryException e) {
			log.error("In unSubscribeCompletely:", e);
			throw e;
		}
	}

	@Override
	public void insertDeletePairs(String email, String[] disAbbr,
			String[] dataType, boolean doInsert) throws QueryException {
		try {
			String userID = getUserID(email, doInsert);
			if (userID != null) {
				for (int i = 0; i < disAbbr.length; i++) {
					// get pair
					String disDataTypeID = getDiseaseDataTypeID(disAbbr[i],
							dataType[i]);
					if (doInsert)
						upsert(userID, "pair", disDataTypeID, "subscription");

					else
						delete(userID, "pair", disDataTypeID, "subscription");
				}
			}

		} catch (QueryException e) {
			log.error("In insertDeletePairs:", e);
			throw e;
		}

	}

	/**
	 * Delete record represented as Nquad for DB
	 * 
	 * @param s
	 * @param p
	 * @param o
	 * @param graph
	 * @throws QueryException
	 */
	public void delete(String s, String p, String o, String graph)
			throws QueryException {
		String ups = (graph.equalsIgnoreCase("subscription")) ? DELETE_IN_SUBSCRIPTION
				: DELETE;
		ups = ups.replaceAll("<graphName>", graph);
		ups = ups.replaceAll("<col_name>", p);
		ups = ups.replaceAll("<col_val>", o);
		ups = ups.replaceAll("<uuid_val>", s);
		update(ups);
	}

	/**
	 * Insert or update record represented as Nquad
	 * 
	 * @param subj
	 * @param p
	 * @param o
	 * @param g
	 */
	private void upsert(String subj, String p, String o, String g) {
		// subsctiption is a special case - multiple values for the same userId
		String ups = (g.equalsIgnoreCase("subscription") || g
				.equalsIgnoreCase("protocol")) ? INSERT : UPSERT;
		ups = ups.replaceAll("<graphName>", g);
		ups = ups.replaceAll("<col_name>", p);
		ups = ups.replaceAll("<col_val>", o);
		ups = ups.replaceAll("<uuid_val>", subj);
		update(ups);
	}

	@Override
	public void updateProtocol(String statement) throws QueryException {
		String[] stArr = statement.split(Protocol.LINE_BREAK);
		try {
			String[] eachSt = null;
			for (String stStr : stArr) {
				eachSt = stStr.split(Protocol.SEP);
				upsert(eachSt[0], eachSt[1], eachSt[2], "protocol");
			}

			stArr = null;
			eachSt = null;
		} catch (Exception e) {
			log.error("In PostgreStorage updateProtocol:", e);
			throw new QueryException(e.getMessage());
		}
	}

	/**
	 * Returns diseaseDataType_pairs id
	 * 
	 * @param disAbbr
	 * @param dataType
	 * @return String
	 * @throws QueryException
	 */
	private String getDiseaseDataTypeID(String disAbbr, String dataType)
			throws QueryException {
		String query = GET_DIS_DATATYPE_ID.replace("<disAbbr>", disAbbr);
		query = query.replace("<dataType>", dataType);

		String id = null;

		try {
			JSONObject res = getJSONResult(RDFStorage.SPARQL_URL, query);
			JSONArray bindings = getBindings(res);
			if (bindings.length() == 0) {
				id = UUID.randomUUID().toString();
				upsert(id, "studyabbreviation", disAbbr,
						"diseasedatatype_pairs");
				upsert(id, "datatype", dataType, "diseasedatatype_pairs");
			} else {
				JSONObject jsonBin = new JSONObject(bindings.getString(0));
				id = new JSONObject(jsonBin.getString("uuid"))
						.getString("value");
			}

			return id;
		} catch (QueryException e) {
			log.error("In getDiseaseDataTypeID: " + e);
			throw e;
		} catch (JSONException e) {
			log.error("In getDiseaseDataTypeID: " + e);
			throw new QueryException(
					"QueryException: in getDiseaseDataTypeID: " + e);
		}

	}

}
