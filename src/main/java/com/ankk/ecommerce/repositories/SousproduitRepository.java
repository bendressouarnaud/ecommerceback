package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Sousproduit;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SousproduitRepository extends CrudRepository<Sousproduit, Integer> {
    List<Sousproduit> findAll();
    List<Sousproduit> findAllByIdprd(int id);
    Sousproduit findByIdspr(int id);
    Sousproduit findByLibelle(String lib);
}
