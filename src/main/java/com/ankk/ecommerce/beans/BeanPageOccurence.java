package com.ankk.ecommerce.beans;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public class BeanPageOccurence {
    int page;
    List<BeanLigneOccurence> donnee;

    public BeanPageOccurence() {
        donnee = new ArrayList<>();
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<BeanLigneOccurence> getDonnee() {
        return donnee;
    }

    public void setDonnee(List<BeanLigneOccurence> donnee) {
        this.donnee = donnee;
    }
}
