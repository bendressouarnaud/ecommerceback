package com.ankk.ecommerce.beans;

import lombok.Data;

@Data
public class BeanArticleDiscounted {
    int idart;
    String libelle;
    String lienweb;
    int modepourcentage;
    int reduction;
}
