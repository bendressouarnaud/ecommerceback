package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.beans.BeanInterfaceCommandeProjection;
import com.ankk.ecommerce.beans.BeanOngoingCommande;
import com.ankk.ecommerce.beans.BeanOngoingCommandeProjection;
import com.ankk.ecommerce.models.Commande;
import com.ankk.ecommerce.models.Utilisateur;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface CommandeRepository extends CrudRepository<Commande, Integer> {

    List<Commande> findAllByEtat(int etat);
    List<Commande> findAllByIduserAndIdartAndLivre(int iduser, int idart, int livre);
    List<Commande> findAllByIduserAndDatesAndHeure(int iduser, Date dates, String heure);
    List<Commande> findAllByIduserAndDatesAndHeureAndIdart(int iduser, Date dates, String heure, int idart);
    Commande findByIdcde(int idcde);

    @Query(value = "select b.idcli, a.dates, a.heure,b.nom,b.prenom,b.numero, count(a.idcde) as total" +
            ",sum(a.emission) as emission, sum(livre) as livre from commande a inner join " +
            "client b on a.iduser=b.idcli inner join article c on c.idart=a.idart where a.traite = ?1 and " +
            "c.ident = ?2 group by b.idcli, a.dates, a.heure,b.nom,b.prenom,b.numero",
            nativeQuery = true)
    List<BeanOngoingCommandeProjection> findAllOnGoingCommande(int traite, int ident);


    // Liste des commandes pau 'CLIENT' :
    @Query(value = "select a.iduser, a.dates, a.heure, count(a.idcde) as nbrearticle, count(a.traite) as traites," +
            "sum(a.disponible) as demandeconfirme, sum(a.total) as demandeorigine, sum(a.prix) as montant," +
            "sum(a.emission) as emissions, sum(a.livre) as livres from commande a " +
            "where a.iduser = ?1 group by a.iduser, a.dates, a.heure",
            nativeQuery = true)
    List<BeanInterfaceCommandeProjection> findAllCustomerCommande(int idcl);

}
