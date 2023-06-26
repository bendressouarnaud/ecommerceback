package com.ankk.ecommerce.securite;

import com.ankk.ecommerce.models.Profil;
import com.ankk.ecommerce.models.Utilisateur;
import com.ankk.ecommerce.repositories.ProfilRepository;
import com.ankk.ecommerce.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import java.util.List;

@Service
public class UserDetailsServiceImp implements UserDetailsService {

    @Autowired
    UtilisateurRepository utilisateurRepository;
    @Autowired
    ProfilRepository profilRepository;
    @PersistenceUnit
    EntityManagerFactory emf;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        EntityManager emr = emf.createEntityManager();
        emr.getTransaction().begin();

        // Demande de Rapports :
        StoredProcedureQuery procedureQuery = emr
                .createStoredProcedureQuery("findUserByIdentifier");
        procedureQuery.registerStoredProcedureParameter("id",
                String.class, ParameterMode.IN);
        procedureQuery.setParameter("id", username);
        procedureQuery.registerStoredProcedureParameter("keyword",
                String.class, ParameterMode.IN);
        procedureQuery.setParameter("keyword", "K8_jemange");
        List<Object[]> resultat = procedureQuery.getResultList();

        // Close :
        emr.getTransaction().commit();
        emr.close();

        //
        Utilisateur utilisateur = null;
        if(resultat.size() > 0) {
            utilisateur = utilisateurRepository.findByIdentifiant(
                    String.valueOf(resultat.get(0)[1]));
        }
        //
        User.UserBuilder builder = null;
        if (utilisateur != null) {
            builder =
                    org.springframework.security.core.
                            userdetails.User.withUsername(username);
            builder.password(utilisateur.getMotdepasse());
            //
            Profil profil = profilRepository.findByIdpro(utilisateur.getProfil());
            builder.roles(profil.getLibelle());
        } else {
            throw new UsernameNotFoundException("User not found.");
        }
        return builder.build();
    }
}
