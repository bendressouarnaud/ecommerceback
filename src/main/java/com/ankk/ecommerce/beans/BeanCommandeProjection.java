package com.ankk.ecommerce.beans;

import lombok.Data;

@Data
public class BeanCommandeProjection {
    int iduser, nbrearticle, traites, demandeconfirme, demandeorigine, montant, emissions, livres;
    String dates, heure;
}
