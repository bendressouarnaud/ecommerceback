package com.ankk.ecommerce.beans;

import lombok.Data;

@Data
public class BeanLibImg {
    String libelle, image;

    public BeanLibImg(String libelle, String image) {
        this.libelle = libelle;
        this.image = image;
    }
}
