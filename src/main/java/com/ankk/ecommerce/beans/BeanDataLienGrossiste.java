package com.ankk.ecommerce.beans;

import lombok.Data;

@Data
public class BeanDataLienGrossiste {
    String grossiste, article;
    int idart,prix, prixforfait;
    Long idgro, idlgo;
}
