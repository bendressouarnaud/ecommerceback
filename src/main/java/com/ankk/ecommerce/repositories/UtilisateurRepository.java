package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Utilisateur;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UtilisateurRepository extends CrudRepository<Utilisateur, Integer> {

    Utilisateur findByIduser(int iduser);
    Utilisateur findByIdentifiant(String identifiant);
    Utilisateur findByEmail(String email);
    Utilisateur findByIdentifiantAndMotdepasse(String id, String pwd);
    List<Utilisateur> findAllByProfilOrderByNomAsc(int profil);
    List<Utilisateur> findAllByProfilNotAndIdentOrderByNomAsc(int profil, int id);
    List<Utilisateur> findAllByOrderByNomAsc();
    //List<Utilisateur> findAllByIdmaiOrderByNomAsc(int idmai);
    List<Utilisateur> findAllByEmail(String email);
    Utilisateur findByToken(String token);

    // Get History of TODAY :
    @Query(value = "SELECT * FROM Utilisateur j WHERE DATALENGTH(j.fcmtoken) > 0 and j.iduser <> ?1",
            nativeQuery = true)
    List<Utilisateur> findAllUsersWithNoFcmtoken(int iduser);
}
