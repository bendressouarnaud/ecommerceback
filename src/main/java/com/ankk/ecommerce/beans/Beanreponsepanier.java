package com.ankk.ecommerce.beans;

import lombok.Data;

@Data
public class Beanreponsepanier {
    String lienweb, libelle;
    int idart, totalcomment, restant, prix, reduction;
    double note;
}
