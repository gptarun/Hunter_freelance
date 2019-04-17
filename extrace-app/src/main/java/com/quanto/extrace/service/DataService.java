package com.quanto.extrace.service;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.quanto.extrace.GpgSign;
import com.quanto.extrace.GraphQLClientHandling;
import com.quanto.extrace.model.Customer;

import graphql.GraphQLException;

@Service
public class DataService {
	private static Logger logger = LoggerFactory.getLogger(DataService.class);

	@Autowired
	private GraphQLClientHandling graphQLClient;

	@Autowired
	private Customer customer;

	@Autowired
	private GpgSign gpgSign;

	public Map<String, String> createSession(String callbackUrl) {
		Map<String, String> sessionValues = new HashMap<>();
		JsonObject variables = new JsonObject();
		String query = "mutation CreateSession {  Hunter_CreateSession(     input: {"
				+ "      webhooks: {       type: post,        url: \"" + callbackUrl + "\"      }" + "  }) {"
				+ "    sessionId" + "    sessionUrl" + "  }" + " }";

		// variables.addProperty("$callbackUrl", callbackUrl);

		JsonObject body = createBody("CreateSession", query, variables, "extraceCreateSession");
		System.out.println(body);
		headerUtil(body.toString());

		try {
			sessionValues = graphQLClient.execute(body, (JsonObject o) -> {
				Map<String, String> jsonMap = new HashMap<>();
				System.out.println(o.toString());
				jsonMap.put("sessionId", o.getAsJsonObject("Hunter_CreateSession").get("sessionId").toString());
				jsonMap.put("sessionUrl", o.getAsJsonObject("Hunter_CreateSession").get("sessionUrl").toString().replaceAll("^\"", "").replaceAll("\"$", ""));
				return jsonMap;
			});
		} catch (GraphQLException e) {
			logger.error("Cannot create the hunter session", e);
		} catch (IOException e) {
			logger.error("Cannot create the hunter session", e);
		}
		return sessionValues;
	}

	public void getCustomerData(String fingerPrint) {
		JsonObject variables = new JsonObject();
		String query = "query User {\r\n" + "  User_viewer {\r\n" + "    user(fingerPrint: $fingerPrint) {\r\n"
				+ "      accounts {\r\n" + "        routingType\r\n" + "        routingNumber\r\n"
				+ "        branchNumber\r\n" + "        accountNumber\r\n" + "      }\r\n" + "    }\r\n" + "  }\r\n"
				+ "}";

		variables.addProperty("fingerPrint", fingerPrint);

		JsonObject body = createBody("User", query, variables, "1234");

		headerUtil(body.toString());

		try {
			JsonObject data = graphQLClient.execute(body, (JsonObject o) -> {
				return o;
			});
			customer.setRoutingType(data.get("sessionUrl").toString());
			customer.setRoutingNumber(data.get("sessionUrl").toString());
			customer.setBranchNumber(data.get("sessionUrl").toString());
			customer.setRoutingType(data.get("sessionUrl").toString());
		} catch (GraphQLException e) {
			logger.error("Cannot create the hunter session", e);
		} catch (IOException e) {
			logger.error("Cannot create the hunter session", e);
		}

	}

	public void getCustomerStatement(String fingerPrint) {
		JsonObject variables = new JsonObject();
		String query = "query GetAccountStatement {\r\n" + "  Bank_GetAccountStatement(\r\n"
				+ "    routingType: $routingType, \r\n" + "    routingNumber: $routingNumber,\r\n"
				+ "    branchNumber: $branchNumber,\r\n" + "    accountNumber: $accountNumber\r\n" + "  ) {\r\n"
				+ "    isoDateTime\r\n" + "    timestamp\r\n" + "    type\r\n" + "    name\r\n"
				+ "    documentNumber\r\n" + "    documentAmount\r\n" + "  }\r\n" + "}";

		variables.addProperty("routingType", customer.getRoutingType());
		variables.addProperty("routingNumber", customer.getRoutingNumber());
		variables.addProperty("branchNumber", customer.getBranchNumber());
		variables.addProperty("accountNumber", customer.getAccountNumber());

		JsonObject body = createBody("GetAccountStatement", query, variables, "1234");

		headerUtil(body.toString());

		try {
			JsonObject data = graphQLClient.execute(body, (JsonObject o) -> {
				return o;
			});
			// customer.setRoutingType(data.get("sessionUrl").toString());
			// customer.setRoutingNumber(data.get("sessionUrl").toString());
			// customer.setBranchNumber(data.get("sessionUrl").toString());
			// customer.setRoutingType(data.get("sessionUrl").toString());
		} catch (GraphQLException e) {
			logger.error("Cannot create the hunter session", e);
		} catch (IOException e) {
			logger.error("Cannot create the hunter session", e);
		}

	}

	public Map queryMe() {
		Map reponseMap = new HashMap();
		/*
		 * String query =
		 * "{\"query\":\"query Me {  User_viewer {    me {        baseName    }   }}\",\"operationName\":\"Me\",\"_timestamp\":"
		 * + ZonedDateTime.now().toInstant().toEpochMilli() +
		 * ",\"_timeUniqueId\":\"myAmazingUniqueId\"}";
		 */
		String query = "query Me {\r\n" + "  User_viewer {\r\n" + "    me {\r\n" + "      baseName\r\n" + "    }\r\n"
				+ "  }\r\n" + "}";

		JsonObject body = createBody("Me", query, "queryMeTest");
		headerUtil(body.toString());

		try {
			JsonObject data = graphQLClient.execute(body, (JsonObject o) -> {
				return o;
			});
			System.out.println(data.toString());
			String baseName = data.getAsJsonObject("User_viewer").getAsJsonObject("me").get("baseName").toString();
			reponseMap.put("baseName", baseName);
			System.out.println(baseName.toString());
		} catch (Exception e) {
			reponseMap.put("Error", "Some exception");
			e.printStackTrace();
		}
		/*
		 * Need to create a loginc to fetch the { "data": { "User_viewer": { "me": {
		 * "baseName": "Tarun Gupta" } } } }
		 * 
		 * base name and store it into the new MAP and Map should have key - "baseName"
		 * value - "Tarun Gupta"
		 */

		return reponseMap;

	}

	private void headerUtil(String headerFormat) {
		String header = "";
		Map<String, String> keySignature = gpgSign.createSignature(headerFormat);
		header = keySignature.get("fingerPrint") + "_" + keySignature.get("hashingAlgo") + "_"
				+ keySignature.get("asciiArmoredSignature");
		System.out.println(header);
		graphQLClient.updateHeader("signature", header);
	}

	private JsonObject createBody(String operationName, String query, String uniqueId) {
		return createBody(operationName, query, new JsonObject(), uniqueId);
	}

	private JsonObject createBody(String operationName, String query, JsonObject variables, String uniqueId) {
		JsonObject body = new JsonObject();
		body.addProperty("operationName", operationName);
		body.addProperty("query", query);
		body.add("variables", variables);
		body.addProperty("_timestamp", ZonedDateTime.now().toInstant().toEpochMilli());
		// body.addProperty("_timestamp", new Date().getTime());
		body.addProperty("_timeUniqueId", uniqueId + "" + ZonedDateTime.now().toInstant().toEpochMilli());
		return body;
	}

}
