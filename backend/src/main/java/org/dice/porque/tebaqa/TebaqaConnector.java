package org.dice.porque.tebaqa;

import org.dice.porque.constants.PORQUEConstant;
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
public class TebaqaConnector {
    private static final String requestURL = PORQUEConstant.TEBAQA_URL;

    public Object getTebaqaResponse(String query, String lang) {
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
}
