package com.quanto.extrace;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.instantor.api.InstantorException;
import com.instantor.api.elements.InstantorAPIKey;
import com.instantor.api.elements.InstantorEncryption;
import com.instantor.api.elements.InstantorMsgId;
import com.quanto.extrace.service.DataService;

/**
 * 
 * @author tarun
 * @since 10-Apr-2019
 *
 */
@Controller
public class CallBackController {

	@Autowired
	private DataService dataService;

	@Value("${instantor.api.key}")
	private String apiKey;

	@RequestMapping("/")
	public String homePage() {
		return "login";
	}

	@RequestMapping(value = "/callHunter", method = RequestMethod.POST)
	public ResponseEntity<Object> callHunter() {
		JsonObject variables = null;
		Map<String, String> sessionValues = null;
		try {
			System.out.println("Testing");
			sessionValues = dataService.createSession("https://52946d5c.ngrok.io/webhook");
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
	@RequestMapping(value = "/webhookInstantor", method = RequestMethod.POST)
	public ResponseEntity<Object> webhookURL(@RequestBody String responseInstantor) throws InstantorException {

		System.out.println("Testing");
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
		Map<String, String> responseMap = new HashMap<>();
		for (String param : responseParams) {
			String[] paramArr = param.split("=");
			responseMap.put(paramArr[0], paramArr[1]);
		}

		String decryptedPayload = InstantorEncryption.B64_MD5_AES_CBC_PKCS5.decrypt(new InstantorAPIKey(apiKey),
				new InstantorMsgId(responseMap.get("msg_id")), responseMap.get("payload").getBytes()).toString();

		JsonParser jsonParser = new JsonParser();
		JsonObject jsonPayload = (JsonObject) jsonParser.parse(decryptedPayload);
		String bankName = jsonPayload.getAsJsonObject("bankInfo").get("name").getAsString();
		String accountNumber = jsonPayload.getAsJsonObject("accountList").get("number").getAsString();
		String fileName = bankName + "_" + accountNumber + "_statement.json";
		dataService.writeDataInFile(decryptedPayload, fileName);
		return new ResponseEntity<>("Success", HttpStatus.OK);
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
