package com.quanto.extrace;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.JsonObject;
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
			sessionValues = dataService.createSession("YOUR_CALLBACK_URL");

			/*
			 * Here we need to get session url from the client object of
			 * GraphQLClientHandling https://staging.quanto.app/?hsession=
			 */
			//String sessionURL = "https://staging.quanto.app/?hsession=1b92a8ae-bab0-4dc7-a25b-5f65d8413b24"; // for now
			//sessionValues.put("sessionId", "1233456789");
			//sessionValues.put("sessionURL", sessionURL);
			
			variables = new JsonObject();
			variables.addProperty("searchText", "test");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(sessionValues, HttpStatus.OK);
	}
}
