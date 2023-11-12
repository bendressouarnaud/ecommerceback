package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Grossiste;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface GrossisteRepository extends CrudRepository<Grossiste, Long> {
    List<Grossiste> findAllByOrderByDenominationAsc();
    List<Grossiste> findAllByDenomination(String lib);
    Grossiste findByIdgro(Long id);
    Grossiste findByCode(String code);
    Grossiste findByContact(String contact);
    Grossiste findByEmail(String email);
    List<Grossiste> findByDenominationStartsWith(String lib);
    List<Grossiste> findByDenominationIsContaining(String lib);
}