package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Parametres;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ParametresRepository extends CrudRepository<Parametres, Integer> {

    List<Parametres> findAll();
    Parametres findByIdparam(int idparam);

}
