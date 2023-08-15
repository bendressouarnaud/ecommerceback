package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Lienpromotion;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LienpromotionRepository extends CrudRepository<Lienpromotion, Long> {

    Lienpromotion findByIdartAndIdpro(int idart, int idpro);
    List<Lienpromotion> findAllByIdart(int idart);

}
