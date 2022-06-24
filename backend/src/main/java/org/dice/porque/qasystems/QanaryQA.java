package org.dice.porque.qasystems;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.dice.porque.constants.PORQUEConstant;
import org.dice.porque.util.PorqueUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class QanaryQA implements QASystems {
	
	Logger logger = LoggerFactory.getLogger(QanaryQA.class);

    private final String qanaryUrl = PORQUEConstant.QANARY_URL;
    private final String sparqlEndpoint = PORQUEConstant.QANARY_SPARQL_ENDPOINT;
    private final String dbpediaEndpoint = PORQUEConstant.DBPEDIA_SPARQL_LOCAL_ENDPOINT;

    @Override
    public String getQALDresponse(String query, String lang) {

        return null;
    }

    public String getQALDresponse(String query, String lang, int pipeline) {

        JSONObject respJson = new JSONObject();
        JSONArray questionsArr = new JSONArray();
        
        String sparqlEndpoint = dbpediaEndpoint;
        String defaultGraph = "";
        switch (pipeline) {
        	case 1:
            case 2:
            case 4:
                defaultGraph = "http://www.upb.de/en-dbp2016-10";
                break;
            case 3:
            case 5:
                defaultGraph = "http://www.upb.de/en-dbp2016-10-enriched";
                sparqlEndpoint = PORQUEConstant.DBPEDIA_SPARQL_ENR_LOCAL_ENDPOINT;
                break;
        }
        
        try {
        	respJson.put("questions", questionsArr);
        	// Fetch relevant qanary graph
            String qanaryResponseOutGraph = getResponseQanaryPipeline(query, pipeline);
            logger.debug("Qanary graph for the query: "+ qanaryResponseOutGraph);
            // Fetch the generated SPARQL(s) from the graph
            List<String> sparqlList = getSparqlQuery(qanaryResponseOutGraph);
            logger.debug("Received SPARQL: "+ sparqlList);
            for(String sparqlStr : sparqlList) {
            	logger.debug("Processing SPARQL: "+sparqlStr);
            	// Check if sparql is empty, if yes then continue
            	if(sparqlStr == null || sparqlStr.trim().isEmpty()) {
            		continue;
            	}
            	// Form the Question json
            	JSONObject questionJson = new JSONObject(String.format(PORQUEConstant.QUE_JSON, lang, query, sparqlStr));
            	// Run the sparql
            	JSONObject sparqlResp = PorqueUtil.fetchSparqlAnswer(sparqlStr, sparqlEndpoint, defaultGraph);
            	// Form the answer json
            	JSONObject answerJson = new JSONObject(PORQUEConstant.ANS_JSON);
            	// Pick items from the sparql results and put into the final answer
            	if(sparqlResp.has("head") && sparqlResp.getJSONObject("head").has("vars")) {
            		// Replace vars in answerjson
            		JSONArray vars = sparqlResp.getJSONObject("head").getJSONArray("vars");
            		answerJson.getJSONObject("head").put("vars", vars);
            	}
            	if(sparqlResp.has("results") && sparqlResp.getJSONObject("results").has("bindings")) {
            		// Replace bindings in answerjson
            		JSONArray bindings = sparqlResp.getJSONObject("results").getJSONArray("bindings");
            		answerJson.getJSONObject("results").put("bindings", bindings);
            	}
            	if(sparqlResp.has("boolean")) {
            		// insert boolean in answerjson
            		answerJson.put("boolean", sparqlResp.getBoolean("boolean"));
            	}
            	
            	questionJson.getJSONArray("answers").put(answerJson);
            	questionsArr.put(questionJson);
            }
            

        } catch (JSONException | IOException e) {
        	logger.debug("Error occured, stopping further processing of this question.");
            e.printStackTrace();
        } finally {
        	// Checking for default response
        	if(questionsArr.length() == 0) {
        		return String.format(PORQUEConstant.DEF_RESPONSE, query, lang);
        	}
        }

        return respJson.toString();
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
        try {
            ResultSet resultset = qExe.execSelect();
            while (resultset.hasNext()) {
                QuerySolution querySolution = resultset.next();
                spaqlQueries.add(querySolution.get("resultAsSparqlQuery").toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            qExe.close();
        }
        return spaqlQueries;
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
        logger.debug("Component List: "+ componentListJson);

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
                logger.debug("Qanary response: "+responseJson);
                // Check for outgraph
                if(responseJson.has("outGraph")) {
                	outGraph = (String) responseJson.get("outGraph");
                }
                else {
                	logger.error("Response from Qanary has no attribute named outgraph.");
                }
            }
            // HttpEntity entity = response.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outGraph;
    }

}
