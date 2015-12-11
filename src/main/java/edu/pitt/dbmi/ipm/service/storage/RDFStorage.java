package edu.pitt.dbmi.ipm.service.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.pitt.dbmi.ipm.service.DataSelection;
import edu.pitt.dbmi.ipm.service.EntityStreamingOutput;
import edu.pitt.dbmi.ipm.service.QueryException;
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
 * Virtuoso storage
 * 
 * @author opm1
 * @version 1
 * @since Dec 8, 2015
 * 
 */
public class RDFStorage extends Storage {
	private static Log log = LogFactory.getLog(RDFStorage.class);

	public static String[] qHeaderTypes = null;
	public static String[] qHeaderValues = null;
	public static String[] updHeaderTypes = null;
	public static String[] updHeaderValues = null;
	public static String[] tsvHeaderValues = null;
	public static String[] csvHeaderValues = null;

	public static String PROTOCOL = null;
	public static String HOST = null;
	public static String PORT = null;
	// public static String SPARQL_ENDPOINT = "sparql-auth/";
	public static String SPARQL_ENDPOINT = null;
	public static String UPDATE_ENDPOINT = null;
	public static String USER = null;
	public static String PWD = null;
	// endpoints
	public static String SPARQL_URL = null;
	public static String UPDATE_URL = null;

	// query stuff
	public static final String SPACE = " ";
	public static final String PREFIX_NAME = "pgrr:";
	private String dateTimeSuff = "^^<http://www.w3.org/2001/XMLSchema#dateTime>";

	// storage location
	public static final String STORAGE_PATH = "\"\"";

	private boolean hasParams = false;
	// replace all spaces with it for web service
	public static String GET = "GET";
	public static String POST = "POST";

	private static final Storage INSTANCE = new RDFStorage();

	private static final String INSERT_DELETE_PAIRS = "PREFIX pgrr:<http://purl.org/pgrr/core#> "
			+ "<INSERT_DELETE> GRAPH pgrr:subscription { "
			+ "<userID> pgrr:pair <pair> . }";
	private static final String INSERT_EMAIL = "PREFIX pgrr:<http://purl.org/pgrr/core#> "
			+ "INSERT IN GRAPH pgrr:subscription "
			+ "{ <userID> pgrr:email <email> .  } ";

	private static final String GET_DIS_DATATYPE_ID = "PREFIX pgrr:<http://purl.org/pgrr/core#> "
			+ "SELECt ?id FROM pgrr:diseaseDataType-pairs "
			+ "WHERE { ?id pgrr:studyAbbreviation <disAbbr>. "
			+ " ?id pgrr:dataType <dataType> . }";

	private static final String INSERT_DIS_DATATYPE = "PREFIX pgrr:<http://purl.org/pgrr/core#> "
			+ "INSERT IN GRAPH pgrr:diseaseDataType-pairs { "
			+ " <ID> pgrr:studyAbbreviation <disAbbr>. "
			+ " <ID> pgrr:dataType <dataType> . }";

	private static final String UNSUBSCRIBE_ME = "prefix pgrr:<http://purl.org/pgrr/core#> "
			+ "DELETE FROM GRAPH pgrr:subscription {?s  ?p ?o } "
			+ "FROM pgrr:subscription WHERE { ?s ?p ?o . "
			+ "FILTER (?s = <userID>) }";

	private static final String logTemplate = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
			+ "INSERT IN GRAPH <http://purl.org/pgrr/core#protocol> { \n<triples> }";

	private Map<String, String> labelMethodMap = null;
	private Map<String, String> labelFilterNameMap = null;
	private Map<String, String> filterPredicateNameMap = null;
	private Map<String, String> optionalFilterSearchStrMap = null;

	private String prefix_name = null;

	private static final String OPTIONAL_DATE_FILTER = "FILTER(xsd:dateTime(?dateCreated) <= xsd:dateTime(<dateTime>) && "
			+ "(!BOUND(?dateArchived) || xsd:dateTime(?dateArchived) > xsd:dateTime(<dateTime>)))";

	/**
	 * 
	 * @return instance
	 */
	public static Storage getInstance() {
		return INSTANCE;
	}

