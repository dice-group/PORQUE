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
    private final String dbpediaEndpoint = PORQUEConstant.DBPEDIA_SPARL_LOCAL_ENDPOINT;

    @Override
    public String getQALDresponse(String query, String lang) {

        return null;
    }

    public String getQALDresponse(String query, String lang, int pipeline) {

        String qanaryResponseOutGraph = getResponseQanaryPipeline(query, pipeline);
        List<String> sparqlQuery = getSparqlQuery(qanaryResponseOutGraph);
        List<QanaryResult> results = executeSparqlDBpedia(sparqlQuery);
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (QanaryResult qanaryResult : results) {
            try {

                JSONArray bindings = new JSONArray();
                for (String resultstr : qanaryResult.result) {
                    bindings.put(
                            new JSONObject().put(
                                    "uri", new JSONObject().put(
                                            "type", "uri").put(
                                            "value", resultstr)
                            )
                    );
                }

                JSONObject question = new JSONObject();
                question.put(
                        "question", new JSONArray().put(
                                new JSONObject().put(
                                        "string", query).put(
                                        "language", "en"))).put(
                        "query", new JSONObject().put(
                                "sparql", qanaryResult.query)).put(
                        "answers", new JSONArray().put(
                                new JSONObject().put(
                                        "head", new JSONObject().put(
                                                "vars", new JSONArray().put(
                                                        "uri"))).put(
                                        "results", new JSONObject().put(
                                                "bindings", bindings))));
                jsonArray.put(question);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jsonObject.toString();
    }

    private List<QanaryResult> executeSparqlDBpedia(List<String> sparqlQuery) {

        List<QanaryResult> qanaryResults = new ArrayList<>();
        for (String query : sparqlQuery) {
            ArrayList<String> results = new ArrayList<>();
            QueryExecution qExe = QueryExecutionFactory.sparqlService(dbpediaEndpoint, query);
            String queryToken = query.substring(query.indexOf("?") + 1, Math.min(query.indexOf(".", query.indexOf("?")), query.indexOf(" ", query.indexOf("?"))));
            ResultSet resultset = qExe.execSelect();
            while (resultset.hasNext()) {
                QuerySolution querySolution = resultset.next();
                results.add(querySolution.get(queryToken).toString());
            }
            qanaryResults.add(new QanaryResult(query, results));
        }
        return qanaryResults;
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

    public String getResponseQanaryPipeline(String query, int pipeline) {
        String outGraph = "";
        String componentListJson = new String();
        switch (pipeline) {
            case 1:
                componentListJson = "{ \"question\": \"" + query + "\", \"componentlist\":[\"NED-DBpediaSpotlight\",\"RelationLinker2\",\"SINA\"]}";
                break;
            case 2:
                componentListJson = "{ \"question\": \"" + query + "\", \"componentlist\":[\"NED-Falcon\",\"RelationLinker2\",\"SINA\"]}";
                break;
            case 3:
                componentListJson = "{ \"question\": \"" + query + "\", \"componentlist\":[\"NED-Falcon-Enriched\",\"RelationLinker3\",\"SINA\"]}";
                break;
            case 4:
                componentListJson = "{ \"question\": \"" + query + "\", \"componentlist\":[\"NED-Falcon\",\"RelationLinker2\",\"QB-SQG\"]}";
                break;
            case 5:
                componentListJson = "{ \"question\": \"" + query + "\", \"componentlist\":[\"NED-Falcon-Enriched\",\"RelationLinker3\",\"QB-SQG\"]}";
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

    protected class QanaryResult {
        public String query;
        public ArrayList<String> result;

        QanaryResult(String query) {
            this.query = query;
        }

        QanaryResult(String query, ArrayList<String> result) {
            this.query = query;
            this.result = result;
        }
    }

}
