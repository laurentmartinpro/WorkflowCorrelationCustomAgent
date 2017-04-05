package com.appdynamics.field.utils;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

/**
 * Created by laurent.martin on 4/3/17.
 */
public class EventEntity {
    private static final Logger logger = Logger.getLogger(EventEntity.class);
    private String appName = "";
    private String btName = "";
    private DateTime eventTime = null;
    private String reqGUID = "";
    private String commonId = "";
    private Float responseTime = 0f;
    private String userExperience = "";
    private EventEntity sameFlowEntity = null;
    public EventEntity() {

    }

    public static Boolean hasSameCommonId(EventEntity e1, EventEntity e2) {
        return e1.getCommonId().equals(e2.getCommonId());
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getBtName() {
        return this.btName;
    }

    public void setBtName(String btName) {
        this.btName = btName;
    }

    public DateTime getEventTime() {
        return this.eventTime;
    }

    public void setEventTime(DateTime eventTime) {
        this.eventTime = eventTime;
    }

    public String getReqGUID() {
        return this.reqGUID;
    }

    public void setReqGUID(String reqGUID) {
        this.reqGUID = reqGUID;
    }

    public String getCommonId() {
        return commonId;
    }

    public void setCommonId(String commonId) {
        this.commonId = commonId;
    }

    public Float getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Float responseTime) {
        this.responseTime = responseTime;
    }

    public String getUserExperience() {
        return userExperience;
    }

    public void setUserExperience(String userExperience) {
        this.userExperience = userExperience;
    }

    public Boolean hasSameFlowEntity() {
        return (this.sameFlowEntity != null);
    }

    public EventEntity getSameFlowEntity() {
        return sameFlowEntity;
    }

    public void setSameFlowEntity(EventEntity sameFlowEntity) {
        this.sameFlowEntity = sameFlowEntity;
    }

    public void printDebug() {
        if (logger.isDebugEnabled()) {
            logger.debug("AppName: '" + this.getAppName() + "' - BTName: '" + this.getBtName() + "' - EventTime: '" + this.getEventTime().toString() + "' - ReqGUID: '" + this.getReqGUID() + "' - CommonId: '" + this.getCommonId() + "' - ResponseTime: '" + this.getResponseTime().toString() + "ms' - UserExperience: '" + this.getUserExperience() + "'");
        }
    }

}
