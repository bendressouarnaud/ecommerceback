package com.ankk.ecommerce.beans;

public class Reponse {
    String element, profil, identifiant;

    public Reponse(String element) {
        this.element = element;
    }

    public Reponse() {
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public String getProfil() {
        return profil;
    }

    public void setProfil(String profil) {
        this.profil = profil;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }
}
