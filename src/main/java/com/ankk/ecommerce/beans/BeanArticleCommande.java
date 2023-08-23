package com.ankk.ecommerce.beans;

import lombok.Data;

@Data
public class BeanArticleCommande {

    String lien, libelle;
    int total, prix, disponibilite;

}
