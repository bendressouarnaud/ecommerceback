package com.ankk.ecommerce.beans;

import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BeanProcessMail {
    int ident;
    Set<Integer> idart;
    Set<BeanLibImg> lienweb;

    public BeanProcessMail() {
        idart = new HashSet<>();
        lienweb = new HashSet<>();
    }

    public int getIdent() {
        return ident;
    }

    public Set<Integer> getIdart() {
        return idart;
    }

    public Set<BeanLibImg> getLienweb() {
        return lienweb;
    }

    public void setIdent(int ident) {
        this.ident = ident;
    }
}
