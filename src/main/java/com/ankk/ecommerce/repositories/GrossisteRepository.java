package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Grossiste;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GrossisteRepository extends CrudRepository<Grossiste, Long> {
    List<Grossiste> findAllByOrderByDenominationAsc();
    List<Grossiste> findAllByDenomination(String lib);
    Grossiste findByIdgro(Long id);
    Grossiste findByContact(String contact);
    List<Grossiste> findByDenominationStartsWith(String lib);
    List<Grossiste> findByDenominationIsContaining(String lib);
}