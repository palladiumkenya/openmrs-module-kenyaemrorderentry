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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openmrs.*;
import org.openmrs.api.DiagnosisService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.ModuleConstants;
import org.openmrs.module.kenyaemrorderentry.task.PushLabRequestsTask;
import org.openmrs.module.kenyaemrorderentry.util.Utils;
import org.openmrs.module.reporting.common.Age;
import org.openmrs.util.PrivilegeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;


public class LimsSystemWebRequest {

	private static final Logger log = LoggerFactory.getLogger(PushLabRequestsTask.class);


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
		LabTestId = labsUtils.limsLabTestIdCodesConverter(order.getConcept());
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

			// If using Bearer Key
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

}


