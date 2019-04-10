package com.quanto.extrace.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quanto.extrace.GraphQLClientHandling;

@Service
public class DataService {

	@Autowired
	private GraphQLClientHandling client;
	
	public void createSession(String callbackUrl) {
		String query = 
				"mutation CreateSession {" + 
				"  Hunter_CreateSession(" + 
				"    input: {" + 
				"      webhooks: {"+ 
				"        type: post,"+ 
				"        url:" + callbackUrl + 
				"      }" + 
				"  }) {" + 
				"    sessionId" + 
				"    sessionUrl" + 
				"  }" + 
				"}";
		
		//client.execute(query, mapper); calling function
	}
	
	public void getCustomerData() {
		
	}
	
}
