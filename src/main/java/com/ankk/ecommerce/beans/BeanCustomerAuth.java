package com.ankk.ecommerce.beans;

import com.ankk.ecommerce.models.Client;
import com.ankk.ecommerce.models.Commune;
import lombok.Data;

import java.util.List;

@Data
public class BeanCustomerAuth {
    int flag;
    Client clt;
    List<Commune> commune;
}
