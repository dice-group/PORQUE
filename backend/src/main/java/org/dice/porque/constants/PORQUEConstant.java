package org.dice.porque.constants;

/**
 *  This interface consists of all the constant values across PORQUE
 *
 * @author Sourabh
 */
public interface PORQUEConstant {

    //Language Codes
    String ENGLISH_LANG_CODE = "en";

    //API URL
    String LIBRE_TRANSLATE_URL = "https://translate.astian.org/translate";
    String TEBAQA_URL = "http://tebaqa.cs.upb.de:8080/qa-porque";
    String QANSWER_URL = "http://qanswer-core1.univ-st-etienne.fr/api/gerbil";
    String QANARY_URL = "http://porque.cs.upb.de:8888/startquestionansweringwithtextquestion";
    String QANARY_SPARQL_ENDPOINT = "http://admin:admin@porque.cs.upb.de:5820/qanary/query";
    String DBPEDIA_SPARQL_ENDPOINT = "http://dbpedia.org/sparql";
    String DBPEDIA_SPARQL_LOCAL_ENDPOINT = "http://porque.cs.upb.de:8830/sparql";
    String DBPEDIA_SPARQL_ENR_LOCAL_ENDPOINT = "http://porque.cs.upb.de:8820/sparql";

    //Knowledge Base
    String DBPEDIA_KB = "dbpedia";

}
