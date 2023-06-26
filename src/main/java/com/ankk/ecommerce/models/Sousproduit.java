package com.ankk.ecommerce.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "sousproduit")
public class Sousproduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idspr")
    private Integer idspr;

    @Column(name = "libelle")
    private String libelle;

    @Column(name = "lienweb")
    private String lienweb;

    @Column(name = "idprd")
    private Integer idprd;

}
