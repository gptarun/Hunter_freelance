package com.quanto.extrace;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.instantor.api.elements.InstantorAPIKey;
import com.instantor.api.elements.InstantorEncryption;
import com.instantor.api.elements.InstantorMsgId;

public class Bankdetail {

	private static String apiKeyValue = "6feb9b5a-c210-4b03-b111-b960cc5aaf55";
	private static String msgid = "154c798aa69-1463637551721";

	public static void main(String[] args) throws IOException {

		JSONParser jsonParser = new JSONParser();

		String instantorFilpath = "src\\main\\resources\\Instantor_Bank.json";

		try (FileReader reader = new FileReader(instantorFilpath)) {
			Object fileObject = jsonParser.parse(reader);

			byte[] bankDetails = fileObject.toString().getBytes("UTF-8");;
			System.out.println(bankDetails);
			byte[] encryptedDetails = InstantorEncryption.B64_MD5_AES_CBC_PKCS5
					.encrypt(new InstantorAPIKey(apiKeyValue), new InstantorMsgId(msgid), bankDetails);

			System.out.println(encryptedDetails);
		} catch (Exception e) {
			e.printStackTrace();
		}

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

			// mapper.writeValue(new File("src\\main\\resources\\" + EmployeeDataStore +
			// ".json"), obj);
			System.out.println("Successfully save the file");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
