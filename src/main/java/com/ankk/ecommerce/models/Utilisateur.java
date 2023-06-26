package com.ankk.ecommerce.models;

import lombok.Data;
import org.hibernate.annotations.ColumnTransformer;

import javax.persistence.*;

@Data
@Entity(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iduser")
    private Integer iduser;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "email")
    private String email;

    @Column(name = "contact")
    private String contact;

    @Column(name = "profil")
    private Integer profil;

    @ColumnTransformer(
            read = "CONVERT(varchar, DecryptByPassphrase('K8_jemange', identifiant ))",
            write ="ENCRYPTBYPASSPHRASE('K8_jemange',?)"
    )
    @Column(name="identifiant")
    private String identifiant;

    @ColumnTransformer(
            read = "CONVERT(varchar, DecryptByPassphrase('K8_jemange', motdepasse ))",
            write ="ENCRYPTBYPASSPHRASE('K8_jemange',?)"
    )
    @Column(name="motdepasse")
    private String motdepasse;

    @Column(name = "token")
    private String token;

    @Column(name = "fcmtoken")
    private String fcmtoken;

    @Column(name = "ident")
    private Integer ident;

}
