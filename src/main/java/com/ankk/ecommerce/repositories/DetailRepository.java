package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Detail;
import com.ankk.ecommerce.models.Sousproduit;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DetailRepository extends CrudRepository<Detail, Integer> {
    List<Detail> findAll();
    List<Detail> findAllByIdspr(int id);
    List<Detail> findAllByIdsprIn(List<Integer> listIdsp);
    Detail findByIddet(int id);
}
