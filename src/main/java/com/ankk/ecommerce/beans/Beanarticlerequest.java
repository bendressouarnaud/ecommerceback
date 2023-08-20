package com.ankk.ecommerce.beans;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Beanarticlerequest {
    // A T T R I B U T E S :
    int idcli, choixpaiement;
    List<BeanActif> liste;
}
