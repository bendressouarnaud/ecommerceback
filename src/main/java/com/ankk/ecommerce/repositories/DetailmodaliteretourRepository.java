package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Detailmodaliteretour;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DetailmodaliteretourRepository extends
        CrudRepository<Detailmodaliteretour, Integer> {

    List<Detailmodaliteretour> findAllByIdent(int id);

}
