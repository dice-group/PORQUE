package org.dice.porque;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.validation.Valid;

import org.dice.porque.constants.PORQUEConstant;
import org.dice.porque.model.QARequest;
import org.dice.porque.model.QAResponse;
import org.dice.porque.qasystems.QAnswer;
import org.dice.porque.qasystems.QanaryQA;
import org.dice.porque.qasystems.Tebaqa;
import org.dice.porque.translator.LibreTranslate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class to map PORQUE http request
 *
 * @author Sourabh Poddar
 */
@RestController
public class Controller {
	
	Logger logger = LoggerFactory.getLogger(Controller.class);
	
	private static int counter = 0;

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
        // Fetch counter value
    	int countId = requestCounter();
    	// Log incoming query
    	logger.info(countId+"\tReceived request for the question: "+ query);
        if (!qaRequest.getLang().equals(PORQUEConstant.ENGLISH_LANG_CODE)) {
            query = libreTranslate.tranlate(query, qaRequest.getLang(), PORQUEConstant.ENGLISH_LANG_CODE);
        }
        QAResponse qaResponse = new QAResponse();
        qaResponse.setResponseJSON(new Tebaqa().getQALDresponse(query, PORQUEConstant.ENGLISH_LANG_CODE));
        // Log outgoing response
        logger.info(countId+"\tSending response: "+qaResponse.getResponseJSON());
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
        // Fetch counter value
    	int countId = requestCounter();
    	// Log incoming query
    	logger.info(countId+"\tReceived request for the question: "+ query);
        if (!qaRequest.getLang().equals(PORQUEConstant.ENGLISH_LANG_CODE)) {
            query = libreTranslate.tranlate(query, qaRequest.getLang(), PORQUEConstant.ENGLISH_LANG_CODE);
        }
        QAResponse qaResponse = new QAResponse();
        qaResponse.setResponseJSON(new QAnswer().getQALDresponse(query,PORQUEConstant.ENGLISH_LANG_CODE));
        // Log outgoing response
        logger.info(countId+"\tSending response: "+qaResponse.getResponseJSON());
        return qaResponse.getResponseJSON();
    }

    /**
     * Method to handle post request for qanary QA system
     *
     * @param qaRequest request body
     */
    @PostMapping(path = "/qa-qanary1", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_XML_VALUE})
    public String postQanaryQA(@Valid @ModelAttribute QARequest qaRequest) {
    	return processQanaryRequest(qaRequest, 1);
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
    	return processQanaryRequest(qaRequest, 2);
    }
    @PostMapping(path = "/qa-qanary3", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_XML_VALUE})
    public String postQanaryQA3(@Valid @ModelAttribute QARequest qaRequest) {
    	return processQanaryRequest(qaRequest, 3);
    }

    @PostMapping(path = "/qa-qanary4", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_XML_VALUE})
    public String postQanaryQA4(@Valid @ModelAttribute QARequest qaRequest) {
    	return processQanaryRequest(qaRequest, 4);
    }

    @PostMapping(path = "/qa-qanary5", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_XML_VALUE})
    public String postQanaryQA5(@Valid @ModelAttribute QARequest qaRequest) {
        return processQanaryRequest(qaRequest, 5);
    }
    
    
    private String processQanaryRequest(QARequest qaRequest, int pipelineCode) {
    	String query = qaRequest.getQuery();
    	// Fetch counter value
    	int countId = requestCounter();
    	// Log incoming query
    	logger.info(countId+"\tReceived request for the question: "+ query);
    	
        if (!qaRequest.getLang().equals(PORQUEConstant.ENGLISH_LANG_CODE)) {
            query = libreTranslate.tranlate(query, qaRequest.getLang(), PORQUEConstant.ENGLISH_LANG_CODE);
        }
        QAResponse qaResponse = new QAResponse();
        qaResponse.setResponseJSON(new QanaryQA().getQALDresponse(query,PORQUEConstant.ENGLISH_LANG_CODE,pipelineCode));
        // Log outgoing response
        logger.info(countId+"\tSending response: "+qaResponse.getResponseJSON());
        return qaResponse.getResponseJSON();
    }

    @PostMapping(path = "/QA", consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_XML_VALUE})
    public String postmethodJSON(@Valid @RequestBody QARequest qaRequest) {
        String query = qaRequest.getQuery();
        // Fetch counter value
    	int countId = requestCounter();
    	// Log incoming query
    	logger.info(countId+"\tReceived request for the question: "+ query);
        if (!qaRequest.getLang().equals(PORQUEConstant.ENGLISH_LANG_CODE)) {
            query = libreTranslate.tranlate(query, qaRequest.getLang(), PORQUEConstant.ENGLISH_LANG_CODE);
        }
        QAResponse qaResponse = new QAResponse();
        qaResponse.setResponseJSON(new Tebaqa().getQALDresponse(query, PORQUEConstant.ENGLISH_LANG_CODE));
        // Log outgoing response
        logger.info(countId+"\tSending response: "+qaResponse.getResponseJSON());
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
    
    private static int requestCounter() {
    	return ++counter;
    }
}
