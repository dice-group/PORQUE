package org.dice.LanTran.Model;

import org.springframework.lang.NonNull;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class QARequest {

    @NotNull(message = "Please provide a Query")
    @Size(min =2)
    private String query;
    @NotNull(message = "Please provide the source language")
    private String lang;

    public QARequest(String query, String lang) {
        this.query = query;
        this.lang = lang;
    }
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }


}
