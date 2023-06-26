package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Profil;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProfilRepository extends CrudRepository<Profil, Integer> {

    List<Profil> findAllByOrderByLibelleAsc();
    List<Profil> findAllByIdproNotOrderByLibelleAsc(int idpro);
    List<Profil> findAllByIdproIn(List<Integer> idpro);
    Profil findByIdpro(int idpro);
    Profil findByLibelle(String libelle);

}
