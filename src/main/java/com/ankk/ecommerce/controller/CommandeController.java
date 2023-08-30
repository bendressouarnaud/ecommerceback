package com.ankk.ecommerce.controller;

import com.ankk.ecommerce.beans.*;
import com.ankk.ecommerce.mesobjets.TachesService;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
    TachesService tachesService;
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
                ce.setEmission(0);
                ce.setLivre(0);
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
    @Operation(summary = "Obtenir la liste des articles issus d'une nouvelle commande")
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


    // Now get all ARTICLES
    @CrossOrigin("*")
    @Operation(summary = "Obtenir la liste des articles issus d'une commande validée")
    @PostMapping(value="/getvalidatedarticlesfromcommande")
    private List<BeanArticleCommande> getvalidatedarticlesfromcommande(
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
        List<BeanArticleCommande> ret = new ArrayList<>();

        // Process :
        listeCom.stream().filter(c -> (c.getTraite() ==1 && c.getDisponible() > 0)).forEach(
                d -> {
                    // Idart :
                    Article ale = articleRepository.findByIdart(d.getIdart());
                    int prix = ale.getPrix();

                    BeanArticleCommande be = new BeanArticleCommande();
                    be.setLibelle(ale.getLibelle());
                    be.setPrix(prix);
                    be.setTotal(0);
                    be.setLien(ale.getLienweb());
                    be.setDisponibilite(d.getDisponible());
                    be.setIdcde(0);
                    ret.add(be);
                }
        );
        return ret;
    }




    @CrossOrigin("*")
    @Operation(summary = "Signaler l'émission de la COMMANDE au CLIENT")
    @PostMapping(value="/emissioncolis")
    private Reponse emissioncolis(
            @RequestParam(value="idcli") Integer idcli,
            @RequestParam(value="dates") String dates,
            @RequestParam(value="heure") String heure,
            HttpServletRequest request){
        Date dte = null;
        try {
            dte = new SimpleDateFormat("yyyy-MM-dd").parse(dates);
        }
        catch (Exception exc){}

        AtomicReference<String> dt = new AtomicReference<>("");
        AtomicInteger iduser = new AtomicInteger();
        AtomicReference<String> heu = new AtomicReference<>("");

        List<Commande> listeCom = commandeRepository.findAllByIduserAndDatesAndHeure(idcli, dte, heure);
        // Only COMMANDE for which 'TRAITE' = 1
        listeCom.forEach(
            d -> {
                Commande ce = commandeRepository.findByIdcde(d.getIdcde());
                ce.setEmission(1);
                commandeRepository.save(ce);

                // Notify the USER
                dt.set(new SimpleDateFormat("yyyy-MM-dd").format(
                        ce.getDates()));
                iduser.set(ce.getIduser());
                heu.set(ce.getHeure());
            }
        );

        if(!dt.get().isEmpty()) {
            tachesService.
                    notifyCustomerForOngoingCommand(iduser.get(), "2", dt.get(), heu.get());
        }

        Reponse re = new Reponse();
        re.setElement("OK");
        re.setIdentifiant("");
        re.setProfil("");
        return re;
    }


    @CrossOrigin("*")
    @Operation(summary = "Enregistrer l'ACTION de LIVRAISON de la COMMANDE au CLIENT")
    @PostMapping(value="/livraisonCommande")
    private Reponse livraisonCommande(
            @RequestParam(value="idcli") Integer idcli,
            @RequestParam(value="dates") String dates,
            @RequestParam(value="heure") String heure,
            HttpServletRequest request){
        Date dte = null;
        try {
            dte = new SimpleDateFormat("yyyy-MM-dd").parse(dates);
        }
        catch (Exception exc){}

        AtomicReference<String> dt = new AtomicReference<>("");
        AtomicInteger iduser = new AtomicInteger();
        AtomicReference<String> heu = new AtomicReference<>("");

        List<Commande> listeCom = commandeRepository.findAllByIduserAndDatesAndHeure(idcli, dte, heure);
        // Only COMMANDE for which 'TRAITE' = 1
        listeCom.forEach(
            d -> {
                Commande ce = commandeRepository.findByIdcde(d.getIdcde());
                ce.setLivre(1);
                commandeRepository.save(ce);

                // Notify the USER
                dt.set(new SimpleDateFormat("yyyy-MM-dd").format(
                        ce.getDates()));
                iduser.set(ce.getIduser());
                heu.set(ce.getHeure());
            }
        );

        if(!dt.get().isEmpty()) {
            tachesService.
                notifyCustomerForOngoingCommand(iduser.get(), "3", dt.get(), heu.get());
        }

        Reponse re = new Reponse();
        re.setElement("OK");
        re.setIdentifiant("");
        re.setProfil("");
        return re;
    }



    @CrossOrigin("*")
    @Operation(summary = "Valider les commandes")
    @PostMapping(value="/validatecommande")
    private Reponse validatecommande(@RequestBody Beanapprobation[] data,
        HttpServletRequest request){
        //
        Utilisateur ur = outil.getCompanyUser(request);
        Reponse re = new Reponse();
        AtomicReference<String> dte = new AtomicReference<>("");
        AtomicInteger iduser = new AtomicInteger();
        AtomicReference<String> heu = new AtomicReference<>("");

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

                    // Notify USER :
                    dte.set(new SimpleDateFormat("yyyy-MM-dd").format(
                            ce.getDates()));
                    iduser.set(ce.getIduser());
                    heu.set(ce.getHeure());
                }
            }
        );

        //
        if(!dte.get().isEmpty()) {
            tachesService.
                    notifyCustomerForOngoingCommand(iduser.get(), "1", dte.get(), heu.get());
        }

        // Notify :
        re.setElement("OK");
        re.setIdentifiant("");
        re.setProfil("");
        return re;
    }

    @CrossOrigin("*")
    @Operation(summary = "Obtenir l'historique des commandes d'un client")
    @PostMapping(value="/getmobilehistoricalcommande")
    private List<BeanCommandeProjection> getmobilehistoricalcommande(
            @RequestBody RequeteBean rn,
            HttpServletRequest request){

        ModelMapper modelMapper = new ModelMapper();
        return commandeRepository.findAllCustomerCommande(rn.getIdprd()).
                stream().map(d -> modelMapper.map(d, BeanCommandeProjection.class))
                .collect(Collectors.toList());
    }
}
