package com.ankk.ecommerce.beans;

import com.ankk.ecommerce.models.Detail;

import java.util.ArrayList;
import java.util.List;

public class Beancategorie {

    // Attributes :
    String sousproduit;
    List<Detail> details;

    // Methods
    public Beancategorie() {
        details = new ArrayList<>();
    }

    public String getSousproduit() {
        return sousproduit;
    }

    public void setSousproduit(String sousproduit) {
        this.sousproduit = sousproduit;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public void setDetails(List<Detail> details) {
        this.details = details;
    }
}
