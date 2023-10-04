package com.ankk.ecommerce.beans;

import com.ankk.ecommerce.models.Commentaire;
import com.ankk.ecommerce.models.Imagesupplement;
import lombok.Data;

import java.util.List;

@Data
public class Beanarticledatahistory {

    List<Imagesupplement> images;
    List<BeanCommentaireContenu> comments;
    String article, entreprise, modaliteretour, descriptionproduit, contact;
    int prix, reduction, nombrearticle, autorisecommentaire, commentaireexiste;
    int iddet, note;
    int trackVetement, taille;
}
