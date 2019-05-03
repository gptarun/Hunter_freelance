package com.quanto.extrace.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
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

	@Value("${output.data.dir}")
	private String outputDirectory;

	public Map<String, String> createSession(String callbackUrl) {
		Map<String, String> sessionValues = new HashMap<>();
		JsonObject variables = new JsonObject();
		String query = "mutation CreateSession {  Hunter_CreateSession(     input: {"
				+ "      webhooks: {       type: post,        url: \"" + callbackUrl + "\"      }" + "  }) {"
				+ "    sessionId" + "    sessionUrl" + "  }" + " }";

		// variables.addProperty("$callbackUrl", callbackUrl);

		JsonObject body = createBody("CreateSession", query, variables, "extraceCreateSession");
		logger.info("Create Session query :- [{}]",body);
		headerUtil(body.toString());

		try {
			sessionValues = graphQLClient.execute(body, (JsonObject o) -> {
				Map<String, String> jsonMap = new HashMap<>();
				System.out.println(o.toString());
				jsonMap.put("sessionId", o.getAsJsonObject("Hunter_CreateSession").get("sessionId").toString());
				jsonMap.put("sessionUrl", o.getAsJsonObject("Hunter_CreateSession").get("sessionUrl").toString()
						.replaceAll("^\"", "").replaceAll("\"$", ""));
				return jsonMap;
			});
		} catch (GraphQLException e) {
			logger.error("Cannot create the hunter session", e);
		} catch (IOException e) {
			logger.error("Cannot create the hunter session", e);
		}
		return sessionValues;
	}

	public void setCustomerData(String fingerPrint) {
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

	public JsonObject getCustomerStatement(String fingerPrint) {
		JsonObject variables = new JsonObject();
		JsonObject data = null;
		setCustomerData(fingerPrint);

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
			data = graphQLClient.execute(body, (JsonObject o) -> {
				return o;
			});
		} catch (GraphQLException | IOException e) {
			throw new RuntimeException("cannot create hunter session due to :- ", e);
		} 
		return data;
	}

	public Map<String, Object> queryMe() {
		Map<String, Object> reponseMap = new HashMap<>();
		String query = "query Me {\r\n" + "  User_viewer {\r\n" + "    me {\r\n" + "      baseName\r\n" + "    }\r\n"
				+ "  }\r\n" + "}";

		JsonObject body = createBody("Me", query, "queryMeTest");
		headerUtil(body.toString());

		try {
			JsonObject data = graphQLClient.execute(body, (JsonObject o) -> {
				return o;
			});
			logger.info("Data returned from the query me operation from hunter's api :- [{}] ",data.toString());
			String baseName = data.getAsJsonObject("User_viewer").getAsJsonObject("me").get("baseName").toString();
			reponseMap.put("baseName", baseName);
		} catch (IOException e) {
			reponseMap.put("Error", "Some exception");
			throw new RuntimeException("could not get the respone of query me operation due to :- ", e);
		}
		return reponseMap;

	}

	private void headerUtil(String headerFormat) {
		String header = "";
		Map<String, String> keySignature = gpgSign.createSignature(headerFormat);
		header = keySignature.get("fingerPrint") + "_" + keySignature.get("hashingAlgo") + "_"
				+ keySignature.get("asciiArmoredSignature");
		logger.info("Header created after getting generated the signature :- [{]] ",header);
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
		body.addProperty("_timeUniqueId", uniqueId + "" + ZonedDateTime.now().toInstant().toEpochMilli());
		return body;
	}

	public void writeDataInFile(String data, String fileName) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputDirectory + "/" + fileName)))) {
			bw.write(data);
			bw.close();
		} catch (IOException e) {
			throw new RuntimeException("cannot write data to file", e);
		}
	}

	public void sendDataToExtrace(String url, String methodType, JsonObject payload) {
		if (methodType.equalsIgnoreCase("instantor")) {
			try {
				httpPost(url, convertPayloadToExtrace(payload, methodType));
			} catch (IOException e) {
				throw new RuntimeException("cannot send request to instantator due to :- ", e);
			}
		}
	}

	private String httpPost(String url, String payload) throws ClientProtocolException, IOException {
		HttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(url);
		StringEntity entity = new StringEntity(payload.toString(), ContentType.APPLICATION_JSON);
		logger.info("payload which is sent to instantor :- [{}]",payload.toString());
		httppost.setEntity(entity);

		// Execute and get the response.
		HttpResponse response = httpclient.execute(httppost);

		InputStream contentStream = response.getEntity().getContent();

		String contentString = IOUtils.toString(contentStream, "UTF-8");
		logger.info("Response status from instantor [{}] url is :- [{}]",url,response.getStatusLine().getStatusCode());
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new HttpResponseException(response.getStatusLine().getStatusCode(),
					"The server responded with" + contentString);
		}

		return contentString;
	}

	private String convertPayloadToExtrace(JsonObject payload, String methodType) {
		JsonObject payloadJson = new JsonObject();
		if (methodType.equalsIgnoreCase("instantor")) {
			JsonArray props = payload.getAsJsonArray("accountReportList");				
			payloadJson.add("account", props.get(0).getAsJsonObject().get("number"));
			payloadJson.addProperty("accountDigit", props.get(0).getAsJsonObject().get("number").getAsString().length());
			payloadJson.add("bank", payload.getAsJsonObject().get("bankInfo").getAsJsonObject().get("name"));
			payloadJson.addProperty("bankIntegrationType", methodType);
			payloadJson.add("bankStatement", payload);
			payloadJson.add("branch", payload.getAsJsonObject().get("bankInfo").getAsJsonObject().get("id"));

		} else {

		}
		return payloadJson.toString();
	}

}
