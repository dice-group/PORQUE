package org.dice.porque.qasystems;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.dice.porque.constants.PORQUEConstant;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class QanaryQA implements QASystems {

    private final String qanaryUrl = PORQUEConstant.QANARY_URL;
    private final String sparqlEndpoint = PORQUEConstant.QANARY_SPARQL_ENDPOINT;
    private final String dbpediaEndpoint = PORQUEConstant.DBPEDIA_SPARQL_ENDPOINT;

    @Override
    public String getQALDresponse(String query, String lang) {

        String qanaryResponseOutGraph = getResponseQanaryPipeline1(query);
        List<String> sparqlQuery = getSparqlQuery(qanaryResponseOutGraph);
        List<String> results = executeSparqlDBpedia(sparqlQuery);
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (String result : results)
            jsonArray.put(result);
        try {
            jsonObject.put("results", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public String getQALDresponse(String query, String lang, int pipeline) {

        String qanaryResponseOutGraph = getResponseQanaryPipeline(query,pipeline);
        List<String> sparqlQuery = getSparqlQuery(qanaryResponseOutGraph);
        List<String> results = executeSparqlDBpedia(sparqlQuery);
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (String result : results)
            jsonArray.put(result);
        try {
            jsonObject.put("results", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    private List<String> executeSparqlDBpedia(List<String> sparqlQuery) {
        List<String> results = new ArrayList<>();
        for (String query : sparqlQuery) {
            QueryExecution qExe = QueryExecutionFactory.sparqlService(dbpediaEndpoint, query);
            String queryToken = query.substring(query.indexOf("?") + 1, Math.min(query.indexOf(".", query.indexOf("?")), query.indexOf(" ", query.indexOf("?"))));
            ResultSet resultset = qExe.execSelect();
            while (resultset.hasNext()) {
                QuerySolution querySolution = resultset.next();
                results.add(querySolution.get(queryToken).toString());
            }
        }
        return results;
    }

    private List<String> getSparqlQuery(String qanaryResponseOutGraph) {
        List<String> spaqlQueries = new ArrayList<>();
        String query = "PREFIX oa: <http://www.w3.org/ns/openannotation/core/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX qa: <http://www.wdaqua.eu/qa#>\n" +
                "SELECT *\n" +
                "FROM <" + qanaryResponseOutGraph + ">\n" +
                "WHERE {\n" +
                "    ?s rdf:type qa:AnnotationOfAnswerSPARQL.\n" +
                "    ?s oa:hasBody ?resultAsSparqlQuery.\n" +
                "}";
        QueryExecution qExe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
        ResultSet resultset = qExe.execSelect();
        while (resultset.hasNext()) {
            QuerySolution querySolution = resultset.next();
            spaqlQueries.add(querySolution.get("resultAsSparqlQuery").toString());
        }
        return spaqlQueries;
    }

    public String getResponseQanaryPipeline1(String query) {
        String outGraph = "";
        String componentListJson = new String();
        componentListJson = "{ \"question\": \"" + query + "\", \"componentlist\":[\"NED-DBpediaSpotlight\",\"RelationLinker2\",\"SINA\"]}";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(qanaryUrl);
        httppost.addHeader("Content-Type", "application/json");
        try {
            StringEntity entitytemp = new StringEntity(componentListJson);
            httppost.setEntity(entitytemp);
            CloseableHttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                // String result = getStringFromInputStream(instream);
                String responsetext = IOUtils.toString(instream, StandardCharsets.UTF_8.name());
                JSONObject responseJson = new JSONObject(responsetext);
                outGraph = (String) responseJson.get("outGraph");
            }
            // HttpEntity entity = response.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outGraph;
    }
    public String getResponseQanaryPipeline(String query,int pipeline) {
        String outGraph = "";
        String componentListJson = new String();
        switch (pipeline)
        {
            case 1:
                componentListJson = "{ \"question\": \"" + query + "\", \"componentlist\":[\"NED-DBpediaSpotlight\",\"RelationLinker2\",\"SINA\"]}";
                break;
            case 2:
                componentListJson = "{ \"question\": \"" + query + "\", \"componentlist\":[\"NED-Falcon\",\"RelationLinker2\",\"SINA\"]}";
                break;
            case 3:
                componentListJson = "{ \"question\": \"" + query + "\", \"componentlist\":[\"NED-Falcon-Enriched\",\"RelationLinker3\",\"SINA\"]}";
                break;

        }


        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(qanaryUrl);
        httppost.addHeader("Content-Type", "application/json");
        try {
            StringEntity entitytemp = new StringEntity(componentListJson);
            httppost.setEntity(entitytemp);
            CloseableHttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                // String result = getStringFromInputStream(instream);
                String responsetext = IOUtils.toString(instream, StandardCharsets.UTF_8.name());
                JSONObject responseJson = new JSONObject(responsetext);
                outGraph = (String) responseJson.get("outGraph");
            }
            // HttpEntity entity = response.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outGraph;
    }
}
