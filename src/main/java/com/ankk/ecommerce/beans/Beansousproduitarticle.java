package com.ankk.ecommerce.beans;

import com.ankk.ecommerce.models.Detail;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Beansousproduitarticle {
    String detail;
    int iddet;
    List<Beanresumearticle> liste;

    public Beansousproduitarticle(){
        liste = new ArrayList<>();
    }
}
