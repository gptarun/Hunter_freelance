package com.codebind;

import java.io.File;
import java.io.IOException;


import org.json.simple.JSONObject;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class BankDetails {
   
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
   
		JSONObject obj=new JSONObject();
		obj.put("routingType", "COMPE");
		obj.put("routingNumber", 077);
		obj.put("branchNumber", 0001);
		obj.put("accountNumber", 52314);
		
		
		ObjectMapper mapper=new ObjectMapper();
		
		try
		{
			
			mapper.writeValue(new File("C:/Users/Himanshu Garg/Documents/BankDetails.json"), obj);
		    System.out.println("Successfully save the file");
		} catch(JsonGenerationException e)
		{
			e.printStackTrace();
		}
		catch(JsonMappingException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		}
		  
	}


