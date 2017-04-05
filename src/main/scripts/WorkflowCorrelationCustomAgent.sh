#!/bin/sh -x
java -Xms256M -Xmx256M -Dlog4j.configuration=file:conf/log4j.properties -jar bin/WorkflowCorrelationCustomAgent.jar "$@"