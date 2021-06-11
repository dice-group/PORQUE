package org.dice.porque.model;

import java.util.List;

public class QAResponse {

    private List<String> answer;
    public QAResponse(List<String> answer) {
        this.answer = answer;
    }
    public List<String> getAnswer() {
        return answer;
    }

    public void setAnswer(List<String> answer) {
        this.answer = answer;
    }




}
