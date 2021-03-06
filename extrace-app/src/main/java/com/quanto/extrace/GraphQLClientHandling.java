package com.quanto.extrace;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import graphql.GraphQLException;

/**
 * 
 * @author tarun
 * @since 10-Apr-2019
 */
public class GraphQLClientHandling {
	private URL endpoint;
	private Map<String, String> headers;

	/**
	 * Creates a new instance of this class for the given GraphQL endpoint.
	 * 
	 * @param endpoint
	 *            The endpoint to use.
	 */
	public GraphQLClientHandling(URL endpoint) {
		this(endpoint, new HashMap<String, String>());
	}

	/**
	 * Creates a new instance of this class for the given GraphQL endpoint and
	 * request heraders.
	 * 
	 * @param endpoint
	 *            The endpoint to use.
	 * @param headers
	 *            The headers to use for each request.
	 */
	public GraphQLClientHandling(URL endpoint, Map<String, String> headers) {
		this.endpoint = endpoint;
		this.headers = headers;
	}

	/**
	 * Updates the header of the object
	 * 
	 * @param key
	 * @param value
	 */
	public void updateHeader(String key, String value) {
		this.headers.put(key, value);
	}

	public void updateHeader(Map<String, String> header) {
		this.headers = header;
	}

	/**
	 * Executes the given query or mutation.
	 * 
	 * @param query
	 *            The query or mutation to execute.
	 * @param mapper
	 *            A function object to convert the payload from JSON to an actual
	 *            object.
	 * @param <T>
	 *            The desired return object type.
	 * @return The result of the query, with the mapper function applied.
	 * @throws IOException
	 *             Thrown in case of connection errors.
	 * @throws GraphQLException
	 *             Thrown in case of errors set in the response body.
	 */
	public <T> T execute(String query, Function<JsonObject, T> mapper) throws IOException, GraphQLException {
		return execute(query, new JsonObject(), String.valueOf(ZonedDateTime.now().toInstant().toEpochMilli()), "1234",
				mapper);
	}

	/**
	 * Executes the given query or mutation.
	 * 
	 * @param query
	 *            The query or mutation to execute.
	 * @param variables
	 *            The variables to pass to the query or mutation.
	 * @param mapper
	 *            A function object to convert the payload from JSON to an actual
	 *            object.
	 * @param <T>
	 *            The desired return object type.
	 * @return The result of the query, with the mapper function applied.
	 * @throws IOException
	 *             Thrown in case of connection errors.
	 * @throws GraphQLException
	 *             Thrown in case of errors set in the response body.
	 */
	public <T> T execute(String query, JsonObject variables, String timeStamp, String uniqueId,
			Function<JsonObject, T> mapper) throws IOException, GraphQLException {
		JsonObject body = new JsonObject();
		body.addProperty("query", query);
		body.add("variables", variables);
		body.addProperty("_timestamp", timeStamp);
		body.addProperty("_timeUniqueId", uniqueId);
		String responseString = execute(endpoint.toString(), body.toString(), headers);

		JsonObject response = (JsonObject) (new JsonParser()).parse(responseString);

		if (response.has("error")) {
			throw new GraphQLException(response.get("error").getAsString());
		}
		if (response.has("errors")) {
			String message = "";
			for (JsonElement error : response.get("errors").getAsJsonArray()) {
				message += error.toString();
			}
			throw new GraphQLException(message);
		}

		return mapper.apply(response.get("data").getAsJsonObject());
	}

	public <T> T execute(JsonObject body, Function<JsonObject, T> mapper) throws IOException, GraphQLException {
		String responseString = execute(endpoint.toString(), body.toString(), headers);

		JsonObject response = (JsonObject) (new JsonParser()).parse(responseString);

		if (response.has("error")) {
			throw new GraphQLException(response.get("error").getAsString());
		}
		if (response.has("errors")) {
			String message = "";
			for (JsonElement error : response.get("errors").getAsJsonArray()) {
				message += error.toString();
			}
			throw new GraphQLException(message);
		}

		return mapper.apply(response.get("data").getAsJsonObject());
	}

	private static String execute(String url, String jsonEntity, Map<String, String> headers) throws IOException {
		HttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(url);
		for (Map.Entry<String, String> header : headers.entrySet()) {

			httppost.addHeader(header.getKey(), header.getValue());
		}

		StringEntity entity = new StringEntity(jsonEntity, ContentType.APPLICATION_JSON);
		
		httppost.setEntity(entity);

		// Execute and get the response.
		HttpResponse response = httpclient.execute(httppost);

		InputStream contentStream = response.getEntity().getContent();

		String contentString = IOUtils.toString(contentStream, "UTF-8");

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new HttpResponseException(response.getStatusLine().getStatusCode(),
					"The server responded with" + contentString);
		}

		return contentString;
	}
}
