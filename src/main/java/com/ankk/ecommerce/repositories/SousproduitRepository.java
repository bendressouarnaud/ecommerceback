package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Produit;
import com.ankk.ecommerce.models.Sousproduit;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SousproduitRepository extends CrudRepository<Sousproduit, Integer> {
    List<Sousproduit> findAll();
    List<Sousproduit> findAllByIdprd(int id);
    List<Sousproduit> findAllByIdprdIn(List<Integer> liste);
    Sousproduit findByIdspr(int id);
    Sousproduit findByLibelle(String lib);
    List<Sousproduit> findByLibelleStartsWith(String lib);
    List<Sousproduit> findByLibelleOrderByLibelleAsc(String lib);
}
