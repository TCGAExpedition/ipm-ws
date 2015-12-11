package edu.pitt.dbmi.ipm.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 * 
 * @author opm1
 * @version 1
 * @since Dec 11, 2015
 *
 */

public class SampleSelection {

	private static Log log = LogFactory.getLog(SampleSelection.class);

	public static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");

	/**
	 * 
	 * @return currently available diseases
	 * @throws QueryException
	 */
	public static JSONObject currentDiseaseList() throws QueryException {
		try {
			return DataSelection.diseaseList();
		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * 
	 * @return patient barcodes for a patricular disease
	 * @throws QueryException
	 */
	public static JSONObject patientsByDisease(String diseaseAbbr)
			throws QueryException {
		try {
			String q = DataSelection.patient_by_disease_Q.replace(
					"<diseaseAbbr>", diseaseAbbr);
			return StorageFactory.getStorage().getJSONResult(
					RDFStorage.SPARQL_URL, q);
		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * For display center abbreviation and platform should be combined as
	 * <center abbreviation>+\n+<platform>
	 * 
	 * @return diseaseAbbr, dataType, center abbreviation, platform, data level,
	 *         isPublic
	 * @throws QueryException
	 */
	public static JSONObject centerDatatypePlatformLevel(String diseaseAbbr)
			throws QueryException {
		String q = DataSelection.code_to_level_Q.replace("<diseaseAbbr>",
				diseaseAbbr);
		try {
			return StorageFactory.getStorage().getJSONResult(
					RDFStorage.SPARQL_URL, q);
		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * 
	 * @param diseaseAbbr
	 * @param patients
	 *            format: use comma+space as a delimiter. Example: TCGA-A1-A0SB,
	 *            TCGA-A1-A0SD
	 * @return JSONObject
	 * @throws QueryException
	 */
	public static JSONObject currMetadataByDiseaseForPatientList(
			String diseaseAbbr, String[] patients) throws QueryException {
		try {
			return metadataByDiseaseDateForPatientList(diseaseAbbr,
					dateFormat.format(new Date()), patients);
		} catch (QueryException e) {
			throw e;
		}
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
	public static JSONObject metadataByDiseaseDateForPatientList(
			String diseaseAbbr, String date, String[] patients)
			throws QueryException {

		try {
			return StorageFactory.getStorage()
					.metadataByDiseaseDateForPatientList(diseaseAbbr, date,
							patients);

		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * ATTENTION: can return big dataset for some diseases (> 1000 samples and
	 * 42K rows for brca on Feb.13, 2014)
	 * 
	 * @param diseaseAbbr
	 *            - disease abbreviation
	 * @return list of sample's metadata: sample barcode, tissue source site
	 *         (TSS) abbreviation, TSS name, sample type code, sample type
	 *         description, data type, platform, level, and version
	 * @throws QueryException
	 */
	public static JSONObject currSampleMetadataByDisease(String diseaseAbbr)
			throws QueryException {
		try {
			return sampleMetadataByDiseaseDate(diseaseAbbr,
					dateFormat.format(new Date()));
		} catch (QueryException e) {
			throw e;
		}
	}

	/**
	 * Returns a snapshot for a data available for a particular date.
	 * 
	 * @param diseaseAbbr
	 * @param date
	 *            is in format: yyyy-MM-dd
	 * @return a subset of data that was created before or in the date specified
	 *         and not archived before this date.
	 * @throws QueryException
	 */
	public static JSONObject sampleMetadataByDiseaseDate(String diseaseAbbr,
			String date) throws QueryException {

		try {
			return metadataByDiseaseDateForPatientList(diseaseAbbr, date, null);
		} catch (QueryException e) {
			throw e;
		}
	}

}
