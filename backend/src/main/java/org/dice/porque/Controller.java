package org.dice.porque;

import org.dice.porque.constants.PORQUEConstant;
import org.dice.porque.model.QARequest;
import org.dice.porque.model.QAResponse;
import org.dice.porque.qasystems.QAnswer;
import org.dice.porque.qasystems.QanaryQA;
import org.dice.porque.qasystems.Tebaqa;
import org.dice.porque.translator.LibreTranslate;
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
     * Method to handle post request for tebaqa QA system
     *
     * @param qaRequest request body
     */
    @PostMapping(path = "/QA-tebaqa", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_XML_VALUE})
    public String postmethod(@Valid @ModelAttribute QARequest qaRequest) {
        String query = qaRequest.getQuery();
        if (!qaRequest.getLang().equals(PORQUEConstant.ENGLISH_LANG_CODE)) {
            query = libreTranslate.tranlate(query, qaRequest.getLang(), PORQUEConstant.ENGLISH_LANG_CODE);
        }
        QAResponse qaResponse = new QAResponse();
        qaResponse.setResponseJSON(new Tebaqa().getQALDresponse(query, PORQUEConstant.ENGLISH_LANG_CODE));
        return qaResponse.getResponseJSON();
    }

    /**
     * Method to handle post request for qanswer QA system
     *
     * @param qaRequest request body
     */
    @PostMapping(path = "/QA-qanswer", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_XML_VALUE})
    public String postQAnswer(@Valid @ModelAttribute QARequest qaRequest) {
        String query = qaRequest.getQuery();
        if (!qaRequest.getLang().equals(PORQUEConstant.ENGLISH_LANG_CODE)) {
            query = libreTranslate.tranlate(query, qaRequest.getLang(), PORQUEConstant.ENGLISH_LANG_CODE);
        }
        QAResponse qaResponse = new QAResponse();
        qaResponse.setResponseJSON(new QAnswer().getQALDresponse(query,PORQUEConstant.ENGLISH_LANG_CODE));
        return qaResponse.getResponseJSON();
    }

    /**
     * Method to handle post request for qanary QA system
     *
     * @param qaRequest request body
     */
    @PostMapping(path = "/qa-qanary", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_XML_VALUE})
    public String postQanaryQA(@Valid @ModelAttribute QARequest qaRequest) {
        String query = qaRequest.getQuery();
        if (!qaRequest.getLang().equals(PORQUEConstant.ENGLISH_LANG_CODE)) {
            query = libreTranslate.tranlate(query, qaRequest.getLang(), PORQUEConstant.ENGLISH_LANG_CODE);
        }
        QAResponse qaResponse = new QAResponse();
        qaResponse.setResponseJSON(new QanaryQA().getQALDresponse(query,PORQUEConstant.ENGLISH_LANG_CODE));
        return qaResponse.getResponseJSON();
    }
    /**
     * Method to handle post request for qanary QA system
     *
     * @param qaRequest request body
     */
    @PostMapping(path = "/qa-qanary2", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_XML_VALUE})
    public String postQanaryQA2(@Valid @ModelAttribute QARequest qaRequest) {
        String query = qaRequest.getQuery();
        if (!qaRequest.getLang().equals(PORQUEConstant.ENGLISH_LANG_CODE)) {
            query = libreTranslate.tranlate(query, qaRequest.getLang(), PORQUEConstant.ENGLISH_LANG_CODE);
        }
        QAResponse qaResponse = new QAResponse();
        qaResponse.setResponseJSON(new QanaryQA().getQALDresponse(query,PORQUEConstant.ENGLISH_LANG_CODE,2));
        return qaResponse.getResponseJSON();
    }
    @PostMapping(path = "/QA", consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_XML_VALUE})
    public String postmethodJSON(@Valid @RequestBody QARequest qaRequest) {
        String query = qaRequest.getQuery();
        if (!qaRequest.getLang().equals(PORQUEConstant.ENGLISH_LANG_CODE)) {
            query = libreTranslate.tranlate(query, qaRequest.getLang(), PORQUEConstant.ENGLISH_LANG_CODE);
        }
        QAResponse qaResponse = new QAResponse();
        qaResponse.setResponseJSON(new Tebaqa().getQALDresponse(query, PORQUEConstant.ENGLISH_LANG_CODE));
        return qaResponse.getResponseJSON();
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
