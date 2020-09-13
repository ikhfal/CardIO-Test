package com.idemia.tec.jkt.cardiotest.model;

import javafx.beans.property.SimpleStringProperty;

public class FMRuwi {

    private SimpleStringProperty path_Ruwi;

    public FMRuwi() {}

    public FMRuwi(String path_Ruwi) {
        this.path_Ruwi = new SimpleStringProperty (path_Ruwi);
    }

    public String getPath_Ruwi() {
        return path_Ruwi.get();
    }

    public SimpleStringProperty path_RuwiProperty() {
        return path_Ruwi;
    }

    public void setPath_Ruwi(String path_Ruwi) {
        this.path_Ruwi.set(path_Ruwi);
    }
}
