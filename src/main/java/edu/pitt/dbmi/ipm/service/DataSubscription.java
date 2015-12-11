package edu.pitt.dbmi.ipm.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

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
 */

@Path("/data-subscription/")
public class DataSubscription
{
	private static Log log = LogFactory.getLog(DataSubscription.class);
	// load parameters
	//private static boolean hasConnectorParams = RDFConnector.initParameters();
	private static boolean hasQueryParams = DataSelection.initParameters();

	/**
	 * A sample URL to call this service is http://servername/ipm-ws/data-subscription/getSubscription?email=PittEmail
	 *  
	 * @param email The subscriber's Pitt's email.                  
	 * @return The list of values as specified by the query parameters in a JSON string with an HTML response code of 200.  If an error
	 *         occurs a 400 or 500 HTML response code will be returned along with a description of the error.
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Path("getSubscription")
	public Response getSubscription(@DefaultValue("") @QueryParam("email") String email) throws QueryException
	{
		String out = "";
		if(isEmpty(email)){
			return Response.status(400).type("text/plain").entity("No email provided.").build();
		}
		try
		{
			JSONObject result = Subscription.getSubscription(email);
			out = result.toString();
		}
		catch (QueryException e)
		{
			log.error("A query exception occurred.", e);
			return Response.status(500).type("text/plain").entity("A query exception occurred.  Please ask your system administrator to check the log files for further information.").build();
		}
		catch (Throwable t){
			log.error("A throwable occurred.", t);
			return Response.status(500).type("text/plain").entity("A throwable occurred.  Please ask your system administrator to check the log files for further information.").build();
		}
		return(Response.status(200).type("application/json").entity(out).build());
	}

	/**
	 * A sample URL to call this service is http://servername/ipm-ws/data-subscription/getAllDiseases

	 * @return  A list of all the diseases in json with an HTML response code of 200.
	 *          If an error occurs a 400 or 500 HTML response code will be returned along with a description of the error.
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Path("getAllDiseases")
	public Response getAllDiseases() throws QueryException{
		try {
			JSONObject obj = new JSONObject();
			obj = Subscription.getAllDiseases();
			String out = obj.toString();
			return(Response.status(200).type("application/json").entity(out).build());
		} catch (QueryException e) {
			log.error("A query exception occurred.", e);
			return Response.status(500).type("text/plain").entity("An access exception occurred. Please ask your system administrator to check the log files for further information.").build();
		}
		catch (Throwable t){
			log.error("A throwable occurred.", t);
			return Response.status(500).type("text/plain").entity("A throwable occurred.  Please ask your system administrator to check the log files for further information.").build();
		}		
	}
	
	
	/**
	 * A sample URL to call this service is http://servername/ipm-ws/data-subscription/getAllDatatypes
	 *
	 * @return  A list of all the data types. The response is returned as json with an HTML response code of 200.
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Path("getAllDatatypes")
	public Response getAllDatatypes () throws QueryException{		
		try{
			log.info("In get all datatypes");

			JSONObject obj = new JSONObject();
			obj = Subscription.getAllDatatypes();
			String out = obj.toString();
			return(Response.status(200).type("application/json").entity(out).build());
		} catch (QueryException e) {
			log.error("A query exception occurred.", e);
			return Response.status(500).type("text/plain").entity("An access exception occurred. Please ask your system administrator to check the log files for further information.").build();
		} catch (Throwable t){
			log.error("A throwable occurred.", t);
			return Response.status(500).type("text/plain").entity("A throwable occurred.  Please ask your system administrator to check the log files for further information.").build();
		}			
	}
	
	/**
	 * A sample URL to call this service is http://servername/ipm-ws/data-subscription/subscribe
	 * @param email The subscriber's Pitt's email.
	 * @param disease which disease to subscribe.
	 * @param datatype which datatype to subscribe.
	 * 
	 * @return  "Yes" if the subscription process successfully. The response is returned as plain text with an HTML response code of 200.
	 * For testing:
	 * curl -X POST -H "Content-Type: application/json" http://localhost:8080/ipm-ws/data-subscription/subscribe -d [200158CLASP,200079COHBR]
	 */
	
