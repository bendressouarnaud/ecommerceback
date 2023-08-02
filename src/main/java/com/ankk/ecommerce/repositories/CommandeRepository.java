package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Commande;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommandeRepository extends CrudRepository<Commande, Integer> {

    List<Commande> findAllByEtat(int etat);

}
