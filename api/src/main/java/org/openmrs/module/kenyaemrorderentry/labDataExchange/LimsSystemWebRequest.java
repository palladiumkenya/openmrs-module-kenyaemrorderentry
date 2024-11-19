/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrorderentry.labDataExchange;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openmrs.*;
import org.openmrs.api.DiagnosisService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.ModuleConstants;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.kenyaemrorderentry.task.ProcessViralLoadResults;
import org.openmrs.module.kenyaemrorderentry.task.PushLabRequestsTask;
import org.openmrs.module.kenyaemrorderentry.util.Utils;
import org.openmrs.module.reporting.common.Age;
import org.openmrs.util.PrivilegeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.openmrs.module.kenyaemrorderentry.labDataExchange.LabwareFacilityWideResultsMapper.readLabTestMappingConfiguration;


public class LimsSystemWebRequest {

	private static final Logger log = LoggerFactory.getLogger(PushLabRequestsTask.class);
	public static final String LAB_TEST_CODE_PROPERTY = "testCode";

	/**
	 * Generates the order payload used to post to Lims server	 *
	 *
	 * @param order
	 * @return
	 */
	public static JSONObject generateLIMSpostPayload(Order order) {
		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		JSONObject payload = new JSONObject();

		Patient patient = order.getPatient();
		String patientId = patient.getPatientId() != null ? patient.getPatientId().toString() : "";
		String address = null;
		String ward = null;
		String village = null;
		String landmark = null;
		String firstName = null;
		String middleName = null;
		String lastName = null;
		Person person = Context.getPersonService().getPerson(patient.getPatientId());
		//Address
		if (person.getPersonAddress() != null) {
			village = person.getPersonAddress().getCityVillage() != null ? person.getPersonAddress().getCityVillage() : "";
			landmark = person.getPersonAddress().getAddress2() != null ? person.getPersonAddress().getAddress2() : "";
			address = person.getPersonAddress().getAddress1() != null ? person.getPersonAddress().getAddress1() : "";
			ward = person.getPersonAddress().getAddress4() != null ? person.getPersonAddress().getAddress4() : "";

		}
		//Names
		PersonName personName = person.getPersonName();
		if (personName != null) {
			firstName = personName.getGivenName() != null ? personName.getGivenName() : "";
			middleName = personName.getMiddleName() != null ? personName.getMiddleName() : "";
			lastName = personName.getFamilyName() != null ? personName.getFamilyName() : "";
		}

		//Age and gender
		String dob = patient.getBirthdate() != null ? sd.format(patient.getBirthdate()) : null;
		Age age = new Age(patient.getBirthdate());
		String LabRequestId = null;
		String LabTestId = null;
		String gender = patient.getGender();
		//Test details
		String orderId = null;
		String testName = null;
		String dateRequestReceived = null;
		String RequestedByName = null;

		//Diagnosis
		String diagnosisName = null;
		//Gets final and preliminary diagnosis
		DiagnosisService diagnosisService = Context.getDiagnosisService();
		List<Diagnosis> allDiagnosis = diagnosisService.getDiagnosesByEncounter(order.getEncounter(), false, false);
		if (!allDiagnosis.isEmpty()) {
			for (Diagnosis diagnosisType : allDiagnosis) {
				if (diagnosisType.getCertainty().equals(ConditionVerificationStatus.CONFIRMED)) {
					diagnosisName = diagnosisType.getDiagnosis().getCoded().getName().getName();
				} else {
					diagnosisName = diagnosisType.getDiagnosis().getCoded().getName().getName();
				}
			}
		}
		//Order details
		dateRequestReceived = sd.format(order.getDateCreated());
		RequestedByName = order.getCreator().getGivenName() != null ? order.getCreator().getGivenName() : "";
		LabRequestId = order.getOrderId().toString();
		// Get mapping for the test concept
		ObjectNode mapping = readLabTestMappingConfiguration();
		if (mapping == null) {
			System.out.println("LIMS-EMR mapping configuration is missing or invalid!");
		}
		System.out.println("Starting concept mapping for orders ==>");
		System.out.println("Order Concept UUID for orders==> " + order.getConcept().getUuid());
		ObjectNode testConceptMapping = (ObjectNode) mapping.get(order.getConcept().getUuid());
		System.out.println("Concept map for orders ==>" + testConceptMapping);
		if (testConceptMapping == null) {
			System.out.println("Mapping for order with concept does not exists ==>" + order.getConcept().getId());
			// return null;
		}

		System.out.println("Mapping exists ==>");
		// assign LabTestId as test code from mapper
		LabTestId = testConceptMapping.get(LAB_TEST_CODE_PROPERTY).asText();
		System.out.println("TEST CODE ==> " + LabTestId);

		testName = order.getConcept().getName().getName();
		Context.removeProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);