	/** POST /subscribe */
    @POST 
    @Path("subscribe")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response subscribe(String subscribe) throws Exception {
		String rVal = "No";   
		try {
//			subscribe = "{\"email\" : \"pgrr@pitt.edu\", \"disAbbr\" : [\"cesc\",\"gbm\",\"laml\",\"lihc\",\"lusc\",\"prad\",\"tgct\"], \"datatype\" : [\"CNV (SNP Array)\",\"Expression-miRNA\",\"CNV (Low Pass DNASeq)\",\"Clinical\",\"RNASeqV2\",\"CNV (CN Array)\",\"Somatic Mutations\"]}";
			
			JSONObject obj = new JSONObject(subscribe);		
			String email = obj.getString("email");
       		if(isEmpty(email)){
    			return Response.status(400).type("text/plain").entity("No email provided.").build();
    		}
			JSONArray disAbbr = (JSONArray) obj.get("disAbbr");
			JSONArray datatype = (JSONArray) obj.get("datatype");
			
			String[] disAbbr_array = new String[disAbbr.length()];	
			for (int i = 0; i < disAbbr.length(); i++) {
				disAbbr_array[i] = disAbbr.getString(i);
			}
			String[] datatype_array = new String[datatype.length()];
			for (int i = 0; i < datatype.length(); i++) {
				datatype_array[i] = datatype.getString(i);
			}
			Subscription.subscribe(email, disAbbr_array, datatype_array);    	 
    	    rVal = "Yes";  
       } catch (Exception e) {
    	   String errorMsg = e.getMessage();
           log.error(errorMsg, e);
           //return internalServerError(errorMsg);
       } catch (Throwable t){
			log.error("A throwable occurred.", t);
			return Response.status(500).type("text/plain").entity("A throwable occurred.  Please ask your system administrator to check the log files for further information.").build();
		}
       return(Response.status(200).type("text/plain").entity(rVal).build());
    }
	
    
    /**
	 * A sample URL to call this service is http://servername/ipm-ws/data-subscription/unsubscribe
	 * @param email The subscriber's Pitt's email.
	 * @param disease which disease to unsubscribe.
	 * @param datatype which datatype to unsubscribe.
	 * 
	 * @return  Yes" if the subscription process successfully. The response is returned as plain text with an HTML response code of 200.
	 */
    
	/** POST /unsubscribe */
    @POST 
    @Path("unsubscribe")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response unsubscribe(String unsubsribe) throws Exception {
		String rVal = "No";   
		try {
			//unsubsribe = "{\"email\" : \"jop55@pitt.edu\", \"disAbbr\" : [\"cesc\",\"gbm\",\"laml\",\"lihc\",\"lusc\",\"prad\",\"tgct\"], \"datatype\" : [\"CNV (SNP Array)\",\"Expression-miRNA\",\"CNV (Low Pass DNASeq)\",\"Clinical\",\"RNASeqV2\",\"CNV (CN Array)\",\"Somatic Mutations\"]}";
			
			JSONObject obj = new JSONObject(unsubsribe);			
			String email = obj.getString("email");
       		if(isEmpty(email)){
    			return Response.status(400).type("text/plain").entity("No email provided.").build();
    		}      		
			JSONArray disAbbr = (JSONArray) obj.get("disAbbr");
			JSONArray datatype = (JSONArray) obj.get("datatype");
			
			String[] disAbbr_array = new String[disAbbr.length()];	
			for (int i = 0; i < disAbbr.length(); i++) {
				disAbbr_array[i] = disAbbr.getString(i);
			}
			String[] datatype_array = new String[datatype.length()];
			for (int i = 0; i < datatype.length(); i++) {
				datatype_array[i] = datatype.getString(i);
			}
			Subscription.unsubscribe(email, disAbbr_array, datatype_array);    	 
    	    rVal = "Yes";  
       } catch (Exception e) {
    	   String errorMsg = e.getMessage();
           log.error(errorMsg, e);
           //return internalServerError(errorMsg);
       } catch (Throwable t){
			log.error("A throwable occurred.", t);
			return Response.status(500).type("text/plain").entity("A throwable occurred.  Please ask your system administrator to check the log files for further information.").build();
		}
       return(Response.status(200).type("text/plain").entity(rVal).build());
    }
    
