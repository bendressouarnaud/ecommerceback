package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Promotion;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PromotionRepository extends CrudRepository<Promotion, Long> {

    Promotion findByIdprn(long id);
    List<Promotion> findAllByIdent(int id);
    List<Promotion> findAllByIdprnIn(List<Long> liste);

}
