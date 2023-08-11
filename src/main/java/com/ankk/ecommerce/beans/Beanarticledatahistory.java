package com.ankk.ecommerce.beans;

import com.ankk.ecommerce.models.Commentaire;
import com.ankk.ecommerce.models.Imagesupplement;
import lombok.Data;

import java.util.List;

@Data
public class Beanarticledatahistory {

    List<Imagesupplement> images;
    List<Commentaire> comments;
    String article, entreprise, modaliteretour, descriptionproduit;
    int prix, reduction, nombrearticle;

}
