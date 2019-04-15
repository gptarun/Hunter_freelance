package com.quanto.extrace.service;

import java.io.IOException;
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

	public Map<String, String> createSession(String callbackUrl) {
		Map<String, String> sessionValues = new HashMap<>();
		JsonObject variables = new JsonObject();
		String query = "mutation CreateSession {\r\n" + "  Hunter_CreateSession(\r\n" + "    input: {\r\n"
				+ "      webhooks: {\r\n" + "        type: post,\r\n" + "        url: $url\r\n" + "      }\r\n"
				+ "  }) {\r\n" + "    sessionId\r\n" + "    sessionUrl\r\n" + "  }\r\n" + "}";

		variables.addProperty("url", callbackUrl);

		try {
			sessionValues = graphQLClient.execute(query, variables, (JsonObject o) -> {
				Map<String, String> jsonMap = new HashMap<>();
				jsonMap.put("sessionId", o.get("sessionId").toString());
				jsonMap.put("sessionUrl", o.get("sessionUrl").toString());
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

		try {
			JsonObject data = graphQLClient.execute(query, variables, (JsonObject o) -> {
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

		try {
			JsonObject data = graphQLClient.execute(query, variables, (JsonObject o) -> {
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
		GpgSign gpgSign = new GpgSign();
		Map keySignature = gpgSign.createSignature();
		/*
		 * Need to create a loginc to fetch the { "data": { "User_viewer": { "me": {
		 * "baseName": "Tarun Gupta" } } } }
		 * 
		 * base name and store it into the new MAP and
		 * Map should have 
		 * key - "baseName"
		 * value - "Tarun Gupta"
		 */
		Map reponseMap = null;

		return reponseMap;

	}

}
