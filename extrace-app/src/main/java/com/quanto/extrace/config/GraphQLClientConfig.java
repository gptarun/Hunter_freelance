package com.quanto.extrace.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.quanto.extrace.GraphQLClientHandling;

@Configuration
public class GraphQLClientConfig {
	
	private static Logger logger = LoggerFactory.getLogger(GraphQLClientConfig.class);
	
	@Bean
	@Qualifier("graphQLClient")
	public GraphQLClientHandling graphQLClientConfiguration() { 
		URL callHunterURL=null;
		try {
			callHunterURL = new URL("https://api.staging.contaquanto.com/graphql");
		} catch (MalformedURLException e) {
			logger.error("your call back URL is invalid",e);
		}
		Map<String, String> inputHeaders = new HashMap<>();
		inputHeaders.put("Content-Type", "application/graphql");
		inputHeaders.put("signature","27E1F7EC3119CE6D_SHA512_");
		return new GraphQLClientHandling(callHunterURL,inputHeaders);
	}
	
}
