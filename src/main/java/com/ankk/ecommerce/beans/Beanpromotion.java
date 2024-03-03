package com.ankk.ecommerce.beans;

import lombok.Data;

@Data
public class Beanpromotion {
    long idprn;
    int reduction, modepourcentage, prix;
    String libelle, datedebut,datefin;
}
