package com.ankk.ecommerce.models;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity(name = "article")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idart")
    private Integer idart;

    @Column(name = "libelle")
    private String libelle;

    @Column(name = "prix")
    private Integer prix;

    @Column(name = "publication")
    private Date publication;

    @Column(name = "ident")
    private Integer ident;

    @Column(name = "detail")
    private String detail;

    @Column(name = "lienweb")
    private String lienweb;

    @Column(name = "idspr")
    private Integer idspr;
}
