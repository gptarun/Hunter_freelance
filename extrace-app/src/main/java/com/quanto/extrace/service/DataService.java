package com.quanto.extrace.service;

import java.io.IOException;

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
	private GraphQLClientHandling client;
	
	public String createSession(String callbackUrl) {
		String fingerPrint=null;
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
		JsonObject variables = new JsonObject();
		variables.addProperty("url", callbackUrl);

		try {
			fingerPrint = client.execute(query, variables,(JsonObject o) -> { return o.get("figerprint").toString();});
		} catch (GraphQLException e) {
			logger.error("Cannot create the hunter session",e);
		} catch (IOException e) {
			logger.error("Cannot create the hunter session",e);
		}
		return fingerPrint;
	}
	
	public void getCustomerData() {
		
	}
	
}
