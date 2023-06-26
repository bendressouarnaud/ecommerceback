package com.ankk.ecommerce.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "produit")
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idprd")
    private Integer idprd;

    @Column(name = "libelle")
    private String libelle;

    @Column(name = "lienweb")
    private String lienweb;

}
