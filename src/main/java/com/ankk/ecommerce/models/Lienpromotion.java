package com.ankk.ecommerce.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "lienpromotion")
public class Lienpromotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idlpr")
    private Long idlpr;

    @Column(name = "idpro")
    private int idpro;

    @Column(name = "idart")
    private int idart;

}
