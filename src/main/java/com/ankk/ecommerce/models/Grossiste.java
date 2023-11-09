package com.ankk.ecommerce.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "grossiste")
public class Grossiste {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idgro")
    private Long idgro;

    @Column(name = "denomination")
    private String denomination;

    @Column(name = "contact")
    private String contact;

    @Column(name = "email")
    private String email;

    @Column(name = "code")
    private String code;

}
