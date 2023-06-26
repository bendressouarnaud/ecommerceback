package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Produit;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProduitRepository extends CrudRepository<Produit, Integer> {

    Produit findByIdprd(int id);
    List<Produit> findAll();

}
