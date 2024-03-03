package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.beans.BeanInterfaceCommandeProjection;
import com.ankk.ecommerce.beans.BeanOngoingCommande;
import com.ankk.ecommerce.beans.BeanOngoingCommandeProjection;
import com.ankk.ecommerce.beans.BeanPaiementGrossisteProjection;
import com.ankk.ecommerce.models.Commande;
import com.ankk.ecommerce.models.Utilisateur;
import com.ankk.ecommerce.projections.BeanArticleBookedProjection;
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


    @Query(value = "select f.denomination, c.libelle, b.dates,c.prix, e.prixforfait," +
            "(c.prix-e.prixforfait) as apayer from client a inner join commande b on a.idcli=" +
            "b.iduser inner join article c on c.idart = b.idart inner join liengrossiste d on " +
            "d.idart=c.idart inner join liengrossiste e on e.ident=c.ident inner join grossiste f " +
            "on f.code=a.codeinvitation where c.ident = ?1",
            nativeQuery = true)
    List<BeanPaiementGrossisteProjection> findAllPaiementGrossiste(int ident);


    // Liste des commandes pau 'CLIENT' :
    @Query(value = "select a.iduser, a.dates, a.heure, count(a.idcde) as nbrearticle, count(a.traite) as traites," +
            "sum(a.disponible) as demandeconfirme, sum(a.total) as demandeorigine, sum(a.prix) as montant," +
            "sum(a.emission) as emissions, sum(a.livre) as livres from commande a " +
            "where a.iduser = ?1 group by a.iduser, a.dates, a.heure",
            nativeQuery = true)
    List<BeanInterfaceCommandeProjection> findAllCustomerCommande(int idcl);


    @Query(value = "select a.dates,a.heure,a.iduser,c.nom,c.prenom,sum(a.total) as totaux " +
            "from commande a inner join article b on a.idart = b.idart inner join client c on " +
            "c.idcli=a.iduser where b.ident = ?1 group by a.dates,a.heure,a.iduser,c.nom,c.prenom " +
            "order by a.dates desc, a.heure desc",
            nativeQuery = true)
    List<BeanArticleBookedProjection> findAllCompanyCommande(int company);

}
