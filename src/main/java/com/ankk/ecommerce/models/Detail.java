package com.ankk.ecommerce.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "detail")
public class Detail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddet")
    private Integer iddet;

    @Column(name = "libelle")
    private String libelle;

    @Column(name = "lienweb")
    private String lienweb;

    @Column(name = "idspr")
    private Integer idspr;

}
