package com.ankk.ecommerce.models;

import javax.persistence.*;

@Entity(name="parametres")
public class Parametres {

    @Id
    @Column(name="idparam")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idparam;

    @Column(name="alertemail")
    private int alertemail;

    public Parametres() {
    }

    public int getIdparam() {
        return idparam;
    }

    public void setIdparam(int idparam) {
        this.idparam = idparam;
    }

    public int getAlertemail() {
        return alertemail;
    }

    public void setAlertemail(int alertemail) {
        this.alertemail = alertemail;
    }
}
