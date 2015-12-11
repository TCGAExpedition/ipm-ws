package edu.pitt.dbmi.ipm.service;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

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
 * Selects data requested by a web server
 * 
 * 
 * @author opm1
 * @version 1
 * @since Dec 11, 2015
 *
 */

public class DataSelection {
	private static Log log = LogFactory.getLog(DataSelection.class);

	public static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");

	private static String prefix_name = null;

	// filter queries
	private static String disease_list_Q = null;
	private static String tss_list_Q = null;
	private static String sampleType_list_Q = null;
	private static String analyteType_list_Q = null;
	private static String center_list_Q = null;
	private static String analysisType_list_Q = null;
	private static String platform_list_Q = null;
	private static String level_list_Q = null;
	private static String genRef_list_Q = null;
	private static String genRefURL_list_Q = null;

	// data queries
	private static String availDisDataType_Q = null;
	private static String count_pgrrUUIDList_Q = null;
	private static String filter_metadata_Q = null;
	private static String max_metadata_Q = null;
	private static String min_metadata_Q = null;

	public static String sample_list_data_Q = null;
	public static String code_to_level_Q = null;
	public static String patient_by_disease_Q = null;

	// data for subscription
	public static String all_diseases_Q = null;
	public static String all_datatypes_Q = null;
	public static String get_subsc_Q = null;

	private static boolean hasParams = false;

	private static Map<String, String> protocolPredicateMap = null;

