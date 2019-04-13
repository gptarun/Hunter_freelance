package com.quanto.extrace.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.quanto.extrace.GraphQLClientHandling;

import graphql.GraphQLException;

@Service
public class DataService {
	private static Logger logger = LoggerFactory.getLogger(DataService.class);
	
	@Autowired
	private GraphQLClientHandling graphQLClient;
	
	public Map<String,String> createSession(String callbackUrl) {
		Map<String, String> sessionValues = new HashMap<>();
		JsonObject variables = new JsonObject();
		String query = 
				"mutation CreateSession {" + 
				"  Hunter_CreateSession(" + 
				"    input: {" + 
				"      webhooks: {"+ 
				"        type: post,"+ 
				"        url: $url" + 
				"      }" + 
				"  }) {" + 
				"    sessionId" + 
				"    sessionUrl" + 
				"  }" + 
				"}";
		
		variables.addProperty("url", callbackUrl);

		try {
			sessionValues = graphQLClient.execute(query, variables,(JsonObject o) -> {
				Map<String, String> jsonMap = new HashMap<>();
				jsonMap.put("sessionId", o.get("sessionId").toString());
				jsonMap.put("sessionUrl", o.get("sessionUrl").toString());
			return jsonMap;});
		} catch (GraphQLException e) {
			logger.error("Cannot create the hunter session",e);
		} catch (IOException e) {
			logger.error("Cannot create the hunter session",e);
		}
		return sessionValues;
	}
	
	public void getCustomerData(String fingerPrint) {

	}
	
}
