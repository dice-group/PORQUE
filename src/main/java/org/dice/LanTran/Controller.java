package org.dice.LanTran;

import org.dice.LanTran.Model.QARequest;
import org.dice.LanTran.Model.QAResponse;
import org.dice.LanTran.Tebaqa.TebaqaConnector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/PORQUE")
public class Controller {


    @GetMapping("/checkservive")
    public String checkService() {
        return "Service is running";
    }

    @PostMapping(path = "/QA", consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_XML_VALUE})
    public QAResponse postmethod(@Valid @RequestBody QARequest qaRequest) {

        Map<String, Object> result = null;
        JSONObject tebaqaResponse = (JSONObject) new TebaqaConnector().getTebaqaResponse(qaRequest.getQuery(), qaRequest.getLang());
        try {
            result = new ObjectMapper().readValue(tebaqaResponse.toString(), new TypeReference<>() {
            });

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new QAResponse((List<String>) result.get("answers"));
    }
}
