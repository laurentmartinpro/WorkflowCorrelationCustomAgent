package com.appdynamics.field.utils;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by laurent.martin on 4/4/17.
 */
public class EventAggregator {
    private static final Logger logger = Logger.getLogger(EventAggregator.class);
    public List<EventWorkflow> events = new ArrayList<EventWorkflow>();


    public EventAggregator(EventContainer ec) {
        logger.info("Creating Event Aggregator...");
        for (EventEntity ee1 : ec.eventEntities) {
            if (ee1.getSameFlowEntity() == null) {
                EventWorkflow res = new EventWorkflow();
                res.addEventEntity(ee1);
                res.setCommonId(ee1.getCommonId());
                for (EventEntity ee2 : ec.eventEntities) {
                    if ((ee2.getSameFlowEntity() == null) & (!ee1.getReqGUID().equals(ee2.getReqGUID())) & EventEntity.hasSameCommonId(ee1, ee2)) {
                        ee2.setSameFlowEntity(ee1);
                        res.addEventEntity(ee2);
                    }
                }
                events.add(res);
            }
        }
        printDebug();
        logger.info("Creating Event Aggregator completed...");
    }

    private void printDebug() {
        if (logger.isDebugEnabled()) {
            logger.debug("EventAggregator Print Debug");
            for (EventWorkflow eeList : this.events) {
                eeList.printDebug();
            }
        }
    }

    public void buildJsonObject() {
        for (EventWorkflow ewf : this.events) {
            ewf.buildJsonObject();
        }
    }
}
