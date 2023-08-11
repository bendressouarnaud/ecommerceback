package com.ankk.ecommerce.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "imagesupplement")
public class Imagesupplement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idims")
    private Long idims;

    @Column(name = "lienweb")
    private String lienweb;

    @Column(name = "idart")
    private Integer idart;

    public Imagesupplement(Long idims, String lienweb, Integer idart) {
        this.idims = idims;
        this.lienweb = lienweb;
        this.idart = idart;
    }

    public Imagesupplement() {
    }
}
