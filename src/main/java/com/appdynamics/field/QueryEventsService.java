package com.appdynamics.field;

import com.appdynamics.field.utils.EventAggregator;
import com.appdynamics.field.utils.EventWorkflow;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by laurent.martin on 4/3/17.
 */
public class QueryEventsService {
    private static final Logger logger = Logger.getLogger(QueryEventsService.class);
    private String esUrl = "";
    private Integer esPort = 0;
    private String globalAccountName = "";
    private String apiKey = "";
    private String accept = "application/vnd.appd.events+json;v=1";
    private Boolean multipleQueries = false;
    private String contentType = "application/vnd.appd.events+text;v=1";
    private String contentTypeJson = "application/vnd.appd.events+json;v=1";
    private String adql = "";
    private Integer limit = 20000;
    private String mode = "none";
    private Integer offset = 0;
    private Integer count = 0;
    private DateTime startDate = null;
    private DateTime endDate = null;
    private String publishSchema = "";
    private String schemaTemplate = "";
    private String fieldAppName = null;
    private String fieldBtName = null;
    private String fieldEventTime = null;
    private String fieldGuid = null;
    private String fieldCommonId = null;
    private String fieldResponseTime = null;
    private String fieldUserExperience = null;

    public Integer getLimit() {
        return limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public String getMode() {
        return mode;
    }

    public String getFieldAppName() {
        return fieldAppName;
    }

    public String getFieldResponseTime() {
        return fieldResponseTime;
    }

    public String getFieldUserExperience() {
        return fieldUserExperience;
    }

    public String getFieldBtName() {
        return fieldBtName;
    }

    public String getFieldEventTime() {
        return fieldEventTime;
    }

    public String getFieldGuid() {
        return fieldGuid;
    }

    public String getFieldCommonId() {
        return fieldCommonId;
    }


    public void init(Map<String, String> config, DateTime startDate, DateTime endDate) throws Exception {
        logger.info("Initializing environment with config file items...");
        if (startDate != null)
            this.startDate = new DateTime(startDate);
        if (endDate != null)
            this.endDate = new DateTime(endDate);

        if (!config.containsKey("es-url") & !config.containsKey("es-port") & !config.containsKey("global-account-name") & !config.containsKey("api-key")) {
            throw new Exception("Missing key configuration element...");
        } else {
            this.esUrl = config.get("es-url");
            this.esPort = Integer.parseInt(config.get("es-port"));
            this.globalAccountName = config.get("global-account-name");
            this.apiKey = config.get("api-key");
        }

        if (config.containsKey("query-limit")) {
            this.limit = Integer.parseInt(config.get("query-limit"));
        }

        if (config.containsKey("mode")) {
            this.mode = config.get("mode");
            if (this.mode.equals("page") & this.limit > 10000)
                this.limit = 10000;
            this.count = this.limit;
        }

        if (config.containsKey("accept")) {
            this.accept = config.get("accept");
        }
        if (config.containsKey("multiple-queries")) {
            this.multipleQueries = Boolean.parseBoolean(config.get("multiple-queries"));
        }
        if (config.containsKey("content-type")) {
            this.contentType = config.get("content-type");
        }
        if (config.containsKey("content-type-json")) {
            this.contentTypeJson = config.get("content-type-json");
        }
        if (config.containsKey("adql")) {
            this.adql = config.get("adql");
        } else {
            throw new Exception("Missing ADQL query...");
        }

        if (!config.containsKey("field-application") & !config.containsKey("field-btname") & !config.containsKey("field-event-time") & !config.containsKey("field-guid") & !config.containsKey("field-common-id") & !config.containsKey("field-response-time") & !config.containsKey("field-user-experience")) {
            throw new Exception("Cannot find ADQL field correspondance...");
        } else {
            this.fieldAppName = config.get("field-application");
            this.fieldBtName = config.get("field-btname");
            this.fieldEventTime = config.get("field-event-time");
            this.fieldGuid = config.get("field-guid");
            this.fieldCommonId = config.get("field-common-id");
            this.fieldResponseTime = config.get("field-response-time");
            this.fieldUserExperience = config.get("field-user-experience");
        }
        if (config.containsKey("publish-schema") & config.containsKey("schema-template")) {
            this.publishSchema = config.get("publish-schema");
            this.schemaTemplate = config.get("schema-template");
        } else {
            throw new Exception("Missing Schema for publishing aggregated events...");
        }
        printDebugConfig();
        logger.info("Initializing environment completed...");
    }

    private void printDebugConfig() {
        if (logger.isDebugEnabled()) {
            logger.debug("Events-service URL: '" + this.esUrl + "'");
            logger.debug("Events-service Port: '" + this.esPort.toString() + "'");
            logger.debug("Global Account Name: '" + this.globalAccountName + "'");
            logger.debug("API-Key: '" + this.apiKey + "'");
            logger.debug("Accept: '" + this.accept + "'");
            logger.debug("Multiple queries: '" + this.multipleQueries.toString() + "'");
            logger.debug("Content-Type: '" + this.contentType + "'");
            logger.debug("Content-Type JSON: '" + this.contentTypeJson + "'");
            logger.debug("ADQL: \"" + this.adql + "\"");
            logger.debug("Start: \"" + this.startDate + "\"");
            logger.debug("End: \"" + this.endDate + "\"");
            logger.debug("Limit: \"" + this.limit + "\"");
            logger.debug("Mode: \"" + this.mode + "\"");
            logger.debug("Publish Schema: \"" + this.publishSchema + "\"");
        }
    }

    public String runQuery() throws Exception {
        String requestUrl = this.esUrl + ":" + this.esPort.toString() + "/events/query";
        String response = "";
        try {
            StringBuilder urlParameters = new StringBuilder();
            if (this.startDate != null) {
                urlParameters.append("start=");
                urlParameters.append(URLEncoder.encode(this.startDate.toString(), "UTF-8"));
                urlParameters.append("&");
            }
            if (this.endDate != null) {
                urlParameters.append("end=");
                urlParameters.append(URLEncoder.encode(this.endDate.toString(), "UTF-8"));
                urlParameters.append("&");
            }
            if (this.mode.equals("page")) {
                urlParameters.append("mode=");
                urlParameters.append(URLEncoder.encode(this.mode, "UTF-8"));
                urlParameters.append("&");
                urlParameters.append("size=");
                urlParameters.append(URLEncoder.encode(this.count.toString(), "UTF-8"));
                urlParameters.append("&");
                urlParameters.append("offset=");
                urlParameters.append(URLEncoder.encode(this.offset.toString(), "UTF-8"));
                urlParameters.append("&");
            }
            urlParameters.append("limit=");
            urlParameters.append(URLEncoder.encode(this.limit.toString(), "UTF-8"));
            requestUrl = requestUrl + "?" + urlParameters.toString();
            response = sendPostRequest(requestUrl, this.adql, false, true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        //print result
        if (logger.isDebugEnabled()) {
            logger.debug(response);
        }
        if (this.mode.equals("page"))
            this.offset = this.offset + this.count;
        return response;
    }

    public String getSchema() throws Exception {
        logger.info("Retrieving Event Schema...");
        String requestUrl = this.esUrl + ":" + this.esPort.toString() + "/events/schema/" + this.publishSchema;
        String response = null;
        try {
            response = sendGetRequest(requestUrl);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        //print result
        if (logger.isDebugEnabled()) {
            logger.debug(response);
        }
        logger.info("Retrieving Event Schema completed...");
        return response;
    }

    public String createSchema() {
        logger.info("Creating Event Schema...");
        String requestUrl = this.esUrl + ":" + this.esPort.toString() + "/events/schema/" + this.publishSchema;
        String response = "";
        try {

            response = sendPostRequest(requestUrl, this.schemaTemplate, true, true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        //print result
        if (logger.isDebugEnabled()) {
            logger.debug(response);
        }
        logger.info("Creating Event Schema completed...");
        return response;
    }

    public String deleteSchema() {
        logger.info("Deleting Event Schema...");
        String requestUrl = this.esUrl + ":" + this.esPort.toString() + "/events/schema/" + this.publishSchema;
        String response = "";
        try {

            response = sendDeleteRequest(requestUrl);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        //print result
        if (logger.isDebugEnabled()) {
            logger.debug(response);
        }
        logger.info("Deleting Event Schema completed...");
        return response;
    }

    private String sendPostRequest(String requestUrl, String payload, Boolean isPayloadJSON, Boolean isAccept) {
        StringBuffer jsonString = null;
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("X-Events-API-AccountName", globalAccountName);
            connection.setRequestProperty("X-Events-API-Key", apiKey);
            if (isAccept)
                connection.setRequestProperty("Accept", accept);
            if (!isPayloadJSON)
                connection.setRequestProperty("Content-Type", contentType);
            else
                connection.setRequestProperty("Content-Type", contentTypeJson);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writer.write(payload);
            writer.flush();
            writer.close();
            int responseCode = connection.getResponseCode();
            if (logger.isDebugEnabled()) {
                logger.debug("Sending '" + "POST" + "' request to URL: " + url);
                logger.debug("Payload: \"" + payload + "\"");
                logger.debug("Response Code: " + responseCode);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            jsonString = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
            br.close();
            connection.disconnect();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return jsonString.toString();
    }

    private String sendGetRequest(String requestUrl) {
        StringBuffer jsonString = null;
        try {

            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            //connection.setDoInput(true);
            //connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Events-API-AccountName", globalAccountName);
            connection.setRequestProperty("X-Events-API-Key", apiKey);
            connection.setRequestProperty("Accept", accept);
            int responseCode = connection.getResponseCode();
            if (logger.isDebugEnabled()) {
                logger.debug("Sending '" + "GET" + "' request to URL: " + url);
                logger.debug("Response Code: " + responseCode);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            jsonString = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
            br.close();
            connection.disconnect();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return jsonString.toString();
    }

    private String sendDeleteRequest(String requestUrl) {
        StringBuffer jsonString = null;
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("X-Events-API-AccountName", globalAccountName);
            connection.setRequestProperty("X-Events-API-Key", apiKey);
            connection.setRequestProperty("Accept", accept);
            int responseCode = connection.getResponseCode();
            if (logger.isDebugEnabled()) {
                logger.debug("Sending '" + "DELETE" + "' request to URL: " + url);
                logger.debug("Response Code: " + responseCode);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            jsonString = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
            br.close();
            connection.disconnect();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return jsonString.toString();
    }

    public void publishEvents(EventAggregator ea) {
        logger.info("Publishing events...");
        for (EventWorkflow ewf : ea.events) {
            String requestUrl = this.esUrl + ":" + this.esPort.toString() + "/events/publish/" + this.publishSchema;
            String response = "";
            try {
                Gson gson = new Gson();
                JsonArray ja = new JsonArray();
                ja.add(ewf.getJsonObject());
                response = sendPostRequest(requestUrl, gson.toJson(ja), true, false);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            //print result
            if (logger.isDebugEnabled()) {
                logger.debug(response);
            }
        }
        logger.info("Publishing events completed...");
    }
}
