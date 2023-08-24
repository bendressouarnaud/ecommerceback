package com.ankk.ecommerce.models;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity(name = "commande")
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcde")
    private Integer idcde;

    @Column(name = "idart")
    private Integer idart;

    @Column(name = "dates")
    private Date dates;

    @Column(name = "heure")
    private String heure;

    @Column(name = "iduser")
    private Integer iduser;

    @Column(name = "prix")
    private Integer prix;

    @Column(name = "total")
    private Integer total;

    @Column(name = "disponible")
    private Integer disponible;

    @Column(name = "etat")
    private Integer etat;

    @Column(name = "traite")
    private Integer traite;
}
