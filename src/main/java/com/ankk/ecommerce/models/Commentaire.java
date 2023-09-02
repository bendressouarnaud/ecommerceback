package com.ankk.ecommerce.models;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity(name = "commentaire")
public class Commentaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcmt")
    private Long idcmt;

    @Column(name = "appreciation")
    private String appreciation;

    @Column(name = "note")
    private int note;

    @Column(name = "commentaire")
    private String commentaire;

    @Column(name = "dates")
    private Date dates;

    @Column(name = "heure")
    private String heure;

    @Column(name = "idcli")
    private int idcli;

    @Column(name = "idart")
    private int idart;
}
