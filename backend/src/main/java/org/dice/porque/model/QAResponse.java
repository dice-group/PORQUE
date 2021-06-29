package org.dice.porque.model;

import java.util.Set;

/**
 * Model class for PORQUE post response object
 *
 * @author Sourabh Poddar
 */
public class QAResponse {

    private Set<String> answers;
    private String type;
    private String sparqlQuery;

    /**
     * @return the answers
     */
    public Set<String> getAnswers() {
        return answers;
    }

    /**
     * @param answers the answers to set
     */
    public void setAnswers(Set<String> answers) {
        this.answers = answers;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the sparqlQuery
     */
    public String getSparqlQuery() {
        return sparqlQuery;
    }

    /**
     * @param sparqlQuery the sparqlQuery to set
     */
    public void setSparqlQuery(String sparqlQuery) {
        this.sparqlQuery = sparqlQuery;
    }

}


