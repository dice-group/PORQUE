package org.dice.porque.qasystems;

import org.dice.porque.constants.PORQUEConstant;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Class to connect to TeBaQa system.
 *
 * @author Sourabh Poddar
 */
public class Tebaqa implements QASystems {
    private static final String requestURL = PORQUEConstant.TEBAQA_URL;

    public Object getAnswer(String query, String lang) {
        BufferedReader reader;
        String line;
        StringBuilder responseContent = new StringBuilder();
        String urlParameters = "query=" + query + "&lang=" + lang;
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        URL url;
        JSONObject jsonObject = null;
        try {
            url = new URL(requestURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            DataOutputStream wr;
            wr = new DataOutputStream(connection.getOutputStream());
            wr.write(postData);
            int status;
            status = connection.getResponseCode();
            if (status > 299) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
            }
            jsonObject = new JSONObject(responseContent.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public String getQALDresponse(JSONObject tebaqaResponse, String query) {
        String type = null;
        String sparqlQuery;
        JSONObject response = null;

        try {
            JSONArray questions = new JSONArray();
            JSONArray question = tebaqaResponse.getJSONArray("questions");
            sparqlQuery = question.getJSONObject(0).getJSONObject("query").get("sparql").toString();
            JSONArray bindings = new JSONObject(question.getJSONObject(0).getJSONObject("question").get("answers").toString()).getJSONObject("results").getJSONArray("bindings");
            JSONArray resultbindings = new JSONArray();
            for (int i = 0; i < bindings.length(); i++) {
                if (i == 0)
                    type = bindings.getJSONObject(i).getJSONObject("x").get("type").toString();
                String value = bindings.getJSONObject(i).getJSONObject("x").get("value").toString();
                resultbindings.put(new JSONObject().put("uri", new JSONObject().put("type", type)
                        .put("value", value)));
            }
            JSONObject qu = new JSONObject();
            qu.put("id", 1);
            qu.put("question", new JSONArray().put(new JSONObject().put("language", "en")
                    .put("string", query)));
            qu.put("query", new JSONObject().put("sparql", sparqlQuery));
            qu.put("answers", new JSONArray().put(new JSONObject()
                    .put("head", new JSONObject()
                            .put("vars", new JSONArray().put("uri")))
                    .put("results", new JSONObject()
                            .put("bindings", resultbindings))));
            questions.put(qu);
            response = new JSONObject().put("questions", questions);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response.toString();
    }
}
