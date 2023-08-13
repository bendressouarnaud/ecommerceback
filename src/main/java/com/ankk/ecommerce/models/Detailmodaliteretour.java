package com.ankk.ecommerce.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "detailmodaliteretour")
public class Detailmodaliteretour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddtr")
    private Integer iddtr;

    @Column(name = "commentaire")
    private String commentaire;

    @Column(name = "iddet")
    private Integer iddet;

    @Column(name = "ident")
    private Integer ident;

}
