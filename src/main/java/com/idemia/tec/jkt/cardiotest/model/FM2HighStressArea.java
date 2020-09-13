package com.idemia.tec.jkt.cardiotest.model;

import javafx.beans.property.SimpleStringProperty;

public class FM2HighStressArea {

    private SimpleStringProperty path_HighStressArea;

    public FM2HighStressArea() {}

    public FM2HighStressArea(String path_HighStressArea) {
        this.path_HighStressArea = new SimpleStringProperty (path_HighStressArea);
    }

    public String getPath_HighStressArea() {
        return path_HighStressArea.get();
    }

    public void setPath_HighStressArea(String path_HighStressArea) {
        this.path_HighStressArea.set(path_HighStressArea);
    }

}
