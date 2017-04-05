package com.appdynamics.field.utils;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by laurent.martin on 4/4/17.
 */
public class EventWorkflow {
    private static final Logger logger = Logger.getLogger(EventAggregator.class);

    public List<EventEntity> eventEntities = new ArrayList<EventEntity>();
    public String commonId = null;
    public String userExperience = "NORMAL";
    public EventEntity startEntity = null;
    public EventEntity endEntity = null;
    private JsonObject jsonObject = null;

    public JsonObject getJsonObject() {
        return jsonObject;
    }

    public List<EventEntity> getEventEntities() {
        return eventEntities;
    }

    public void setEventEntities(List<EventEntity> eventEntities) {
        this.eventEntities = eventEntities;
    }

    public void addEventEntity(EventEntity ee) {

        this.eventEntities.add(ee);
        if (!ee.getUserExperience().equals("NORMAL") && !this.userExperience.equals("ERROR"))
            this.userExperience = ee.getUserExperience();
        if (this.startEntity == null) {
            this.startEntity = ee;
        } else {
            if (this.startEntity.getEventTime().isAfter(ee.getEventTime())) {
                this.startEntity = ee;
            }
        }
        if (this.endEntity == null) {
            this.endEntity = ee;
        } else {
            if (this.endEntity.getEventTime().isBefore(ee.getEventTime())) {
                this.endEntity = ee;
            }
        }
    }

    public String getCommonId() {
        return commonId;
    }

    public void setCommonId(String commonId) {
        this.commonId = commonId;
    }


    public void printDebug() {
        if (logger.isDebugEnabled()) {
            logger.debug("Start GUID: " + this.startEntity.getReqGUID());
            logger.debug("End GUID: " + this.endEntity.getReqGUID());
            logger.debug("Common Id: '" + this.commonId + "' - IsWorkflow: '" + isWorkflow().toString() + "' - Timing: '" + getTimingWorkflowInMillis().toString() + "ms' - FullTiming (Including Response Time of End): '" + getFullTimingWorkflowInMillis().toString() + "ms'");
            for (EventEntity ee : this.eventEntities) {
                ee.printDebug();
            }
        }
    }

    public Boolean isWorkflow() {
        Boolean res = false;
        try {
            if ((startEntity != null & endEntity != null) & (!startEntity.getReqGUID().equals(endEntity.getReqGUID())))
                res = true;
        } catch (NullPointerException e) {
            logger.error(e.getMessage());
        }
        return res;
    }

    public Long getTimingWorkflowInMillis() {
        if (isWorkflow())
            return endEntity.getEventTime().getMillis() - startEntity.getEventTime().getMillis();
        else
            return -1L;
    }

    public Long getFullTimingWorkflowInMillis() {
        if (isWorkflow())
            return (endEntity.getEventTime().getMillis() + endEntity.getResponseTime().longValue()) - startEntity.getEventTime().getMillis();
        else
            return -1L;
    }

    public void buildJsonObject() {
        this.jsonObject = new JsonObject();
        this.jsonObject.addProperty("application", this.startEntity.getAppName());
        this.jsonObject.addProperty("startTransactionName", this.startEntity.getBtName());
        this.jsonObject.addProperty("commonId", this.getCommonId());
        this.jsonObject.addProperty("workflowBT", this.eventEntities.size());
        this.jsonObject.addProperty("eventTimestamp", this.startEntity.getEventTime().toString());
        if (getFullTimingWorkflowInMillis() == -1L) {
            this.jsonObject.addProperty("userExperience", "NOT_COMPLETED_YET");
            this.jsonObject.addProperty("pickupTimestamp", this.startEntity.getEventTime().toString());
        } else {
            this.jsonObject.addProperty("userExperience", this.userExperience);
            this.jsonObject.addProperty("endTransactionName", this.endEntity.getBtName());
            this.jsonObject.addProperty("timing", this.getFullTimingWorkflowInMillis());
            this.jsonObject.addProperty("pickupTimestamp", this.endEntity.getEventTime().plusMillis(this.endEntity.getResponseTime().intValue()).toString());
        }
        /*
        if (logger.isDebugEnabled()) {
            logger.debug(this.jsonObject.toString());
        }*/
    }
}
