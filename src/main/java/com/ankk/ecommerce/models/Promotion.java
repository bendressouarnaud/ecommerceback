package com.ankk.ecommerce.models;


import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity(name = "promotion")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idprn")
    private Long idprn;

    @Column(name = "ident")
    private int ident;

    @Column(name = "libelle")
    private String libelle;

    @Column(name = "reduction")
    private int reduction;

    @Column(name = "datedebut")
    private Date datedebut;

    @Column(name = "datefin")
    private Date datefin;

    @Column(name = "etat")
    private int etat;

}
