package com.ankk.ecommerce.beans;

import lombok.Data;

import java.util.List;

@Data
public class BeanArticleHistoCommande {
    int totalarticle, totalprix;
    List<Beanresumearticle> listearticle;
}