    @POST
	@Path("unsubscribeAll")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
	public Response unsubscribeAll(String unsubsribe) throws Exception
	{
		String rVal = "No";	
		try{
			JSONObject obj = new JSONObject(unsubsribe);		
			String email = obj.getString("email");
       		if(isEmpty(email)){
    			return Response.status(400).type("text/plain").entity("No email provided.").build();
    		}    
			Subscription.unsubscribeCompletely(email);
			rVal = "Yes";
		} catch (Exception e) {
	    	   String errorMsg = e.getMessage();
	           log.error(errorMsg, e);
	    } catch (Throwable t){
			log.error("A throwable occurred.", t);
			return Response.status(500).type("text/plain").entity("A throwable occurred.  Please ask your system administrator to check the log files for further information.").build();
		}
		return(Response.status(200).type("text/plain").entity(rVal).build());
	}
    
    
	/**
	 * A sample URL to call this service is http://servername/ipm-ws/data-subscription/getCountPatientsByTSS
	 * Currently not in use

	 * @return  A list of all the diseases in json with an HTML response code of 200.
	 *          If an error occurs a 400 or 500 HTML response code will be returned along with a description of the error.
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Path("getCountPatientsByTSS")
	public Response getCountPatientsByTSS() throws QueryException{
		try {
			JSONObject obj = new JSONObject();
			JSONObject out = new JSONObject();
			
			// number of UPMC patients with Breast Cancer
			obj = EnterpriseAnalytics.countPatientsByTSS("brca", "University of Pittsburgh");			
			JSONArray  j = obj.getJSONObject("results").getJSONArray("bindings");
			JSONObject k = new JSONObject();
			for (int i = 0; i < j.length(); i++) {
				k = j.getJSONObject(i);
			}
			String value = k.getJSONObject("numPatients").getString("value");
			out.put("brca", value);
			
			// number of UPMC patients with Ovarian Cancer
			obj = EnterpriseAnalytics.countPatientsByTSS("ov", "University of Pittsburgh");
			j = obj.getJSONObject("results").getJSONArray("bindings");
			for (int i = 0; i < j.length(); i++) {
				k = j.getJSONObject(i);
			}
			value = k.getJSONObject("numPatients").getString("value");
			out.put("ov", value);
			
			// number of UPMC patients with head and neck cancer
			obj = EnterpriseAnalytics.countPatientsByTSS("hnsc", "University of Pittsburgh");
			j = obj.getJSONObject("results").getJSONArray("bindings");
			for (int i = 0; i < j.length(); i++) {
				k = j.getJSONObject(i);
			}
			value = k.getJSONObject("numPatients").getString("value");
			out.put("hnsc", value);
	
			return(Response.status(200).type("application/json").entity(out.toString()).build());
		} catch (QueryException e) {
			log.error("A query exception occurred.", e);
			return Response.status(500).type("text/plain").entity("An access exception occurred. Please ask your system administrator to check the log files for further information.").build();
		}
		catch (Throwable t){
			log.error("A throwable occurred.", t);
			return Response.status(500).type("text/plain").entity("A throwable occurred.  Please ask your system administrator to check the log files for further information.").build();
		}		
	}
    
	/**
	 * A sample URL to call this service is http://servername/ipm-ws/data-subscription/getAllDiseasesForSampleSlection
	 *
	 * @return  A list of all the diseases for search repository. The response is returned as json with an HTML response code of 200.
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Path("getAllDiseasesForSampleSlection")
	public Response getAllDiseasesForSampleSlection() throws QueryException{		
		try{
			  JSONObject results = DataSelection.diseaseList();						
			  JSONArray bindings = results.getJSONObject("results").getJSONArray("bindings");
			  JSONObject disease = new JSONObject();
			  String value = new String();
			  JSONArray diseaseAbbr = new JSONArray();
			  
			  for (int i = 0; i < bindings.length(); i++) {
				  disease = bindings.getJSONObject(i);
				  value = disease.getJSONObject("diseaseAbbr").getString("value");
				  diseaseAbbr.put(value);
			  }
			  String out = diseaseAbbr.toString();
			return(Response.status(200).type("application/json").entity(out).build());
		} catch (QueryException e) {
			log.error("A query exception occurred.", e);
			return Response.status(500).type("text/plain").entity("An access exception occurred. Please ask your system administrator to check the log files for further information.").build();
		} catch (Throwable t){
			log.error("A throwable occurred.", t);
			return Response.status(500).type("text/plain").entity("A throwable occurred.  Please ask your system administrator to check the log files for further information.").build();
		}			
	}
	
	/**
	 * A sample URL to call this service is http://servername/ipm-ws/data-subscription/getCurrMetadataByDiseaseForPatientList?disease=blca&patient=["TCGA-A6-2671","TCGA-A6-2672"]
	 *
	 * @return  A list of all the current sample metadata by disease. The response is returned as json with an HTML response code of 200.
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Path("getCurrMetadataByDiseaseForPatientList")
	public Response getCurrMetadataByDiseaseForPatientList(@DefaultValue("") @QueryParam("disease") String disease, @DefaultValue("") @QueryParam("patients") String patients) throws QueryException{		
		try{			 
			JSONArray  j = new JSONArray(patients);
			String[] patientlist_array = new String[j.length()];	
			for (int i = 0; i < j.length(); i++) {
				patientlist_array[i] = j.getString(i);
			}	
			JSONObject results = SampleSelection.currMetadataByDiseaseForPatientList(disease, patientlist_array);	
			  String out = results.toString();
			return(Response.status(200).type("application/json").entity(out).build());
		} catch (QueryException e) {
			log.error("A query exception occurred.", e);
			return Response.status(500).type("text/plain").entity("An access exception occurred. Please ask your system administrator to check the log files for further information.").build();
		} catch (Throwable t){
			log.error("A throwable occurred.", t);
			return Response.status(500).type("text/plain").entity("A throwable occurred.  Please ask your system administrator to check the log files for further information.").build();
		}			
	}
	
	/**
	 * A sample URL to call this service is http://servername/ipm-ws/data-subscription/getDatatypePlatformLevelByDisease?disease=blca
	 *
	 * @return  diseaseAbbr, dataType, center abbreviation, platform, data level, isPublic by disease. The response is returned as json with an HTML response code of 200.
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Path("getDatatypePlatformLevelByDisease")
	public Response getDatatypePlatformLevelByDisease(@DefaultValue("") @QueryParam("disease") String disease) throws QueryException{		
		try{
			  JSONObject results = SampleSelection.centerDatatypePlatformLevel(disease);	
			  String out = results.toString();
			return(Response.status(200).type("application/json").entity(out).build());
		} catch (QueryException e) {
			log.error("A query exception occurred.", e);
			return Response.status(500).type("text/plain").entity("An access exception occurred. Please ask your system administrator to check the log files for further information.").build();
		} catch (Throwable t){
			log.error("A throwable occurred.", t);
			return Response.status(500).type("text/plain").entity("A throwable occurred.  Please ask your system administrator to check the log files for further information.").build();
		}			
	}
	
	
	/**
	 * A sample URL to call this service is http://servername/ipm-ws/data-subscription/getPatientsByDisease?disease=blca
	 *
	 * @return  patient bar code by disease. The response is returned as json with an HTML response code of 200.
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Path("getPatientsByDisease")
	public Response getPatientsByDisease(@DefaultValue("") @QueryParam("disease") String disease) throws QueryException{		
		try{
			  JSONObject results = SampleSelection.patientsByDisease(disease);	
			  String out = results.toString();
			return(Response.status(200).type("application/json").entity(out).build());
		} catch (QueryException e) {
			log.error("A query exception occurred.", e);
			return Response.status(500).type("text/plain").entity("An access exception occurred. Please ask your system administrator to check the log files for further information.").build();
		} catch (Throwable t){
			log.error("A throwable occurred.", t);
			return Response.status(500).type("text/plain").entity("A throwable occurred.  Please ask your system administrator to check the log files for further information.").build();
		}			
	}
	
	/**
	 * A sample URL to call this service is http://servername/ipm-ws/data-subscription/getFilters
	 *
	 * @return all the filters. The response is returned as json with an HTML response code of 200.
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Path("getFilters")
	public Response getFilters() throws QueryException{		
		try{
			JSONObject results = DataSelection.getFilters();	
			String out = results.toString();
			
			return(Response.status(200).type("application/json").entity(out).build());
		} 
		catch (QueryException e) {
			log.error("A query exception occurred.", e);
			return Response.status(500).type("text/plain").entity("An access exception occurred. Please ask your system administrator to check the log files for further information.").build();
		} 
		catch (Throwable t){
			log.error("A throwable occurred.", t);
			return Response.status(500).type("text/plain").entity("A throwable occurred.  Please ask your system administrator to check the log files for further information.").build();
		}			
	}
	
	/**
	 * A sample URL to call this service is http://servername/ipm-ws/data-subscription/getSearchRepositoryResultTotal?jsonReq={results:[{key:"Disease",value:["acc","blca","brca"]},{key:"Snapshot By Date",value:["2014-03-21"]}]}
	 *
	 * @return the count of search repository result. The response is returned an integer/string with an HTML response code of 200.
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Path("getSearchRepositoryResultTotal")
	public Response getSearchRepositoryResultTotal(@DefaultValue("") @QueryParam("jsonReq") String jsonReq) throws QueryException{		
		try{
			if(isEmpty(jsonReq)){
				jsonReq = "{\"results\":[]}";
			}
			Integer results = DataSelection.getSearchRepositoryResultTotal(jsonReq);	
			String out = results.toString();
			return(Response.status(200).type("application/json").entity(out).build());
		} catch (QueryException e) {
			log.error("A query exception occurred.", e);
			return Response.status(500).type("text/plain").entity("An access exception occurred. Please ask your system administrator to check the log files for further information.").build();
		} catch (Throwable t){
			log.error("A throwable occurred.", t);
			return Response.status(500).type("text/plain").entity("A throwable occurred.  Please ask your system administrator to check the log files for further information.").build();
		}			
	}
	
	/**
	 * A sample URL to call this service is http://servername/ipm-ws/data-subscription/getSearchRepositoryResults?jsonReq={"results":[]}&stRecord=1&endRecord=50
	 *
	 * @return the count of search repository result. The response is returned an integer/string with an HTML response code of 200.
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Path("getSearchRepositoryResults")
	public Response getSearchRepositoryResults(@DefaultValue("") @QueryParam("jsonReq") String jsonReq, @DefaultValue("") @QueryParam("stRecord") String stRecord, @DefaultValue("") @QueryParam("endRecord") String endRecord) throws QueryException{		
		try{
			if(isEmpty(jsonReq)){
				jsonReq = "{\"results\":[]}";
			}
			if(isEmpty(stRecord)){
				return Response.status(400).type("text/plain").entity("No start record provided.").build();
			}
			if(isEmpty(endRecord)){
				return Response.status(400).type("text/plain").entity("No end record provided.").build();
			}
			
			JSONObject results = DataSelection.getSearchRepositoryResults(jsonReq, stRecord, endRecord);	
			String out = results.toString();
			return(Response.status(200).type("application/json").entity(out).build());
		} catch (QueryException e) {
			log.error("A query exception occurred.", e);
			return Response.status(500).type("text/plain").entity("An access exception occurred. Please ask your system administrator to check the log files for further information.").build();
		} catch (Throwable t){
			log.error("A throwable occurred.", t);
			return Response.status(500).type("text/plain").entity("A throwable occurred.  Please ask your system administrator to check the log files for further information.").build();
		}			
	}
	
	/**
	 * A sample URL to call this service is http://localhost:8080/ipm-ws/data-subscription/getDownloadFileInformation?jsonReq={"results":[{"key":"Disease","value":["acc"]}]}&extended=true&tableFormat=csv&email=pgrr@pitt.edu
	 *
	 * @return the count of search repository result. The response is returned an integer/string with an HTML response code of 200.
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Path("getDownloadFileInformation")
	public Response getDownloadFileInformation(@DefaultValue("") @QueryParam("jsonReq") String jsonReq, @DefaultValue("") @QueryParam("extended") String extended, @DefaultValue("") @QueryParam("tableFormat") String tableFormat, @DefaultValue("") @QueryParam("email") String email) throws QueryException{		
		try{
			if(isEmpty(jsonReq)){
				jsonReq = "{\"results\":[]}";
			}
			if(isEmpty(extended)){
				return Response.status(400).type("text/plain").entity("No extended value provided.").build();
			}
			if(isEmpty(tableFormat)){
				return Response.status(400).type("text/plain").entity("No tableFormat value provided.").build();
			}
			if(isEmpty(email)){
				return Response.status(400).type("text/plain").entity("No email provided.").build();
			}
			
			boolean meta = Boolean.parseBoolean(extended);
			Response r;
			if(meta == true){
				r = DataSelection.getFilePathsAllMeta(jsonReq, tableFormat, email);
			}else{
				r = DataSelection.getFilePathsMinMeta(jsonReq, tableFormat, email);
			}
			return(r);
		} catch (QueryException e) {
			log.error("A query exception occurred.", e);
			return Response.status(500).type("text/plain").entity("An access exception occurred. Please ask your system administrator to check the log files for further information.").build();
		} catch (Throwable t){
			log.error("A throwable occurred.", t);
			return Response.status(500).type("text/plain").entity("A throwable occurred.  Please ask your system administrator to check the log files for further information.").build();
		}			
	}
		
	private boolean isEmpty(String val)
	{
		return(val == null || val.trim().equals(""));
	}
	
}
	

