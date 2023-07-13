package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Achat;
import com.ankk.ecommerce.models.Article;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AchatRepository extends CrudRepository<Achat, Integer> {

    List<Achat> findAllByIdart(int idart);
    List<Achat> findAllByIdartAndActif(int idart, int actif);

}