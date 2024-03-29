package com.ankk.ecommerce.controller;

import com.ankk.ecommerce.beans.*;
import com.ankk.ecommerce.mesobjets.TachesService;
import com.ankk.ecommerce.models.*;
import com.ankk.ecommerce.outils.Outil;
import com.ankk.ecommerce.projections.BeanArticleDiscountedProjection;
import com.ankk.ecommerce.repositories.*;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@RestController
@Tag(name="ApiCommande")
public class CommandeController {

    // Notes :
    // http://localhost:8080/backendcommerce/swagger-ui/index.html#/

    // A t t r i b u t e s :
    @PersistenceUnit
    EntityManagerFactory emf;
    @Autowired
    NotificationcommandeRepository notificationcommandeRepository;
    @Autowired
    CommentaireRepository commentaireRepository;
    @Autowired
    PromotionRepository promotionRepository;
    @Autowired
    LienpromotionRepository lienpromotionRepository;
    @Autowired
    TachesService tachesService;
    @Autowired
    ArticleRepository articleRepository;
    @Autowired
    ClientRepository clientRepository;
    @Autowired
    CommandeRepository commandeRepository;
    @Autowired
    DetailRepository detailRepository;
    @Autowired
    SousproduitRepository sousproduitRepository;
    @Autowired
    ProduitRepository produitRepository;
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
        List<BeanProcessMail> listeMail = new ArrayList<>();
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
                
                // Process for MAIL :
                Article ale = articleRepository.findByIdart(d.getIdart());
                BeanProcessMail bl = listeMail.stream().filter(
                    e -> e.getIdent() == ale.getIdent())
                        .findFirst().orElse(null);
                if(bl == null){
                    bl = new BeanProcessMail();
                    bl.setIdent(ale.getIdent());
                }

                // go ahead :
                if(bl.getIdart().add(d.getIdart())){
                    // Add 'lienweb'
                    bl.getLienweb().add(new BeanLibImg(ale.getLibelle(), ale.getLienweb()));
                }

