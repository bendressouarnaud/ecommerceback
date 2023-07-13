package com.ankk.ecommerce.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "achat")
public class Achat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idach")
    private Integer idach;

    @Column(name = "idart")
    private Integer idart;

    @Column(name = "actif")
    private Integer actif;
    // 1 : old article bought. This field is set to 1 when NEW STOCK is available
    // 0 : the ongoing stock

}