	/**
	 * 
	 * @return filters as JSONObject
	 * @throws QueryException
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject getFilters() throws QueryException {

		try {
			StringBuilder sb = new StringBuilder("{\"results\":[");

			Method m = null;
			@SuppressWarnings("rawtypes")
			Class c = DataSelection.class;
			Storage storage = StorageFactory.getStorage();
			Map<String, String> labelMethodMap = StorageFactory.getStorage()
					.getLabelMethodMap();
			for (Map.Entry<String, String> entry : labelMethodMap.entrySet()) {
				m = c.getMethod(entry.getValue());
				sb.append(storage.getOneValueJSONObject(
						(JSONObject) m.invoke(null), "results", "bindings",
						"value", entry.getKey()) + ",");
			}

			sb.setLength(sb.length() - 1);
			sb.append("]}");

			String jStr = sb.toString();

			return new JSONObject(jStr);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new QueryException(e.getMessage());
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new QueryException(e.getMessage());
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new QueryException(e.getMessage());
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new QueryException(e.getMessage());
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new QueryException(e.getMessage());
		}
		return null;
	}

	/**
	 * 
	 * @return diseases list
	 * @throws QueryException
	 */
	public static JSONObject diseaseList() throws QueryException {
		try {
			return StorageFactory.getStorage().getJSONResult(
					StorageFactory.getStorage().getSparqlURL(), disease_list_Q);
		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * 
	 * @return tissue source sites list
	 * @throws QueryException
	 */
	public static JSONObject tssList() throws QueryException {
		try {
			return StorageFactory.getStorage().getJSONResult(
					StorageFactory.getStorage().getSparqlURL(), tss_list_Q);
		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * 
	 * @return analysis centers list
	 * @throws QueryException
	 */
	public static JSONObject centerList() throws QueryException {
		try {
			return StorageFactory.getStorage().getJSONResult(
					StorageFactory.getStorage().getSparqlURL(), center_list_Q);
		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * 
	 * @return sample types list
	 * @throws QueryException
	 */
	public static JSONObject sampleTypeList() throws QueryException {
		try {
			return StorageFactory.getStorage().getJSONResult(
					StorageFactory.getStorage().getSparqlURL(),
					sampleType_list_Q);
		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * 
	 * @return analysis types (data type) list
	 * @throws QueryException
	 */
	public static JSONObject analysisTypeList() throws QueryException {
		try {
			return StorageFactory.getStorage().getJSONResult(
					StorageFactory.getStorage().getSparqlURL(),
					analysisType_list_Q);
		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * 
	 * @return analyte types list
	 * @throws QueryException
	 */
	public static JSONObject analyteTypeList() throws QueryException {
		try {
			return StorageFactory.getStorage().getJSONResult(
					StorageFactory.getStorage().getSparqlURL(),
					analyteType_list_Q);
		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * 
	 * @return analysis platforms list
	 * @throws QueryException
	 */
	public static JSONObject platformList() throws QueryException {
		try {
			return StorageFactory.getStorage()
					.getJSONResult(StorageFactory.getStorage().getSparqlURL(),
							platform_list_Q);

		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * 
	 * @return all possible data levels list
	 * @throws QueryException
	 */
	public static JSONObject levelList() throws QueryException {
		try {
			return StorageFactory.getStorage().getJSONResult(
					StorageFactory.getStorage().getSparqlURL(), level_list_Q);

		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * 
	 * @return genome references list
	 * @throws QueryException
	 */
	public static JSONObject genRefList() throws QueryException {
		try {
			return StorageFactory.getStorage().getJSONResult(
					StorageFactory.getStorage().getSparqlURL(), genRef_list_Q);

		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * 
	 * @return genome reference urls list
	 * @throws QueryException
	 */
	public static JSONObject genRefURLList() throws QueryException {
		try {
			return StorageFactory.getStorage().getJSONResult(
					StorageFactory.getStorage().getSparqlURL(),
					genRefURL_list_Q);

		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * 
	 * @return diseases list available in local repository
	 * @throws QueryException
	 */
	public static JSONObject availDiseaseDataTypeList() throws QueryException {
		try {
			return StorageFactory.getStorage().getJSONResult(
					StorageFactory.getStorage().getSparqlURL(),
					availDisDataType_Q);

		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * @return JSONObject for count current (not archived) pgrrUUIDs
	 * @throws QueryException
	 */
	public static JSONObject countPgrrUUIDList() throws QueryException {
		try {
			String date = dateFormat.format(new Date());
			String q = count_pgrrUUIDList_Q.replaceAll("<dateTime>", date);
			return StorageFactory.getStorage().getJSONResult(
					StorageFactory.getStorage().getSparqlURL(), q);

		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * 
	 * @param jsonStr
	 *            format:
	 *            {results:[{key:"Disease",value:["acc","blca","brca"]},{
	 *            key:"Snapshot By Date",value:["2014-03-21"]}]};
	 * @return int total number of records
	 */
	public static int getSearchRepositoryResultTotal(String jsonStr)
			throws QueryException {
		try {
			String q = StorageFactory.getStorage().getQuerySearchString(
					count_pgrrUUIDList_Q, jsonStr);
			// System.out.println("total Q: "+q);
			JSONObject res = StorageFactory.getStorage().getJSONResult(
					StorageFactory.getStorage().getSparqlURL(), q);
			String resStr = StorageFactory.getStorage().jsonResAsArray(res,
					"results", "bindings", "countRecords")[0].replaceAll("\"",
					"");

			return Integer.parseInt(resStr);

		} catch (QueryException e) {
			throw e;
		}

	}

	/**
	 * 
	 * @param jsonStr
	 * @param stRecord
	 *            starts with 1 (if stRecord == null) used to download the
	 *            detailed tsv file
	 * @param endRecord
	 * @return JSONObject for search repository based on filters
	 * @throws QueryException
	 */
	public static JSONObject getSearchRepositoryResults(String jsonStr,
			String stRecord, String endRecord) throws QueryException {

		try {
			String q = StorageFactory.getStorage().getQuerySearchString(
					filter_metadata_Q, jsonStr);
			if (stRecord != null) {
				int stInt = Integer.parseInt(stRecord) - 1;
				q = q + " OFFSET " + String.valueOf(stInt);
				q = q + " LIMIT "
						+ String.valueOf(Integer.parseInt(endRecord) - stInt);
			}
			JSONObject res = StorageFactory.getStorage().getJSONResult(
					StorageFactory.getStorage().getSparqlURL(), q);
			return res;

		} catch (QueryException e) {
			throw e;
		}

	}

	/**
	 * 
	 * @param jsonStr
	 * @param tableFormat
	 *            : "TSV" or "CSV"
	 * @return Response with max data
	 * @throws QueryException
	 */
	public static Response getFilePathsAllMeta(String jsonStr,
			String tableFormat, String email) throws QueryException {
		try {
			Protocol.logRequest(jsonStr, tableFormat, email, "max");
			String q = StorageFactory.getStorage().getQuerySearchString(
					max_metadata_Q, jsonStr);
			// System.out.println("MAX_RES_Q: "+q);
			Response r = StorageFactory.getStorage().getTextStream(
					StorageFactory.getStorage().getSparqlURL(), q, tableFormat);
			return r;

		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * 
	 * @param jsonStr
	 * @param tableFormat
	 *            : "TSV" or "CSV"
	 * @return Response with min data
	 * @throws QueryException
	 */
	public static Response getFilePathsMinMeta(String jsonStr,
			String tableFormat, String email) throws QueryException {
		try {
			Protocol.logRequest(jsonStr, tableFormat, email, "min");
			String q = StorageFactory.getStorage().getQuerySearchString(
					min_metadata_Q, jsonStr);
			// System.out.println("MIN_RES_Q: "+q);
			Response r = StorageFactory.getStorage().getTextStream(
					StorageFactory.getStorage().getSparqlURL(), q, tableFormat);
			return r;

		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * Initializes all required parameters
	 * 
	 * @return true if initialization was successful
	 */
	public static boolean initParameters() {
		if (hasParams)
			return hasParams;
		synchronized (DataSelection.class) {
			try {
				StorageFactory.getStorage().initParameters();
				hasParams = true;
				Properties params = TomcatHelper.getProperties(StorageFactory
						.getStorage().getConfFileName());

				// common
				prefix_name = params.getProperty("prefix_name");
				String prefix = params.getProperty("prefix");
				String storage_path = params.getProperty("storage_path");

				// filters
				disease_list_Q = queryInitReplace(
						params.getProperty("disease_list"), prefix_name, prefix);
				tss_list_Q = queryInitReplace(params.getProperty("tss_list"),
						prefix_name, prefix);
				center_list_Q = queryInitReplace(
						params.getProperty("center_list"), prefix_name, prefix);
				sampleType_list_Q = queryInitReplace(
						params.getProperty("sampleType_list"), prefix_name,
						prefix);
				analyteType_list_Q = queryInitReplace(
						params.getProperty("analyteType_list"), prefix_name,
						prefix);
				analysisType_list_Q = queryInitReplace(
						params.getProperty("analysisType_list"), prefix_name,
						prefix);
				platform_list_Q = queryInitReplace(
						params.getProperty("platform_list"), prefix_name,
						prefix);
				level_list_Q = queryInitReplace(
						params.getProperty("level_list"), prefix_name, prefix);
				genRef_list_Q = queryInitReplace(
						params.getProperty("genRef_list"), prefix_name, prefix);
				genRefURL_list_Q = queryInitReplace(
						params.getProperty("genRefURL_list"), prefix_name,
						prefix);

				// data
				availDisDataType_Q = queryInitReplace(
						params.getProperty("availDisDataType_list"),
						prefix_name, prefix);
				count_pgrrUUIDList_Q = queryInitReplace(
						params.getProperty("count_pgrrUUIDList"), prefix_name,
						prefix);
				filter_metadata_Q = queryInitReplace(
						params.getProperty("paged_metadata"), prefix_name,
						prefix);
				filter_metadata_Q = filter_metadata_Q.replaceAll(
						"<storage_path>", storage_path);

				max_metadata_Q = queryInitReplace(
						params.getProperty("max_metadata"), prefix_name, prefix);
				max_metadata_Q = max_metadata_Q.replaceAll("<storage_path>",
						storage_path);

				min_metadata_Q = queryInitReplace(
						params.getProperty("min_metadata"), prefix_name, prefix);
				min_metadata_Q = min_metadata_Q.replaceAll("<storage_path>",
						storage_path);

				sample_list_data_Q = queryInitReplace(
						params.getProperty("sample_list_data"), prefix_name,
						prefix);
				code_to_level_Q = queryInitReplace(
						params.getProperty("code_to_level"), prefix_name,
						prefix);
				patient_by_disease_Q = queryInitReplace(
						params.getProperty("patient_by_disease"), prefix_name,
						prefix);

				// data for subscription
				all_diseases_Q = queryInitReplace(
						params.getProperty("all_diseases"), prefix_name, prefix);
				all_datatypes_Q = queryInitReplace(
						params.getProperty("all_datatypes"), prefix_name,
						prefix);
				get_subsc_Q = queryInitReplace(params.getProperty("get_subsc"),
						prefix_name, prefix);

				// for protocol
				protocolPredicateMap = StorageFactory.getStorage().jsonToMap(
						params.getProperty("protocol_predicate_list"), true);

			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
		return hasParams;
	}

	/**
	 * Initializes all required parameters for local (no web server) testing
	 * 
	 * @return true if initialization was successful
	 */
	public static boolean initParametersLocal() {
		if (hasParams)
			return hasParams;
		try {
			StorageFactory.getStorage().initParametersLocal();
			hasParams = true;
			Properties params = new Properties();
			params.load(new FileInputStream(System.getProperty("user.dir")
					+ File.separator + "resources" + File.separator
					+ StorageFactory.getStorage().getConfFileName()));

			// common
			prefix_name = params.getProperty("prefix_name");
			String prefix = params.getProperty("prefix");
			String storage_path = params.getProperty("storage_path");

			// filters
			disease_list_Q = queryInitReplace(
					params.getProperty("disease_list"), prefix_name, prefix);
			tss_list_Q = queryInitReplace(params.getProperty("tss_list"),
					prefix_name, prefix);
			center_list_Q = queryInitReplace(params.getProperty("center_list"),
					prefix_name, prefix);
			sampleType_list_Q = queryInitReplace(
					params.getProperty("sampleType_list"), prefix_name, prefix);
			analyteType_list_Q = queryInitReplace(
					params.getProperty("analyteType_list"), prefix_name, prefix);
			analysisType_list_Q = queryInitReplace(
					params.getProperty("analysisType_list"), prefix_name,
					prefix);
			platform_list_Q = queryInitReplace(
					params.getProperty("platform_list"), prefix_name, prefix);
			level_list_Q = queryInitReplace(params.getProperty("level_list"),
					prefix_name, prefix);
			genRef_list_Q = queryInitReplace(params.getProperty("genRef_list"),
					prefix_name, prefix);
			genRefURL_list_Q = queryInitReplace(
					params.getProperty("genRefURL_list"), prefix_name, prefix);

			// data
			availDisDataType_Q = queryInitReplace(
					params.getProperty("availDisDataType_list"), prefix_name,
					prefix);
			count_pgrrUUIDList_Q = queryInitReplace(
					params.getProperty("count_pgrrUUIDList"), prefix_name,
					prefix);
			filter_metadata_Q = queryInitReplace(
					params.getProperty("paged_metadata"), prefix_name, prefix);
			filter_metadata_Q = filter_metadata_Q.replaceAll("<storage_path>",
					storage_path);

			max_metadata_Q = queryInitReplace(
					params.getProperty("max_metadata"), prefix_name, prefix);
			max_metadata_Q = max_metadata_Q.replaceAll("<storage_path>",
					storage_path);

			min_metadata_Q = queryInitReplace(
					params.getProperty("min_metadata"), prefix_name, prefix);
			min_metadata_Q = min_metadata_Q.replaceAll("<storage_path>",
					storage_path);

			sample_list_data_Q = queryInitReplace(
					params.getProperty("sample_list_data"), prefix_name, prefix);
			code_to_level_Q = queryInitReplace(
					params.getProperty("code_to_level"), prefix_name, prefix);
			patient_by_disease_Q = queryInitReplace(
					params.getProperty("patient_by_disease"), prefix_name,
					prefix);

			// data for subscription
			all_diseases_Q = queryInitReplace(
					params.getProperty("all_diseases"), prefix_name, prefix);
			all_datatypes_Q = queryInitReplace(
					params.getProperty("all_datatypes"), prefix_name, prefix);
			get_subsc_Q = queryInitReplace(params.getProperty("get_subsc"),
					prefix_name, prefix);

			// for protocol
			protocolPredicateMap = StorageFactory.getStorage().jsonToMap(
					params.getProperty("protocol_predicate_list"), true);

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return hasParams;
	}

	/**
	 * Replaces all occurrences of prefix and prefix_name. <b>NOTE:<b> leave
	 * this parameters blank if use the relation db
	 * 
	 * @param q
	 * @param prefix_name
	 * @param prefix
	 * @return String
	 */
	public static String queryInitReplace(String q, String prefix_name,
			String prefix) {
		q = q.replaceAll("<prefix_name>", prefix_name);
		return q.replaceAll("<prefix>", prefix);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		StorageFactory.getStorage().initParametersLocal();
		initParametersLocal();

		long stTime = System.currentTimeMillis();
		try {
			// System.out.println("diseaseList: "+diseaseList());
			// System.out.println("tssList: "+tssList());
			// System.out.println("centerList: "+centerList());
			// System.out.println("sampleTypeList: "+sampleTypeList());
			// System.out.println("analyteTypeList: "+analyteTypeList());
			// System.out.println("dataFileTypeList: "+dataFileTypeList());
			// System.out.println("platformList: "+platformList());
			// System.out.println("levelList: "+levelList());
			// System.out.println("genRefList: "+genRefList());
			// System.out.println("genRefURLList: "+genRefURLList());
			// System.out.println("getFilters: "+getFilters());

			String jsonReq = "{\"results\":[{\"key\":\"Disease\",\"value\":[\"acc\"]},{\"key\":\"Snapshot By Date\",\"value\":[\"2015-04-01\"]}]}";
			System.out.println("results: : "
					+ getSearchRepositoryResults(jsonReq, "1", "1"));

		} catch (QueryException e) {
			e.printStackTrace();
		}

		long endTime = System.currentTimeMillis();

		System.out.println("DONE in " + (endTime - stTime) / 1000.00 + " sec");
	}

}
