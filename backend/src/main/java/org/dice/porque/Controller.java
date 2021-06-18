package org.dice.porque;

import org.dice.porque.constants.PORQUEConstant;
import org.dice.porque.model.QARequest;
import org.dice.porque.model.QAResponse;
import org.dice.porque.tebaqa.TebaqaConnector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dice.porque.translator.LibreTranslate;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;


import javax.validation.Valid;
import java.util.List;
import java.util.Map;
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
     * @return "Service is running" if everything OK.
     */
    @GetMapping("/checkservice")
    public String checkService() {
        return "Service is running";
    }

    /**
     * Method to handle post request
     * @param qaRequest request body
     */
    @PostMapping(path = "/QA", consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_XML_VALUE})
    public QAResponse postmethod(@Valid @RequestBody QARequest qaRequest) {

        Map<String, Object> result = null;
        String query = qaRequest.getQuery();
        if (!qaRequest.getLang().equals(PORQUEConstant.ENGLISH_LANG_CODE)) {
            query = libreTranslate.tranlate(query, qaRequest.getLang(), PORQUEConstant.ENGLISH_LANG_CODE);
        }
        JSONObject tebaqaResponse = (JSONObject) new TebaqaConnector().getTebaqaResponse(query, PORQUEConstant.ENGLISH_LANG_CODE);
        try {
            result = new ObjectMapper().readValue(tebaqaResponse.toString(), new TypeReference<>() {
            });

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new QAResponse((List<String>) result.get("answers"));
    }
}
