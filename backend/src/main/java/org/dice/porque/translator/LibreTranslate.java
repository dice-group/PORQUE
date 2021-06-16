package org.dice.porque.translator;

import org.dice.porque.constants.PORQUEConstant;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Class to translate text using Libre Translator
 *
 * @author Sourabh Poddar
 */
@Component
public class LibreTranslate implements LanguageTranslator {

    private static final String requestURL = PORQUEConstant.LIBRE_TRANSLATE_URL;

    @Override
    public String tranlate(String query, String source, String target) {

        BufferedReader reader;
        String line;
        String result = null;
        StringBuilder responseContent = new StringBuilder();
        String urlParameters = "q=" + query + "&source=" + source + "&target=" + target;
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        URL url;
        JSONObject jsonObject;
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
            result = jsonObject.get("translatedText").toString();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return result;

    }

    @Override
    public Object listSupportLang() {

        return null;
    }
}
