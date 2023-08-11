package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Imagesupplement;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ImagesupplementRepository extends CrudRepository<Imagesupplement, Long> {

    List<Imagesupplement> findAllByIdart(int id);

}
