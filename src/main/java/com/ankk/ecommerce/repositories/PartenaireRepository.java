package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Partenaire;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PartenaireRepository extends CrudRepository<Partenaire, Integer> {
    List<Partenaire> findAllByOrderByLibelleAsc();
    Partenaire findByIdent(int id);
    Partenaire findByContact(String contact);
}
