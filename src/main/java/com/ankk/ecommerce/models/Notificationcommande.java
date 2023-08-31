package com.ankk.ecommerce.models;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity(name = "notificationcommande")
public class Notificationcommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnot")
    private Long idnot;

    @Column(name = "dates")
    private Date dates;

    @Column(name = "heure")
    private String heure;

    @Column(name = "statut")
    private Integer statut;

    @Column(name = "idcli")
    private Integer idcli;

}
