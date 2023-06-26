package com.ankk.ecommerce.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "profil")
public class Profil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idpro")
    private Integer idpro;

    @Column(name = "libelle")
    private String libelle;

    @Column(name = "code")
    private String code;
}
