package com.appdynamics.field.utils;

import com.appdynamics.field.QueryEventsService;
import com.appdynamics.field.json.ResponseContainer;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by laurent.martin on 4/3/17.
 */
public class EventContainer {
    private static final Logger logger = Logger.getLogger(EventContainer.class);
    public List<EventEntity> eventEntities = new ArrayList<EventEntity>();
    Integer fieldAppNameId = 0;
    Integer fieldBtNameId = 0;
    Integer fieldEventTime = 0;
    Integer fieldGuid = 0;
    Integer fieldCommonId = 0;
    Integer fieldResponseTime = 0;
    Integer fieldUserExperience = 0;

    public EventContainer(ResponseContainer rc, QueryEventsService qes) {
        logger.info("Creating Event Container...");
        this.fieldAppNameId = rc.getFieldId(qes.getFieldAppName());
        this.fieldBtNameId = rc.getFieldId(qes.getFieldBtName());
        this.fieldEventTime = rc.getFieldId(qes.getFieldEventTime());
        this.fieldGuid = rc.getFieldId(qes.getFieldGuid());
        this.fieldCommonId = rc.getFieldId(qes.getFieldCommonId());
        this.fieldResponseTime = rc.getFieldId(qes.getFieldResponseTime());
        this.fieldUserExperience = rc.getFieldId(qes.getFieldUserExperience());
        logger.info("Creating Event Container completed...");
        addResults(rc, qes);

    }

    public void addResults(ResponseContainer rc, QueryEventsService qes) {
        logger.info("Adding Results to Event Container...");
        for (List<Object> results : rc.getResults()) {
            EventEntity eventEntity = new EventEntity();
            Integer i = 1;
            for (Object item : results) {
                if (i == this.fieldAppNameId) {
                    eventEntity.setAppName(item.toString());
                }
                if (i == this.fieldBtNameId) {
                    eventEntity.setBtName(item.toString());
                }
                if (i == this.fieldEventTime) {
                    eventEntity.setEventTime(new DateTime(item));
                }
                if (i == this.fieldGuid) {
                    eventEntity.setReqGUID(item.toString());
                }
                if (i == this.fieldCommonId) {
                    ArrayList<Object> tmp = (ArrayList<Object>) item;
                    eventEntity.setCommonId(tmp.get(0).toString());
                }
                if (i == this.fieldResponseTime) {
                    eventEntity.setResponseTime(Float.valueOf(item.toString()));
                }
                if (i == this.fieldUserExperience) {
                    eventEntity.setUserExperience(item.toString());
                }
                i++;
            }
            eventEntities.add(eventEntity);
        }
        printDebug();
        logger.info("Adding Results to Event Container completed...");
    }

    private void printDebug() {
        if (logger.isDebugEnabled()) {
            for (EventEntity eventEntity : this.eventEntities) {
                eventEntity.printDebug();
            }
        }
    }
}
