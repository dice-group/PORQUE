package org.dice.porque.model;

/**
 * Model class for PORQUE post response object
 *
 * @author Sourabh Poddar
 */
public class QAResponse {

    private String responseJSON;

    /**
     * @return the QALDResponse
     */
    public String getResponseJSON() {
        return responseJSON;
    }

    /**
     * @param responseJSON QALD response
     */
    public void setResponseJSON(String responseJSON) {
        this.responseJSON = responseJSON;
    }
}


