package org.dice.porque;

import org.dice.porque.constants.PORQUEConstant;
import org.dice.porque.model.QARequest;
import org.dice.porque.model.QAResponse;
import org.dice.porque.qasystems.Tebaqa;
import org.dice.porque.translator.LibreTranslate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import javax.validation.Valid;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Controller class to map PORQUE http request
 *
 * @author Sourabh Poddar
 */
@RestController
public class Controller {

    @Autowired
    private LibreTranslate libreTranslate;

    /**
     * Check service method.
     *
     * @return "Service is running" if everything OK.
     */
    @GetMapping("/checkservice")
    public String checkService() {
        return "Service is running";
    }

    /**
     * Method to handle post request
     *
     * @param qaRequest request body
     */
    @PostMapping(path = "/QA", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_XML_VALUE})
    public QAResponse postmethod(@Valid @ModelAttribute QARequest qaRequest) {
        String query = qaRequest.getQuery();
        if (!qaRequest.getLang().equals(PORQUEConstant.ENGLISH_LANG_CODE)) {
            query = libreTranslate.tranlate(query, qaRequest.getLang(), PORQUEConstant.ENGLISH_LANG_CODE);
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(qaRequest.getQuery()).append("\n").append(qaRequest.getLang()).append("\n").append(query).append("\n");

        Set<String> answer = new HashSet<>();
        String type = null;
        String sparqlQuery = null;
        JSONObject tebaqaResponse = (JSONObject) new Tebaqa().getAnswer(query, PORQUEConstant.ENGLISH_LANG_CODE);
        try {
            JSONArray question = tebaqaResponse.getJSONArray("questions");
            sparqlQuery = question.getJSONObject(0).getJSONObject("query").get("sparql").toString();
            JSONArray bindings = new JSONObject(question.getJSONObject(0).getJSONObject("question").get("answers").toString()).getJSONObject("results").getJSONArray("bindings");
            for (int i = 0; i < bindings.length(); i++) {
                if (i == 0)
                    type = bindings.getJSONObject(i).getJSONObject("x").get("type").toString();
                answer.add(bindings.getJSONObject(i).getJSONObject("x").get("value").toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        stringBuffer.append(answer).append("\n").append(type).append("\n").append(sparqlQuery).append("\n");
        writeLogtoFile(stringBuffer);
        QAResponse qaResponse = new QAResponse();
        qaResponse.setAnswers(answer);
        qaResponse.setType(type);
        qaResponse.setSparqlQuery(sparqlQuery);
        return qaResponse;
    }

    @PostMapping(path = "/QA", consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_XML_VALUE})
    public QAResponse postmethodJSON(@Valid @RequestBody QARequest qaRequest) {
        String query = qaRequest.getQuery();
        if (!qaRequest.getLang().equals(PORQUEConstant.ENGLISH_LANG_CODE)) {
            query = libreTranslate.tranlate(query, qaRequest.getLang(), PORQUEConstant.ENGLISH_LANG_CODE);
        }
        Set<String> answer = new HashSet<>();
        String type = null;
        String sparqlQuery = null;
        JSONObject tebaqaResponse = (JSONObject) new Tebaqa().getAnswer(query, PORQUEConstant.ENGLISH_LANG_CODE);
        try {
            JSONArray question = tebaqaResponse.getJSONArray("questions");
            sparqlQuery = question.getJSONObject(0).getJSONObject("query").get("sparql").toString();
            JSONArray bindings = new JSONObject(question.getJSONObject(0).getJSONObject("question").get("answers").toString()).getJSONObject("results").getJSONArray("bindings");
            for (int i = 0; i < bindings.length(); i++) {
                if (i == 0)
                    type = bindings.getJSONObject(i).getJSONObject("x").get("type").toString();
                answer.add(bindings.getJSONObject(i).getJSONObject("x").get("value").toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        QAResponse qaResponse = new QAResponse();
        qaResponse.setAnswers(answer);
        qaResponse.setType(type);
        qaResponse.setSparqlQuery(sparqlQuery);
        return qaResponse;
    }
    private void writeLogtoFile(StringBuffer stringBuffer) {
        File file = new File(getClass().getClassLoader().getResource("").getPath() + "log.txt");
        try {
            if (!file.exists())
                file.createNewFile();
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(stringBuffer.toString());
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
