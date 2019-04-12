package com.quanto.extrace;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.JsonObject;

/**
 * 
 * @author tarun
 * @since 10-Apr-2019
 *
 */
@Controller
public class CallBackController {

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
			URL callHunterURL = new URL("https://api.staging.contaquanto.com/graphql");
			Map<String, String> inputHeaders = new HashMap<>();
			inputHeaders.put("Content-Type", "application/graphql");
			String query = "";
			GraphQLClientHandling client = new GraphQLClientHandling(callHunterURL, inputHeaders);
			sessionValues = new HashMap<>();

			/*
			 * Here we need to get session url from the client object of
			 * GraphQLClientHandling https://staging.quanto.app/?hsession=
			 */
			String sessionURL = "https://staging.quanto.app/?hsession=1b92a8ae-bab0-4dc7-a25b-5f65d8413b24"; // for now
																												// need
																												// to
																												// remove
			sessionValues.put("sessionId", "1233456789");
			sessionValues.put("sessionURL", sessionURL);
			variables = new JsonObject();
			variables.addProperty("searchText", "test");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(sessionValues, HttpStatus.OK);
	}
}
