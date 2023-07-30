package com.ankk.ecommerce.repositories;
import com.ankk.ecommerce.models.Commune;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommuneRepository extends CrudRepository<Commune, Integer> {

    List<Commune> findAllByOrderByLibelleAsc();

}
