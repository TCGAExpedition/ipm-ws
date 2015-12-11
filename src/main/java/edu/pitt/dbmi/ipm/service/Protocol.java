package edu.pitt.dbmi.ipm.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * Saves information about metadata files downloaded by user with all the
 * user-defined filters
 * 
 * @author opm1
 * @version 1
 * @since Dec 8, 2015
 * 
 */
public class Protocol {
	private static Log log = LogFactory.getLog(Protocol.class);

	private static String TIMESTAMP_SS_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static SimpleDateFormat dtWithSeconds = null;
	private static SimpleDateFormat dtDateOnly = new SimpleDateFormat(
			"yyyy-MM-dd");
	private static final String PREFIX = "http://purl.org/pgrr/core#";
	public static final String LINE_BREAK = " .\n";
	public static final String SEP = "\t";

	private static final String recordType = "live";

	/**
	 * TODO: change recortType to "live" when move to production Inserts the
	 * download request into <http://purl.org/pgrr/core#"protocol> graph
	 * 
	 * @param jsonStr
	 *            -
	 * @param tableFormat
	 *            - cvs or tsv
	 * @param email
	 * @param numberOfFields
	 *            - number of fields to be downloaded: "max" or "min"
	 */
	public static void logRequest(String jsonStr, String tableFormat,
			String email, String numberOfFields) throws QueryException {
		if (email == null || email.equals(""))
			return;

		Storage storage = StorageFactory.getStorage();

		String uuid = storage.nameWithPrefixUUID(PREFIX, UUID.randomUUID()
				.toString());
		StringBuilder sb = new StringBuilder();
		sb.append(uuid + SEP + storage.nameWithPrefixPorG(PREFIX, "recordType")
				+ SEP + storage.literal(recordType) + LINE_BREAK);
		sb.append(uuid + SEP
				+ storage.nameWithPrefixPorG(PREFIX, "requesttime") + SEP
				+ storage.formatTimeInStorage(formatNow()) + LINE_BREAK);
		sb.append(uuid + SEP + storage.nameWithPrefixPorG(PREFIX, "email")
				+ SEP + storage.literal(email) + LINE_BREAK);
		sb.append(uuid + SEP + storage.nameWithPrefixPorG(PREFIX, "format")
				+ SEP + storage.literal(tableFormat) + LINE_BREAK);
		sb.append(uuid + SEP
				+ storage.nameWithPrefixPorG(PREFIX, "numberoffields") + SEP
				+ storage.literal(numberOfFields) + LINE_BREAK); // min or all

		storage.updateProtocol(sb.toString());

		if (jsonStr.startsWith("results"))
			jsonStr = "{" + jsonStr + "}";

		Map<String, String> map = storage.jsonToMap(jsonStr, false); // no need
																		// to
																		// remove
																		// double
																		// quotes
		String[] split = null;
		String key = null, val = null;
		for (Entry<String, String> entry : map.entrySet()) {
			key = entry.getKey().replaceAll(" ", "").toLowerCase();
			val = entry.getValue().replaceAll("\"", "");
			if (key.equals("snapshotbydate")) {
				storage.updateProtocol(uuid + SEP
						+ storage.nameWithPrefixPorG(PREFIX, "snapshotbydate")
						+ SEP + storage.formatTimeInStorage(val) + LINE_BREAK);
			} else {
				split = val.split(",");
				for (String s : split)
					storage.updateProtocol(uuid + SEP
							+ storage.nameWithPrefixPorG(PREFIX, key) + SEP
							+ storage.literal(s) + LINE_BREAK);
			}

		}

	}

	/**
	 * 
	 * @return formatted String without ending "Z" for database
	 */
	private static String formatNowNoZ() {
		if (dtWithSeconds == null) {
			dtWithSeconds = new SimpleDateFormat(TIMESTAMP_SS_FORMAT);
			dtWithSeconds.setTimeZone(TimeZone.getTimeZone("UTC"));
		}

		return dtWithSeconds.format(new Date()).replace(" ", "T");
	}

	/**
	 * 
	 * @return formatted String with ending "Z" for virtuoso
	 */
	private static String formatNow() {
		return formatNowNoZ() + "Z";
	}

	public static void main(String[] args) {
		String jsonStr = "{results:["
				+ "{key:\"Disease\",value:[\"acc\",\"blca\",\"brca\"]},"
				// +"{key:\"Sample Type\",value:[\"Primary solid Tumor\",\"Metastatic\"]},"
				+ "{key:\"Analysis Type\",value:[\"Clinical\",\"Protected_Mutations\"]},"
				/*
				 * +
				 * "{key:\"Tissue Source Site\",value:[\"University of Pittsburgh\"]},"
				 */
				+ "{key:\"Snapshot By Date\",value:[\"2014-03-21\"]}" + "]}";
		try {
			logRequest(jsonStr, "csv", "opm1@pitt.edu", "all");
		} catch (QueryException e) {
			e.printStackTrace();
		}
	}

}
