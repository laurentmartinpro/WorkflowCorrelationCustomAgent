package com.appdynamics.field.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Field {

    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("field")
    @Expose
    private String field;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("aggregation")
    @Expose
    private Object aggregation;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Field withLabel(String label) {
        this.label = label;
        return this;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Field withField(String field) {
        this.field = field;
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Field withType(String type) {
        this.type = type;
        return this;
    }

    public Object getAggregation() {
        return aggregation;
    }

    public void setAggregation(Object aggregation) {
        this.aggregation = aggregation;
    }

    public Field withAggregation(Object aggregation) {
        this.aggregation = aggregation;
        return this;
    }

}