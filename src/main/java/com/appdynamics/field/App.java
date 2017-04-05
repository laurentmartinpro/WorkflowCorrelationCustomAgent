package com.appdynamics.field;


import com.appdynamics.field.json.ResponseContainer;
import com.appdynamics.field.utils.EventAggregator;
import com.appdynamics.field.utils.EventContainer;
import com.google.gson.Gson;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.joda.time.DateTime;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Main class
 */
public class App {
    private static final Logger logger = Logger.getLogger(App.class);
    private static String confFile = "conf/config.xml";
    private static DateTime startDate = null;
    private static DateTime endDate = null;
    private static Boolean createSchema = false;
    private static Boolean deleteSchema = false;
    private static Boolean publishEvents = false;
    private static Boolean overrideLimit = true;
    private static Boolean displayHelp = false;

    public static void main(String[] args) {
        try {
            parseArgs(args);
            if (!displayHelp) {
                Map<String, String> config = parseXML(confFile);
                QueryEventsService queryEs = new QueryEventsService();
                queryEs.init(config, startDate, endDate);
                ResponseContainer rc = parseJSON(queryEs.runQuery());
                if (rc.getTotal() > queryEs.getLimit()) {
                    logger.warn("ADQL results items: " + rc.getTotal() + " - With current Limit: " + queryEs.getLimit());
                    if (!overrideLimit & publishEvents)
                        throw new Exception("Cannot publish events: Number of items retrieved by ADQL is too high compared to the current limit.");
                }
                EventContainer ec = new EventContainer(rc, queryEs);
                EventAggregator ea = new EventAggregator(ec);
                if (deleteSchema)
                    queryEs.deleteSchema();
                if (createSchema)
                    queryEs.createSchema();
                if (publishEvents) {
                    ea.buildJsonObject();
                    queryEs.publishEvents(ea);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private static Map<String, String> parseXML(String xml) throws Exception {
        logger.info("Parsing XML configuration file...");
        Map<String, String> map = new HashMap<String, String>();
        try {

            SAXReader reader = new SAXReader();
            Document document = reader.read(xml);
            Element root = document.getRootElement();
            for (Iterator<Element> i = root.elementIterator(); i.hasNext(); ) {
                Element element = i.next();
                map.put(element.getName(), element.getText());
                if (logger.isDebugEnabled()) {
                    logger.debug("Element: '" + element.getName() + "' - Value: '" + element.getText() + "'");
                }
            }
        } catch (DocumentException doc) {
            logger.error("Cannot read or find '" + confFile + "'", doc);
            throw new Exception("Parsing XML configuration error!");
        }
        logger.info("Parsing XML configuration file completed...");
        return map;
    }

    private static ResponseContainer parseJSON(String json) throws Exception {
        logger.info("Parsing JSON response...");
        Gson gson = new Gson();
        ResponseContainer rc = null;
        try {
            rc = gson.fromJson(json, ResponseContainer.class);
        } catch (Exception e) {
            logger.error("Parsing JSON reponse error!");
            throw new Exception("Parsing JSON reponse error!");
        }
        logger.info("Parsing JSON response completed...");
        return rc;
    }

    private static void parseArgs(String[] parameters) throws Exception {
        if (logger.isDebugEnabled())
            logger.debug("Parsing arguments...");
        // create the command line parser
        CommandLineParser parser = new DefaultParser();

        // create the Options
        Options options = new Options();

        options.addOption(Option.builder("c")
                .required(false)
                .longOpt("config-file")
                .desc("[OPTIONAL] Specify this option to choose <file> as the config file to be used")
                .hasArg()
                .argName("file")
                .build());
        options.addOption(Option.builder("s")
                .required(false)
                .longOpt("start")
                .desc("[OPTIONAL] Specify this option to include events newer than <start-date> (ISO 8601)")
                .hasArg()
                .argName("start-date")
                .build());
        options.addOption(Option.builder("e")
                .required(false)
                .longOpt("end")
                .desc("[OPTIONAL] Specify this option to include events older than <end-date> (ISO 8601)")
                .hasArg()
                .argName("end-date")
                .build());
        options.addOption(Option.builder("d")
                .required(false)
                .longOpt("delete-schema")
                .desc("[OPTIONAL] Delete Events Schema")
                .build());
        options.addOption(Option.builder("r")
                .required(false)
                .longOpt("create-schema")
                .desc("[OPTIONAL] Create Events Schema")
                .build());
        options.addOption(Option.builder("p")
                .required(false)
                .longOpt("publish-events")
                .desc("[OPTIONAL] Publish Events")
                .build());
        options.addOption(Option.builder("l")
                .required(false)
                .longOpt("limit")
                .desc("[OPTIONAL] Stop publishing if ADQL retrieves more results than the limit")
                .build());
        options.addOption(Option.builder("h")
                .required(false)
                .longOpt("help")
                .desc("[OPTIONAL] Display help")
                .build());

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, parameters);

            // validate that block-size has been set
            if (line.hasOption("h")) {
                displayHelp = true;
                printUsage(options);
            } else {
                if (line.hasOption("c")) {
                    File f = new File(line.getOptionValue("c"));
                    if (!f.exists()) {
                        throw new Exception("File " + f.getName() + " doesn't exist");
                    }
                    confFile = line.getOptionValue("c");
                    if (logger.isDebugEnabled()) {
                        logger.debug("Using the Config File: " + line.getOptionValue("c"));
                    }
                }
                if (line.hasOption("s")) {
                    startDate = new DateTime(line.getOptionValue("s"));
                    if (logger.isDebugEnabled()) {
                        logger.debug("Using the Start Date: " + line.getOptionValue("s") + " (" + startDate.toString() + ")");
                    }
                }
                if (line.hasOption("e")) {
                    endDate = new DateTime(line.getOptionValue("e"));
                    if (logger.isDebugEnabled()) {
                        logger.debug("Using the End Date: " + line.getOptionValue("e") + " (" + endDate.toString() + ")");
                    }
                }
                if (line.hasOption("d")) {
                    deleteSchema = true;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Deleting Events Schema: " + deleteSchema.toString());
                    }
                }
                if (line.hasOption("r")) {
                    createSchema = true;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Creating Events Schema: " + deleteSchema.toString());
                    }
                }
                if (line.hasOption("p")) {
                    publishEvents = true;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Publishing Events: " + publishEvents.toString());
                    }
                }
                if (line.hasOption("l")) {
                    overrideLimit = false;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Override Limit: " + overrideLimit.toString());
                    }
                }
            }
        } catch (ParseException exp) {
            logger.error("Unexpected exception:" + exp.getMessage());
            printUsage(options);
            throw new Exception("Parsing arguments error!");
        }
        if (logger.isDebugEnabled())
            logger.debug("Parsing arguments completed...");
    }

    private static void printUsage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("WorkflowCorrelationCustomAgent", options);
    }
}
