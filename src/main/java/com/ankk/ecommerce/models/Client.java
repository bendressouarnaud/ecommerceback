package com.ankk.ecommerce.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcli")
    private Integer idcli;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "email")
    private String email;

    @Column(name = "numero")
    private String numero;

    @Column(name = "commune")
    private Integer commune;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "genre")
    private Integer genre;

    @Column(name = "fcmtoken")
    private String fcmtoken;
}
