package com.ankk.ecommerce.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "partenaire")
public class Partenaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ident")
    private Integer ident;

    @Column(name = "libelle")
    private String libelle;

    @Column(name = "contact")
    private String contact;

    @Column(name = "email")
    private String email;

}
