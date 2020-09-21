package com.idemia.tec.jkt.cardiotest.model;

import com.google.gson.Gson;
import javafx.beans.property.SimpleStringProperty;

public class AppletParam {


    private SimpleStringProperty packageAid, instanceAid, lifeCycle;
    private Boolean includeVerifGp;

    public AppletParam() {}

    public AppletParam(String packageAid, String instanceAid, String lifeCycle) {
        this.packageAid = new SimpleStringProperty(packageAid);
        this.instanceAid = new SimpleStringProperty(instanceAid);
        this.lifeCycle = new SimpleStringProperty(lifeCycle);
    }

    public String getPackageAid() {
        return packageAid.get();
    }

    public void setPackageAid(String packageAid) {
        this.packageAid = new SimpleStringProperty(packageAid);
    }

    public String getInstanceAid() {
        return instanceAid.get();
    }

    public void setInstanceAid(String instanceAid) {
        this.instanceAid = new SimpleStringProperty(instanceAid);
    }

    public String getLifeCycle() {
        return lifeCycle.get();
    }

    public void setLifeCycle(String lifeCycle) {
        this.lifeCycle = new SimpleStringProperty(lifeCycle);
    }

    public SimpleStringProperty packageAidProperty() {
        return packageAid;

    }public SimpleStringProperty instanceAidProperty() {
        return instanceAid;

    }public SimpleStringProperty lifeCycleProperty() {
        return lifeCycle;
    }



}
