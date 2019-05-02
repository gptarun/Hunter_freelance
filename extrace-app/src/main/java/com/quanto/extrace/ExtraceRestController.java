package com.quanto.extrace;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.instantor.api.InstantorException;
import com.instantor.api.elements.InstantorAPIKey;
import com.instantor.api.elements.InstantorEncryption;
import com.instantor.api.elements.InstantorMsgId;
import com.quanto.extrace.service.DataService;

@RestController
public class ExtraceRestController {
	@Autowired
	private DataService dataService;

	@Value("${instantor.api.key}")
	private String apiKey;

	@RequestMapping(value = "/callHunter", method = RequestMethod.POST)
	public ResponseEntity<Object> callHunter() {
		JsonObject variables = null;
		Map<String, String> sessionValues = null;
		try {
			System.out.println("Testing");
			sessionValues = dataService
					.createSession("http://ec2-13-233-232-197.ap-south-1.compute.amazonaws.com:8080/webHookHunter");
			variables = new JsonObject();
			variables.addProperty("searchText", "test");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(sessionValues, HttpStatus.OK);
	}

	/**
	 * This request will be called by the Hunters API
	 */
	@RequestMapping(value = "/webHookHunter", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public String handleResponse(@RequestBody JsonObject response) {
		System.out.println("Hunter's response : " + response);
		String fingerPrint = response.get("fingerPrint").toString();
		JsonObject customerStatement = dataService.getCustomerStatement(fingerPrint);
		// store the data to JSON file
		return "";
	}

	/**
	 * This request will be called by the Instantor API
	 * 
	 * @throws InstantorException
	 * @throws NumberFormatException
	 */
	@RequestMapping(value = "/webhookInstantor", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> webhookURL(@RequestBody String responseInstantor) throws InstantorException {

		Map<String, Object> responseObject = new HashMap();
		// trying to fetch the body using instantor api

		/*
		 * InstantorParams reponse =
		 * InstantorParams.loadRequestParams(instantorParams.iS.getParamName(),
		 * instantorParams.iE.getParamName(), instantorParams.iM.getParamName(),
		 * instantorParams.iA.getParamName(), instantorParams.iP.getParamName(),
		 * Long.parseLong(instantorParams.iT.getParamName()));
		 */
		System.out.println("Get the webhook url response");
		List<String> responseParams = Arrays.asList(responseInstantor.split("&"));
		System.out.println(responseInstantor);
		Map<String, String> responseMap = new HashMap<>();
		for (String param : responseParams) {
			String[] paramArr = param.split("=", 2);
			responseMap.put(paramArr[0], paramArr[1]);
		}
		String msgId = responseMap.get("msg_id");
		String decryptedPayload = new String(
				InstantorEncryption.B64_MD5_AES_CBC_PKCS5.decrypt(new InstantorAPIKey(apiKey),
						new InstantorMsgId(responseMap.get("msg_id")), responseMap.get("payload").getBytes()));

		String accountNumber = null;
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonPayload = (JsonObject) jsonParser.parse(decryptedPayload);
		/*
		 * String bankName =
		 * jsonPayload.getAsJsonObject("bankInfo").get("name").getAsString(); JsonArray
		 * accountList = jsonPayload.getAsJsonArray("accountList"); for (JsonElement
		 * number : accountList) { accountNumber =
		 * number.getAsJsonObject().get("number").getAsString(); }
		 * 
		 * String fileName = bankName + "_" + accountNumber + "_statement.json";
		 */
		// here we are going to pass extrace url, webhook type, payload and file name
		String instantorURL = "http://ec2-18-207-220-175.compute-1.amazonaws.com:8080/bank_statement";
		dataService.sendDataToExtrace(instantorURL, "INSTANTOR", jsonPayload);
		responseObject.put("OK", msgId);
		return new ResponseEntity<Object>(responseObject, HttpStatus.OK);
	}

	/**
	 * To test our code is able to call Hunter's API or not
	 */
	@RequestMapping(value = "/queryMe", method = RequestMethod.POST)
	public ResponseEntity<Object> queryMe() {
		Map response = null;
		System.out.println("Inside the query me");
		try {
			response = dataService.queryMe();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/getUser", method = RequestMethod.POST)
	public ResponseEntity<Object> getUser() {
		System.out.println("Get the user data url response");
		return new ResponseEntity<>("Sucess", HttpStatus.OK);
	}

	@RequestMapping(value = "/getAccountDetails", method = RequestMethod.POST)
	public ResponseEntity<Object> getAccountDetails() {
		System.out.println("Get the account details url response");
		return new ResponseEntity<>("Sucess", HttpStatus.OK);
	}

	@RequestMapping(value = "/getAccountStatement", method = RequestMethod.POST)
	public ResponseEntity<Object> getAccountStatement() {
		System.out.println("Get the account statement url response");
		return new ResponseEntity<>("Sucess", HttpStatus.OK);
	}
}
