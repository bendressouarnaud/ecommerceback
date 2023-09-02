package com.ankk.ecommerce.beans;

import lombok.Data;

@Data
public class BeanCommentRequest {
    int note, idcli, idart;
    String commentaire;
}
