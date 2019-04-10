package com.quanto.extrace;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;

/**
 * 
 * @author tarun
 * @since 10-Apr-2019
 *
 */
@RestController
public class CallBackController {

	@RequestMapping("/callHunter")
	public void callHunter() {
		try {
			URL callHunterURL = new URL("https://api.staging.contaquanto.com/graphql");
			Map<String, String> inputHeaders = new HashMap<>();
			inputHeaders.put("Content-Type", "application/graphql");
			String query = "";
			GraphQLClientHandling client = new GraphQLClientHandling(callHunterURL, inputHeaders);

			JsonObject variables = new JsonObject();
			variables.addProperty("searchText", "test");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
