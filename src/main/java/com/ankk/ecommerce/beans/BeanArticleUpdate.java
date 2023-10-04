package com.ankk.ecommerce.beans;

import lombok.Data;

import java.util.List;

@Data
public class BeanArticleUpdate {
    int quantite, actif, taille;
    List<Beanpromotion> promotion;
}