	@Override
	public JSONObject getJSONResult(String SPARQL_URL, String query)
			throws QueryException {

		try {

			return getJSONResult(qHeaderTypes, qHeaderValues, SPARQL_URL, query);
		} catch (QueryException e) {
			throw e;
		}

	}

	/**
	 * sample URL: http://localhost:8080/ipm-ws/data-subscription/
	 * getDownloadFileInformation
	 * ?jsonReq={"results":[{key:"Disease",value:["acc"
	 * ]}]}&extended=false&tableFormat=csv
	 * 
	 * @param qUrl
	 * @param body
	 * @param tableFormat
	 *            : currently accepting {"csv", "tsv"}
	 * @return Response
	 * @throws QueryException
	 */
	@Override
	public Response getTextStream(String qUrl, String body, String tableFormat)
			throws QueryException {

		HttpClient httpclient = getHttpClient();
		try {
			qUrl = buildQueryURL(qUrl, body);
			String[] hTypes = qHeaderTypes;
			String[] hValues = null;
			if (tableFormat.equalsIgnoreCase("CSV"))
				hValues = csvHeaderValues;
			else
				hValues = tsvHeaderValues;

			HttpPost httppost = new HttpPost(qUrl);
			for (int i = 0; i < hTypes.length; i++)
				httppost.setHeader(hTypes[i], hValues[i]);

			HttpResponse response = httpclient.execute(httppost);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK
					&& statusCode != HttpStatus.SC_CREATED)
				throw new QueryException(
						"QueryException: in getPostResponse: HTTP Status Code: "
								+ statusCode);

			if (statusCode == HttpStatus.SC_OK) {

				EntityStreamingOutput so = new EntityStreamingOutput(
						httpclient, response.getEntity());

				return Response.ok(so, MediaType.TEXT_PLAIN).build();
			}
		} catch (UnsupportedEncodingException e) {
			log.error("In getPostResponse:", e);
			throw new QueryException("QueryException: in getPostResponse: " + e);
		} catch (ClientProtocolException e) {
			log.error("In getPostResponse:", e);
			throw new QueryException("QueryException: in getPostResponse: " + e);
		} catch (IOException e) {
			log.error("In getPostResponse:", e);
			throw new QueryException("QueryException: in getPostResponse: " + e);
		}

