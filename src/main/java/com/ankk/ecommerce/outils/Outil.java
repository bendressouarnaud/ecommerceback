package com.ankk.ecommerce.outils;

import com.ankk.ecommerce.models.Utilisateur;
import com.ankk.ecommerce.repositories.CommandeRepository;
import com.ankk.ecommerce.repositories.UtilisateurRepository;
import com.ankk.ecommerce.securite.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class Outil {

    // A t t r i b u t e s :
    @PersistenceUnit
    EntityManagerFactory emf;
    @Autowired
    UtilisateurRepository utilisateurRepository;
    @Autowired
    CommandeRepository commandeRepository;
    @Autowired
    JwtUtil jwtUtil;



    // M e t h o d s :
    public Utilisateur getCompanyUser(HttpServletRequest request){
        String identifiant = getBackUserConnectedName(request);

        //
        EntityManager emr = emf.createEntityManager();
        emr.getTransaction().begin();

        // Demande de Rapports :
        StoredProcedureQuery procedureQuery = emr
                .createStoredProcedureQuery("findUserByIdentifier");
        procedureQuery.registerStoredProcedureParameter("id",
                String.class, ParameterMode.IN);
        procedureQuery.setParameter("id", identifiant);
        procedureQuery.registerStoredProcedureParameter("keyword",
                String.class, ParameterMode.IN);
        procedureQuery.setParameter("keyword", "K8_jemange");
        List<Object[]> resultat = procedureQuery.getResultList();

        // Close :
        emr.getTransaction().commit();
        emr.close();

        Utilisateur ur = null;
        if(resultat.size() > 0) {
            ur = utilisateurRepository.findByIdentifiant(String.valueOf(resultat.get(0)[1]));
        }

        return ur;
    }


    private String getBackUserConnectedName(HttpServletRequest request){
        //
        String username = "";
        try {
            String requestTokenHeader = request.getHeader("Authorization");
            String token = null;
            token = requestTokenHeader.substring(7);
            username = jwtUtil.getUsernameFromToken(token);
        }
        catch (Exception exc){

        }
        return username;
    }
}
