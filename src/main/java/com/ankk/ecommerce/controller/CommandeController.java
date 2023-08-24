package com.ankk.ecommerce.controller;

import com.ankk.ecommerce.beans.*;
import com.ankk.ecommerce.models.*;
import com.ankk.ecommerce.outils.Outil;
import com.ankk.ecommerce.repositories.ArticleRepository;
import com.ankk.ecommerce.repositories.CommandeRepository;
import com.ankk.ecommerce.repositories.LienpromotionRepository;
import com.ankk.ecommerce.repositories.PromotionRepository;
import com.ankk.ecommerce.securite.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@RestController
@Tag(name="ApiCommande")
public class CommandeController {


    // A t t r i b u t e s :
    @PersistenceUnit
    EntityManagerFactory emf;
    @Autowired
    PromotionRepository promotionRepository;
    @Autowired
    LienpromotionRepository lienpromotionRepository;
    @Autowired
    ArticleRepository articleRepository;
    @Autowired
    CommandeRepository commandeRepository;
    @Autowired
    Outil outil;
    @Autowired
    JwtUtil jwtUtil;
    @Value("${app.firebase-config}")
    private String firebaseConfig;



    // M e t h o d s :
    @CrossOrigin("*")
    @PostMapping(value={"/sendbooking"})
    private ResponseBooking sendbooking(@RequestBody Beanarticlerequest data){
        //
        ResponseBooking rn = new ResponseBooking();
        String dte = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String heure = new SimpleDateFormat("HH:mm:ss").format(new Date());
        data.getListe().forEach(
            d -> {
                Commande ce = new Commande();
                ce.setIdart(d.getIdart());
                // Get Article PRICE & Compute PERCENTAGE Price if needed :
                int pourcentage = 0;
                Lienpromotion lnt =
                        lienpromotionRepository.findByIdartAndEtat(d.getIdart(), 1);
                if(lnt != null){
                    pourcentage =
                            promotionRepository.findByIdprn(lnt.getIdpro()).getReduction();
                }
                if(pourcentage > 0){
                    int price = articleRepository.findByIdart(d.getIdart()).getPrix();
                    int tpPrice = ((price * pourcentage) / 100);
                    int articlePrix = price - tpPrice;
                    ce.setPrix(articlePrix);
                }
                else ce.setPrix(articleRepository.findByIdart(d.getIdart()).getPrix());
                /*
                1 : MOBILE MONEY
                2 : PAIEMENT à la LIVRAISON
                 */
                ce.setEtat(data.getChoixpaiement());
                try {
                    Date dateToday = new SimpleDateFormat("yyyy-MM-dd").
                            parse(dte);
                    ce.setDates(dateToday);
                }
                catch (Exception exc){
                    ce.setDates(null);
                }
                ce.setHeure(heure);
                ce.setIduser(data.getIdcli());
                ce.setTraite(0);
                ce.setTotal(d.getTotal());
                ce.setDisponible(0);
                commandeRepository.save(ce);
            }
        );

        rn.setEtat(1);
        rn.setDates(dte);
        rn.setHeure(heure);

        //
        return rn;
    }


    @CrossOrigin("*")
    @Operation(summary = "Obtenir la liste des commande en cours")
    @PostMapping(value="/getongoingcommande")
    private List<BeanOngoingCommande> getongoingcommande(@RequestParam(value="traite") Integer traite,
                                                         HttpServletRequest request){
        ModelMapper modelMapper = new ModelMapper();
        Utilisateur ur = outil.getCompanyUser(request);
        return commandeRepository.findAllOnGoingCommande(traite, ur.getIdent()).
                        stream().map(d -> modelMapper.map(d, BeanOngoingCommande.class))
                        .collect(Collectors.toList());
    }


    // Now get all ARTICLES
    @CrossOrigin("*")
    @Operation(summary = "Obtenir la liste des articles issus d'une commande")
    @PostMapping(value="/getongoingarticlesfromcommande")
    private List<BeanArticleCommande> getongoingarticlesfromcommande(
            @RequestParam(value="idcli") Integer idcli,
            @RequestParam(value="dates") String dates,
            @RequestParam(value="heure") String heure,
            HttpServletRequest request){
        Date dte = null;
        try {
            dte = new SimpleDateFormat("yyyy-MM-dd").
                    parse(dates);
        }
        catch (Exception exc){
        }

        List<Commande> listeCom = commandeRepository.findAllByIduserAndDatesAndHeure(idcli, dte, heure);
        /*IntStream li = listeCom.stream().mapToInt(Commande::getIdart).distinct();
        Date finalDte = dte;*/
        List<BeanArticleCommande> ret = new ArrayList<>();

        // Process :
        listeCom.forEach(
            d -> {
                // Idart :
                Article ale = articleRepository.findByIdart(d.getIdart());
                int prix = ale.getPrix();

                BeanArticleCommande be = new BeanArticleCommande();
                be.setLibelle(ale.getLibelle());
                be.setPrix(prix);
                be.setTotal(d.getTotal());
                be.setLien(ale.getLienweb());
                be.setDisponibilite(ale.getQuantite());
                be.setIdcde(d.getIdcde());
                ret.add(be);
            }
        );
        return ret;
    }

    @CrossOrigin("*")
    @Operation(summary = "Valider les commandes")
    @PostMapping(value="/validatecommande")
    private Reponse validatecommande(@RequestBody Beanapprobation[] data,
        HttpServletRequest request){
        //
        Utilisateur ur = outil.getCompanyUser(request);
        Reponse re = new Reponse();
        re.setElement("POK");

        // Keep only Requests with ARTICLEs we can provide to CUSTOMER :
        Arrays.stream(data).filter(f -> f.getApprobation() > 0).forEach(
            d -> {
                Commande ce = commandeRepository.findByIdcde(d.getIdcde());
                ce.setDisponible(d.getApprobation());
                ce.setTraite(1);
                // Reduce the number of article :
                Article art = articleRepository.findByIdart(ce.getIdart());
                if((d.getApprobation() > 0) && (art.getQuantite() >= d.getApprobation())){
                    art.setQuantite( art.getQuantite() - d.getApprobation() );
                    articleRepository.save(art);
                    commandeRepository.save(ce);
                }
            }
        );

        // Notify :
        re.setElement("OK");
        re.setIdentifiant("");
        re.setProfil("");
        return re;
    }
}