                // Add :
                listeMail.add(bl);
            }
        );

        // Send the MAIL :
        tachesService.notifyCompany(listeMail,
                clientRepository.findByIdcli(data.getIdcli()));

        rn.setEtat(1);
        rn.setDates(dte);
        rn.setHeure(heure);

        //
        return rn;
    }

    @CrossOrigin("*")
    @Operation(summary = "Obtenir la liste des paiements pour les grossistes")
    @GetMapping(value="/getgrossistepaiement")
    private List<BeanPaiementGrossiste> getgrossistepaiement(HttpServletRequest request){
        ModelMapper modelMapper = new ModelMapper();
        Utilisateur ur = outil.getCompanyUser(request);
        return commandeRepository.findAllPaiementGrossiste(ur.getIdent()).
                stream().map(d -> modelMapper.map(d, BeanPaiementGrossiste.class))
                .collect(Collectors.toList());
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

        Utilisateur ur = outil.getCompanyUser(request);

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
        listeCom.stream().filter(c -> (ur.getIdent() == articleRepository.findByIdart(c.getIdart()).getIdent())).forEach(
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

        // Get connected USER :
        Utilisateur ur = outil.getCompanyUser(request);

        List<Commande> listeCom = commandeRepository.findAllByIduserAndDatesAndHeure(idcli, dte, heure);
        List<BeanArticleCommande> ret = new ArrayList<>();

        // Process :
        listeCom.stream().filter(c -> (c.getTraite() ==1 && c.getDisponible() > 0 &&
                (ur.getIdent() == articleRepository.findByIdart(c.getIdart()).getIdent()))).forEach(
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

        Utilisateur ur = outil.getCompanyUser(request);

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
        listeCom.stream().filter(c -> ur.getIdent() == articleRepository.findByIdart(c.getIdart()).getIdent()).forEach(
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

        // Trace action :
        Notificationcommande ne = new Notificationcommande();
        try {
            ne.setDates(new SimpleDateFormat("yyyy-MM-dd").
                    parse(dt.get()));
        } catch (Exception exc) {
            ne.setDates(null);
        }
        ne.setHeure(heu.get());
        ne.setStatut(2);
        ne.setIdcli(iduser.get());
        notificationcommandeRepository.save(ne);

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

        Utilisateur ur = outil.getCompanyUser(request);

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
        listeCom.stream().filter(c -> (ur.getIdent() == articleRepository.findByIdart(c.getIdart()).getIdent())).forEach(
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

        // Trace action :
        Notificationcommande ne = new Notificationcommande();
        try {
            ne.setDates(new SimpleDateFormat("yyyy-MM-dd").
                    parse(dt.get()));
        } catch (Exception exc) {
            ne.setDates(null);
        }
        ne.setHeure(heu.get());
        ne.setStatut(3);
        ne.setIdcli(iduser.get());
        notificationcommandeRepository.save(ne);

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

        // Trace action :
        Notificationcommande ne = new Notificationcommande();
        try {
            ne.setDates(new SimpleDateFormat("yyyy-MM-dd").
                    parse(dte.get()));
        } catch (Exception exc) {
            ne.setDates(null);
        }
        ne.setHeure(heu.get());
        ne.setStatut(1);
        ne.setIdcli(iduser.get());
        notificationcommandeRepository.save(ne);

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


    @CrossOrigin("*")
    @Operation(summary = "Obtenir les articles composant une commande d'un client")
    @PostMapping(value="/getcustomercommandearticle")
    private BeanArticleHistoCommande getcustomercommandearticle(
            @RequestBody RequeteHistoCommande re,
            HttpServletRequest request){

        Date dte = null;
        try {
            dte = new SimpleDateFormat("yyyy-MM-dd").parse(re.getDates());
        }
        catch (Exception exc){}

        List<Beanresumearticle> listearticle = new ArrayList<>();
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger prix = new AtomicInteger(0);
        List<Commande> listeCom = commandeRepository.findAllByIduserAndDatesAndHeure(re.getIdcli(), dte, re.getHeure());
        listeCom.forEach(
            d -> {
                Beanresumearticle be = new Beanresumearticle();
                be.setIdart(d.getIdart()); // Set TOTAL ARTICLE
                be.setPrix(d.getPrix());
                // Get ARTICLE
                Article ae = articleRepository.findByIdart(d.getIdart());
                be.setLibelle(ae.getLibelle());
                be.setLienweb(ae.getLienweb());
                total.set( total.get() + d.getTotal() );
                prix.set( prix.get() + (d.getPrix() * d.getTotal()) );
                listearticle.add(be);
            }
        );

        BeanArticleHistoCommande rt = new BeanArticleHistoCommande();
        rt.setTotalprix(prix.get());
        rt.setTotalarticle(total.get());
        rt.setListearticle(listearticle);

        //
        return rt;
    }

    @CrossOrigin("*")
    @Operation(summary = "Enregistrer le commentaire d'un client lié à un article")
    @PostMapping(value="/sendmobilecomment")
    private RequeteBean sendmobilecomment(
            @RequestBody BeanCommentRequest bt,
            HttpServletRequest request){

        Commentaire ce = new Commentaire();
        ce.setIdcli(bt.getIdcli());
        ce.setIdart(bt.getIdart());
        ce.setNote(bt.getNote());
        ce.setAppreciation("");
        ce.setCommentaire(bt.getCommentaire());

        String dte = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String heure = new SimpleDateFormat("HH:mm:ss").format(new Date());
        try {
            Date dateToday = new SimpleDateFormat("yyyy-MM-dd").
                    parse(dte);
            ce.setDates(dateToday);
        }
        catch (Exception exc){
            ce.setDates(null);
        }
        ce.setHeure(heure);
        commentaireRepository.save(ce);

        //
        RequeteBean rn = new RequeteBean();
        rn.setIdprd(1);
        return rn;
    }

    @CrossOrigin("*")
    @Operation(summary = "Obtenir la liste des articles en PROMOTION")
    @GetMapping(value="/getarticlesdiscounted")
    private List<BeanArticleDiscounted> getarticlesdiscounted(HttpServletRequest request){
        ModelMapper modelMapper = new ModelMapper();
        Utilisateur ur = outil.getCompanyUser(request);
        return articleRepository.findAllDiscountedArticle().
                stream().map(d -> modelMapper.map(d, BeanArticleDiscounted.class))
                .collect(Collectors.toList());
    }

    @CrossOrigin("*")
    @Operation(summary = "Obtenir la liste des commandes par entreprise")
    @PostMapping(value="/getmobilearticlebookedbycompany")
    private List<BeanArticleBooked> getmobilearticlebookedbycompany(
            @RequestBody RequeteBean requeteBean,
            HttpServletRequest request){
        ModelMapper modelMapper = new ModelMapper();
        return commandeRepository.findAllCompanyCommande(requeteBean.getIdprd()).
                stream().map(d -> modelMapper.map(d, BeanArticleBooked.class))
                .collect(Collectors.toList());
    }
}
