package com.Quanto.Hunter.QuantoCallBack;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;

@RestController
public class CallBackController {

	@RequestMapping("/callHunter")
	public void callHunter() {
		try {
			URL callHunterURL = new URL("need to give url");
			Map<String, String> inputParam = new HashMap<>();

			String query = "";
			GraphQLClientHandling client = new GraphQLClientHandling(callHunterURL, inputParam);

			JsonObject variables = new JsonObject();
			variables.addProperty("searchText", "test");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
