package org.dice.porque.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

public class PorqueUtil {
	
	/**
	 * Method to execute the SPARQL query for the given endpoint and default graph and return the json based SPARQL response. 
	 * @param query : SPARQL query to execute
	 * @param endpoint : HTTP based SPARQL endpoint
	 * @param defaultGraph : Name of the graph (can be empty or null)
	 * @return json based SPARQL response
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONObject fetchSparqlAnswer(String query, String endpoint, String defaultGraph)
			throws ClientProtocolException, IOException, JSONException {
		JSONObject jsonObject = null;

		HttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(endpoint);
		httppost.setHeader("Accept", "application/sparql-results+json");
		// Request parameters and other properties.
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("query", query));
		if(defaultGraph!=null && defaultGraph.trim().length() > 0) {
			params.add(new BasicNameValuePair("dflt_graph", defaultGraph));
		}
		httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

		// Execute and get the response.
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			try (InputStream instream = entity.getContent()) {
				// convert inputstream to json
				jsonObject = new JSONObject(new String(instream.readAllBytes()));
			}
		}

		return jsonObject;
	}
}