		//Create LIMS order payload
		if (person.getId() != null) {
			payload.put("Address", address + "" + village + "" + landmark);
			payload.put("DateRequestReceived", dateRequestReceived);
			payload.put("Diagnosis", diagnosisName != null ? diagnosisName : "No Diagnosis");
			payload.put("IsUrgent", true);
			payload.put("LabRequestId", LabRequestId);
			//Test Object
			JSONObject testsObj = new JSONObject();
			JSONArray labTest = new JSONArray();
			testsObj.put("LabTestId", LabTestId);
			testsObj.put("LabTestName", testName);
			labTest.add(testsObj);
			payload.put("LabTests", labTest);

			payload.put("location", ward);
			payload.put("PatientAge", age);
			payload.put("PatientBedNumber", "");
			payload.put("PatientBirthDate", dob);
			payload.put("PatientFirstName", firstName);
			payload.put("PatientGender", gender != null ? labsUtils.formatGender(gender) : null);
			payload.put("PatientId", patientId);
			payload.put("PatientOtherName", middleName);
			payload.put("PatientPhone", patient.getAttribute("Telephone contact") != null ? patient.getAttribute("Telephone contact").getValue() : "");
			payload.put("PatientStage", "OUTPATIENT");
			payload.put("PatientSurname", lastName);
			payload.put("PatientWard", "");
			payload.put("ReceiptNumber", "");
			payload.put("RequestedByName", RequestedByName);

		}
		return payload;

	}

	public static boolean postLabOrderRequestToLims(String params) throws IOException {
		String serverUrl = "";
		String API_KEY = "";
		GlobalProperty gpLIMsServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LIMS_LAB_SERVER_REQUEST_URL);
		GlobalProperty gpLIMsApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LIMS_LAB_SERVER_API_TOKEN);
		serverUrl = gpLIMsServerPushUrl.getPropertyValue().trim();
		API_KEY = gpLIMsApiToken.getPropertyValue().trim();
		SSLConnectionSocketFactory sslsf = null;
		GlobalProperty gpSslVerification = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_SSL_VERIFICATION_ENABLED);

		if (gpSslVerification != null) {
			String sslVerificationEnabled = gpSslVerification.getPropertyValue();
			if (StringUtils.isNotBlank(sslVerificationEnabled)) {
				if (sslVerificationEnabled.equals("true")) {
					sslsf = Utils.sslConnectionSocketFactoryDefault();
				} else {
					sslsf = Utils.sslConnectionSocketFactoryWithDisabledSSLVerification();
				}
			}
		}

		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

		try {

			//Define a post request
			System.out.println("LIMs Lab Results POST: Server URL: " + serverUrl);
			HttpPost postRequest = new HttpPost(serverUrl);
			//Set the API media type in http content-type header
			postRequest.addHeader("content-type", "application/json");

			// If using api-key
			postRequest.setHeader("x-api-key", API_KEY);
			//Set the request post body
			String payload = params;
			System.out.println("LIMS Lab Request POST: Server Payload: " + payload);
			StringEntity userEntity = new StringEntity(payload);
			postRequest.setEntity(userEntity);
			HttpResponse response = httpClient.execute(postRequest);

			//verify the valid error code first
			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == 400) { // Missing required fields
				System.out.println("Missing required fields");
				log.warn("Missing required fields");
				return (false);
			} else if (statusCode == 401) { // Unauthorized Access
				System.out.println("Unauthorized Access");
				log.warn("Unauthorized Access");
				return (false);
			} else if (statusCode == 500) { // Couldn't connect to server
				System.out.println("Could not connect to server");
				log.warn("Could not connect to server");
				return (false);
			} else if (statusCode == 200) {
				System.out.println("LIMs Lab Request POST: Successfully pushed a lab test");
				log.info("LIMs Lab Request POST: Successfully pushed a lab test");

				Context.flushSession();
				System.out.println("Labware Lab Results POST: Push Successfull");
				return (true);
			}
		} catch (Exception e) {
			System.err.println("LIMs Lab Request POST: Could not push requests to the lab! " + e.getMessage());
			log.error("LIMs Lab Request POST: Could not push requests to the lab! " + e.getMessage());
			e.printStackTrace();
		} finally {
			httpClient.close();
		}

		return (false);
	}

	public static void pullFacilityWideLimsLabResult(List<Integer> orderIds) throws IOException {
		String serverUrl = "";
		String API_KEY = "";
		GlobalProperty gpLIMsServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LIMS_LAB_SERVER_RESULT_URL);
		GlobalProperty gpLIMsApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LIMS_LAB_SERVER_API_TOKEN);
		serverUrl = gpLIMsServerPushUrl.getPropertyValue().trim();
		API_KEY = gpLIMsApiToken.getPropertyValue().trim();
		SSLConnectionSocketFactory sslsf = null;
		GlobalProperty gpSslVerification = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_SSL_VERIFICATION_ENABLED);

		if (gpSslVerification != null) {
			String sslVerificationEnabled = gpSslVerification.getPropertyValue();
			if (StringUtils.isNotBlank(sslVerificationEnabled)) {
				if (sslVerificationEnabled.equals("true")) {
					sslsf = Utils.sslConnectionSocketFactoryDefault();
				} else {
					sslsf = Utils.sslConnectionSocketFactoryWithDisabledSSLVerification();
				}
			}
		}

		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

		for (Integer order : orderIds) {
			try {

				URIBuilder builder = new URIBuilder(serverUrl);
				String currentOrder = order.toString();

				builder.addParameter("LabRequestId", currentOrder);
				URI uri = builder.build();
				System.out.println("Get Lims Results URL: " + uri);

				HttpGet httpget = new HttpGet(uri);

				//Set the API media type in http content-type header
				// If using api-key
				httpget.addHeader("content-type", "application/json");
				httpget.addHeader("x-api-key", API_KEY);

				CloseableHttpResponse response = httpClient.execute(httpget);
				System.out.println("Get Lims Results GET Request: " + httpget);

				final int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200 || statusCode == 201) {
					System.out.println("Get Lims Results: REST Call Success");

					String jsonString = null;
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent()));

						try {
							jsonString = rd.lines().collect(Collectors.joining()).toString();
							System.out.println("Lims Lab Results Get: Request JSON -> " + jsonString);
						} finally {
							rd.close();
						}
					}
					JSONParser parser = new JSONParser();
					JSONObject responseObject = (JSONObject) parser.parse(jsonString);

					if (responseObject != null && !responseObject.isEmpty()) {
						// Update Lims results order
						LabwareFacilityWideResultsMapper.processResultsFromLims(jsonString);
					}
					System.out.println("Lims Results Get: Successfully executed the task that pulls lab requests");
					log.info("Lims Results Get: Successfully executed the task that pulls lab requests");
					System.out.println("Lims Results Get: Successfully Done");
				} else {
					System.err.println("Get Lims Lab Results Failed with HTTP error code : " + statusCode);
				}
			} catch (Exception e) {
				System.err.println("Get Lims Lab Results Error: " + e.getMessage());
				e.printStackTrace();
			}
			// Delay loop
			try {
				//Delay for 5 seconds
				Thread.sleep(5000);
			} catch (Exception ie) {
				Thread.currentThread().interrupt();
			}
		}
		// finally
		httpClient.close();
	}

}


