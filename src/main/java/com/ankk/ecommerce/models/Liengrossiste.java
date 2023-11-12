package com.ankk.ecommerce.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "liengrossiste")
public class Liengrossiste {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idlgo")
    private Long idlgo;

    @Column(name = "idart")
    private Integer idart;

    @Column(name = "idgro")
    private Integer idgro;

    @Column(name = "ident")
    private Integer ident;

    @Column(name = "prixforfait")
    private Integer prixforfait;
}
