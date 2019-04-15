package com.quanto.extrace;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Bankdetail {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		JSONObject obj = new JSONObject();
		obj.put("routingType", "COMPE");
		obj.put("routingNumber", 077);
		obj.put("branchNumber", 0001);
		obj.put("accountNumber", 52314);

		ObjectMapper mapper = new ObjectMapper();
		Map result = new HashMap();

		result.put("routingType", "COMPE");
		result.put("routingNumber", 077);
		result.put("branchNumber", 0001);
		result.put("accountNumber", 52314);

		String EmployeeDataStore = result.get("routingType") + "_" + result.get("routingNumber") + "_"
				+ result.get("branchNumber") + result.get("accountNumber");

		try {

			mapper.writeValue(new File("src\\main\\resources\\" + EmployeeDataStore + ".json"), obj);
			System.out.println("Successfully save the file");
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
