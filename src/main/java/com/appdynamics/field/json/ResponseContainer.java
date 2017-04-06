package com.appdynamics.field.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ResponseContainer {

    @SerializedName("fields")
    @Expose
    private List<Field> fields = new ArrayList<Field>();
    @SerializedName("total")
    @Expose
    private long total;
    @SerializedName("results")
    @Expose
    private List<List<Object>> results = new ArrayList<List<Object>>();
    @SerializedName("moreData")
    @Expose
    private Boolean moreData = false;
    @SerializedName("schema")
    @Expose
    private String schema;

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public ResponseContainer withFields(List<Field> fields) {
        this.fields = fields;
        return this;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public ResponseContainer withTotal(long total) {
        this.total = total;
        return this;
    }

    public List<List<Object>> getResults() {
        return results;
    }

    public void setResults(List<List<Object>> results) {
        this.results = results;
    }

    public ResponseContainer withResults(List<List<Object>> results) {
        this.results = results;
        return this;
    }

    public Boolean getMoreData() {
        return moreData;
    }

    public void setMoreData(Boolean moreData) {
        this.moreData = moreData;
    }

    public ResponseContainer withMoreData(Boolean moreData) {
        this.moreData = moreData;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public ResponseContainer withSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public Integer getFieldId(String key) {
        Integer result = 0;
        Integer i = 0;
        for (Field field : this.getFields()) {
            ++i;
            if (key.equals(field.getLabel()) | key.equals(field.getField())) {
                result = i;
            }
        }
        return result;
    }

}