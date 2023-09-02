package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Commentaire;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommentaireRepository extends CrudRepository<Commentaire, Long> {

    List<Commentaire> findAllByIdart(int id);
    List<Commentaire> findAllByIdartAndIdcli(int id, int idcli);

}
