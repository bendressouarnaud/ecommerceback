package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Grossiste;
import com.ankk.ecommerce.models.Liengrossiste;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface LiengrossisteRepository extends CrudRepository<Liengrossiste, Long> {

    Optional<Liengrossiste> findByIdartAndIdgroAndIdent(int idart, int idgro, int ident);
    Optional<Liengrossiste> findByIdlgo(long idlgo);
    List<Liengrossiste> findAllByIdent(int ident);

}