		return null;
	}

	/**
	 * Returns default one now.
	 * 
	 * @return HttpClient
	 */
	private static HttpClient getHttpClient() {
		HttpClient httpclient = new DefaultHttpClient();

		return httpclient;
	}

	@Override
	public String nameWithPrefix(String pefix, String n) {
		return "<" + pefix + n + ">";
	}

	@Override
	public String nameWithPrefixPorG(String pefix, String n) {
		return "<" + pefix + n + ">";
	}

	/**
	 * Builds http query string
	 * 
	 * @param SPARQL_URL
	 * @param query
	 * @return String
	 * @throws UnsupportedEncodingException
	 */
	private static String buildQueryURL(String SPARQL_URL, String query)
			throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder(SPARQL_URL + "?query=");
		try {
			sb.append(URLEncoder.encode(query, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			log.error("In buidQueryURL:", e);
			throw e;
		}
		return sb.toString();
	}

	@Override
	public String literal(String value) {
		return "\"" + value + "\"";
	}

	@Override
	public String formatPorG(String str) {
		return str;
	}

	@Override
	public String formatNowMetaFile() {
		return formatNowNoTZ().replace(" ", "T") + "Z";
	}

	@Override
	public String formatTimeInStorage(String dateTime) {
		return "\"" + dateTime + "\"" + dateTimeSuff;
	}

	/**
	 * for array param use ";" as a separator
	 */
	@Override
	public boolean initParameters() {
		if (hasParams)
			return hasParams;
		try {
			hasParams = true;
			/*
			 * Properties params = new Properties(); params.load(new
			 * FileInputStream(System.getProperty("user.dir")+
			 * File.separator+"resources"+File.separator+"jVirt.conf"));
			 */
			
			
			//Properties params = TomcatHelper.getProperties("jVirt.conf");
			Properties params = TomcatHelper.getProperties(getConfFileName());
			
			PROTOCOL = params.getProperty("rdf_protocol").trim();
			HOST = params.getProperty("rdf_host").trim();
			PORT = params.getProperty("rdf_port").trim();
			SPARQL_ENDPOINT = params.getProperty("query_endpoint").trim();
			UPDATE_ENDPOINT = params.getProperty("update_endpoint").trim();
			USER = params.getProperty("rdf_username").trim();
			PWD = params.getProperty("rdf_password").trim();
			qHeaderTypes = (params.getProperty("query_headerTypes[]"))
					.split(";");
			qHeaderValues = (params.getProperty("query_headerValues[]"))
					.split(";");
			updHeaderTypes = (params.getProperty("update_headerTypes[]"))
					.split(";");
			updHeaderValues = (params.getProperty("update_headerValues[]"))
					.split(";");

			tsvHeaderValues = (params.getProperty("tsv_headerValues[]"))
					.split(";");
			csvHeaderValues = (params.getProperty("csv_headerValues[]"))
					.split(";");

			SPARQL_URL = PROTOCOL + HOST + ":" + PORT + SPARQL_ENDPOINT;
			UPDATE_URL = PROTOCOL + USER + ":" + PWD + "@" + HOST + ":" + PORT
					+ "/" + UPDATE_ENDPOINT;

			//Properties paramsQ = TomcatHelper.getProperties(getConfFileName());

			prefix_name = params.getProperty("prefix_name");
			// maps for queries
			labelMethodMap = jsonToMap(
					params.getProperty("label_method_list"), true);
			labelFilterNameMap = jsonToMap(
					params.getProperty("label_filtername_list"), true);
			filterPredicateNameMap = jsonToMap(
					params.getProperty("filter_pedicate_name_list"), true);
			optionalFilterSearchStrMap = jsonToMap(
					params.getProperty("optional_filter_search_string_list"),
					true);

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return hasParams;
	}

	@Override
	public boolean initParametersLocal() {
		if (hasParams)
			return hasParams;
		synchronized (RDFStorage.class) {
			try {

				hasParams = true;
				Properties params = new Properties();
				params.load(new FileInputStream(System.getProperty("user.dir")
						+ File.separator + "resources" + File.separator
						+ getConfFileName()));

				PROTOCOL = params.getProperty("rdf_protocol").trim();
				HOST = params.getProperty("rdf_host").trim();
				PORT = params.getProperty("rdf_port").trim();
				SPARQL_ENDPOINT = params.getProperty("query_endpoint").trim();
				UPDATE_ENDPOINT = params.getProperty("update_endpoint").trim();
				USER = params.getProperty("rdf_username").trim();
				PWD = params.getProperty("rdf_password").trim();
				qHeaderTypes = (params.getProperty("query_headerTypes[]"))
						.split(";");
				qHeaderValues = (params.getProperty("query_headerValues[]"))
						.split(";");
				updHeaderTypes = (params.getProperty("update_headerTypes[]"))
						.split(";");
				updHeaderValues = (params.getProperty("update_headerValues[]"))
						.split(";");

				tsvHeaderValues = (params.getProperty("tsv_headerValues[]"))
						.split(";");
				csvHeaderValues = (params.getProperty("csv_headerValues[]"))
						.split(";");

				SPARQL_URL = PROTOCOL + HOST + ":" + PORT + SPARQL_ENDPOINT;
				UPDATE_URL = PROTOCOL + USER + ":" + PWD + "@" + HOST + ":"
						+ PORT + "/" + UPDATE_ENDPOINT;

				/*Properties paramsQ = new Properties();
				paramsQ.load(new FileInputStream(System.getProperty("user.dir")
						+ File.separator + "resources" + File.separator
						+ getConfFileName()));*/
				prefix_name = params.getProperty("prefix_name");

				// maps for queries
				labelMethodMap = jsonToMap(
						params.getProperty("label_method_list"), true);
				labelFilterNameMap = jsonToMap(
						params.getProperty("label_filtername_list"), true);
				filterPredicateNameMap = jsonToMap(
						params.getProperty("filter_pedicate_name_list"), true);
				optionalFilterSearchStrMap = jsonToMap(
						params.getProperty("optional_filter_search_string_list"),
						true);

			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
		return hasParams;
	}

	@Override
	public Map<String, String> getLabelMethodMap() {
		return labelMethodMap;
	}

	@Override
	public void updateProtocol(String statement) throws QueryException {
		String fullStatement = logTemplate.replace("<triples>", statement);
		try {
			update(fullStatement);
		} catch (QueryException e) {
			log.error("In RDFConnector updateProtocol:", e);
			throw e;
		}
	}

	/**
	 * For INSERT or DELETE SPARQL statements
	 * 
	 * @param statement
	 * @throws QueryException
	 */
	@Override
	public void update(String statement) throws QueryException {
		try {
			getPostResponse(RDFStorage.UPDATE_URL, updHeaderTypes,
					updHeaderValues, statement);

		} catch (QueryException e) {
			log.error("In RDFConnector update:", e);
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
				"PREFIX pgrr:<http://purl.org/pgrr/core#> ");
		sb.append("SELECT ?id FROM pgrr:subscription WHERE { ?id pgrr:email ");
		sb.append(literal(email));
		sb.append(" }");
		try {
			JSONObject res = getJSONResult(getSparqlURL(), sb.toString());
			JSONObject jsonRes = new JSONObject(res.getString("results"));
			JSONArray bindings = jsonRes.getJSONArray("bindings");
			String userID = null;
			if (bindings.length() == 0) {
				if (insert) {
					userID = RDFStorage.PREFIX_NAME
							+ UUID.randomUUID().toString();
					// insert new user
					String query = INSERT_EMAIL.replace("<userID>", userID);
					query = query.replace("<email>", literal(email));
					update(query);
				}

			} else {
				JSONObject jsonBin = new JSONObject(bindings.getString(0));
				userID = new JSONObject(jsonBin.getString("id"))
						.getString("value");
				userID = userID.replace("http://purl.org/pgrr/core#",
						RDFStorage.PREFIX_NAME);
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
	public void insertDeletePairs(String email, String[] disAbbr,
			String[] dataType, boolean doInsert) throws QueryException {
		try {
			String userID = getUserID(email, doInsert);
			if (userID != null) {
				String insertTempl = INSERT_DELETE_PAIRS.replaceAll("<userID>",
						userID);
				if (doInsert)
					insertTempl = insertTempl.replace("<INSERT_DELETE>",
							"INSERT IN");
				else
					insertTempl = insertTempl.replace("<INSERT_DELETE>",
							"DELETE FROM");

				String insertIml = null;
				for (int i = 0; i < disAbbr.length; i++) {
					// get pair
					String disDataTypeID = getDiseaseDataTypeID(disAbbr[i],
							dataType[i]);
					insertIml = insertTempl.replace("<pair>", disDataTypeID);
					update(insertIml);

				}
			}

		} catch (QueryException e) {
			log.error("In insertDeletePairs:", e);
			throw e;
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
		String query = GET_DIS_DATATYPE_ID.replace("<disAbbr>",
				literal(disAbbr));
		query = query.replace("<dataType>", literal(dataType));
		try {
			JSONObject res = getJSONResult(RDFStorage.SPARQL_URL, query);
			JSONObject jsonRes = new JSONObject(res.getString("results"));
			JSONArray bindings = jsonRes.getJSONArray("bindings");
			String id = null;

			if (bindings.length() == 0) {
				id = RDFStorage.PREFIX_NAME + UUID.randomUUID().toString();
				// create a new pair
				String insStr = INSERT_DIS_DATATYPE.replaceAll("<ID>", id);
				insStr = insStr.replace("<disAbbr>", literal(disAbbr));
				insStr = insStr.replace("<dataType>", literal(dataType));
				update(insStr);
			} else {
				JSONObject jsonBin = new JSONObject(bindings.getString(0));
				id = new JSONObject(jsonBin.getString("id")).getString("value");
				id = id.replace("http://purl.org/pgrr/core#",
						RDFStorage.PREFIX_NAME);
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

	@Override
	public String nameWithPrefixUUID(String prefix, String value) {
		if (!value.startsWith(prefix))
			return "<" + prefix + value + ">";
		else
			return "<" + value + ">";
	}

	/**
	 * 
	 * @param jsonStr
	 *            format:
	 *            {results:[{key:"Disease",value:["acc","blca","brca"]},{
	 *            key:"Level",value:["2","3"]}]};
	 * @return map of key, values
	 */
	@Override
	public Map<String, String> jsonToMap(String jsonStr,
			boolean removeDoulbeQuotes) {

		try {
			Map<String, String> map = new LinkedHashMap<String, String>();
			JSONArray res = (new JSONObject(jsonStr)).getJSONArray("results");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	@Override
	public String getSparqlURL() {
		return SPARQL_URL;
	}

	/**
	 * 
	 * @param hTypes
	 * @param hValues
	 * @param SPARQL_URL
	 * @param query
	 * @return result as JSONObject object
	 * @throws QueryException
	 */
	private JSONObject getJSONResult(String[] hTypes, String[] hValues,
			String SPARQL_URL, String query) throws QueryException {
		try {

			String respStr = getPostResponse(buildQueryURL(SPARQL_URL, query),
					qHeaderTypes, qHeaderValues, null);
			JSONObject jObj = new JSONObject(respStr);
			String jStr = jObj.toString();
			return new JSONObject(jStr);

		} catch (IOException e) {
			e.printStackTrace();
			log.error("In getJSONResult:", e);
			throw new QueryException("QueryException: in getJSONResult: " + e);
		} catch (JSONException e) {
			e.printStackTrace();
			log.error("In getJSONResult:", e);
			throw new QueryException("QueryException: in getJSONResult: " + e);
		}

	}

	/**
	 * Return http query result
	 * 
	 * @param qUrl
	 * @param hTypes
	 * @param hValues
	 * @param body
	 * @return String
	 * @throws QueryException
	 */
	private String getPostResponse(String qUrl, String[] hTypes,
			String[] hValues, String body) throws QueryException {
		HttpClient httpclient = getHttpClient();
		HttpPost httppost = new HttpPost(qUrl);
		for (int i = 0; i < hTypes.length; i++) {
			httppost.setHeader(hTypes[i], hValues[i]);
		}

		try {
			if (body != null)
				httppost.setEntity(new StringEntity(body));

			HttpResponse response = httpclient.execute(httppost);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK
					&& statusCode != HttpStatus.SC_CREATED)
				throw new QueryException(
						"QueryException: in getPostResponse: HTTP Status Code: "
								+ statusCode);

			if (statusCode == HttpStatus.SC_OK) {
				String toret = EntityUtils.toString(response.getEntity());

				return toret;
			}
		} catch (UnsupportedEncodingException e) {
			log.error("In getPostResponse:", e);
			throw new QueryException("QueryException: in getPostResponse: " + e);
		} catch (ClientProtocolException e) {
			log.error("In getPostResponse:", e);
			throw new QueryException("QueryException: in getPostResponse: " + e);
		} catch (IOException e) {
			log.error("In getPostResponse:", e);
			throw new QueryException("QueryException: in getPostResponse: " + e);
		} finally { // / this was the problem!!!
			httpclient.getConnectionManager().shutdown();
		}

		return null;
	}

	@Override
	public String getConfFileName() {
		return "jQueryVirt.conf";
	}

	@Override
	public void unsubscribeCompletely(String email) throws QueryException {
		try {
			String id = getUserID(email, false);
			if (id != null) {
				String q = UNSUBSCRIBE_ME.replace("<userID>", id);
				update(q);
			}
		} catch (QueryException e) {
			log.error("In unSubscribeCompletely:", e);
			throw e;
		}
	}

	/**
	 * if there is only one value => replace ?*** else use UNION
	 * 
	 * @param template
	 * @param jsonStr
	 * @return String
	 */
	@Override
	public String getQuerySearchString(String template, String jsonStr) {
		String q = template;
		String dateLabel = null;
		// "\""+dateFormat.format(new Date())+"\"";
		Map<String, String> requestMap = jsonToMap(jsonStr, false);
		String label = null, filterName = null, value = null, filterOpt = null, searchOpt = null;
		for (Map.Entry<String, String> entry : labelFilterNameMap.entrySet()) {
			label = entry.getKey();
			value = requestMap.get(label);

			filterName = entry.getValue();
			if (label.equalsIgnoreCase("Snapshot By Date")) {
				if (value != null)
					dateLabel = value;
			} else {
				if (!filterName.startsWith("<optional")) {
					if (value != null && !value.equalsIgnoreCase("\"ALL\""))
						q = q.replace(
								filterName,
								replaceByFilter(filterName,
										filterPredicateNameMap.get(filterName)
												.split(","), value));
					else
						q = q.replaceAll(filterName, "");
				} else {
					filterOpt = null;
					searchOpt = optionalFilterSearchStrMap.get(filterName);
					try {
						searchOpt = java.net.URLDecoder.decode(searchOpt,
								"UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (value != null && !value.equalsIgnoreCase("\"ALL\"")) {
						filterOpt = replaceByFilter(
								filterName,
								filterPredicateNameMap.get(filterName).split(
										","), value);

					}
					searchOpt = "OPTIONAL {" + searchOpt + "} ";
					if (filterOpt != null)
						searchOpt = searchOpt + filterOpt;

					q = q.replace(filterName, searchOpt);
				}

			}
		}
		String repl = "";
		if (dateLabel != null) {
			repl = OPTIONAL_DATE_FILTER.replaceAll("<dateTime>", dateLabel);
		}
		q = q.replaceAll("<OPTIONAL_DATE_FILTER>", repl);

		q = q.replaceAll("<prefix_name>", prefix_name);

		return q;

	}

	/**
	 * Replaces place holders in query string
	 * 
	 * @param filterName
	 * @param nameArr
	 * @param values
	 * @return String
	 */
	private String replaceByFilter(String filterName, String[] nameArr,
			String values) {

		String[] valArr = values.split("\",\"");

		StringBuilder filter = new StringBuilder();
		if (nameArr.length > 1) {
			String[] valPairs = values.split("\",\"");
			String[] temp = null;
			for (String val : valArr) {
				val = val.replaceAll("\"", "");
				temp = val.split(": ");

				filter.append("(?" + nameArr[0] + "=\"" + temp[0] + "\" && ?"
						+ nameArr[1] + "= \"" + temp[1] + "\") || ");
			}
		} else {

			for (String val : valArr) {
				val = val.replaceAll("\"", "");
				filter.append("?" + nameArr[0] + "=\"" + val + "\" || ");
			}
		}
		filter.setLength(filter.length() - 4);

		return "FILTER (" + filter + ")";
	}

	/**
	 * 
	 * @param diseaseAbbr
	 * @param date
	 *            format "yyyy-MM-dd"
	 * @param patients
	 *            format: use comma+space as a delimiter. Example: TCGA-A1-A0SB,
	 *            TCGA-A1-A0SD
	 * @return JSONObject
	 * @throws QueryException
	 */
	@Override
	public JSONObject metadataByDiseaseDateForPatientList(String diseaseAbbr,
			String date, String[] patients) throws QueryException {
		String pFilter = "";
		if (patients != null && patients.length > 0) {
			StringBuilder sb = new StringBuilder(" FILTER (");
			for (String s : patients)
				sb.append("?pBarcode=\"" + s + "\" || ");
			sb.append("?pBarcode=\"AAAAA\""); // workaround Virtuoso FILTER with
												// one entry
			sb.append(") ");
			pFilter = sb.toString();
		}
		String q = DataSelection.sample_list_data_Q.replace("<diseaseAbbr>",
				diseaseAbbr.toLowerCase());
		q = q.replace("<patientListFilter>", pFilter);
		date = date + "T23:59:59";
		q = q.replaceAll("<dateTime>", date);
		try {
			return getJSONResult(RDFStorage.SPARQL_URL, q);
		} catch (QueryException e) {
			throw e;
		}
	}

}
