package com.ankk.ecommerce.controller;

import com.ankk.ecommerce.beans.BeanArticleCommande;
import com.ankk.ecommerce.beans.BeanOngoingCommande;
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
    @Operation(summary = "Obtenir la liste des profils")
    @GetMapping(value="/getongoingcommande")
    private List<BeanOngoingCommande> getongoingcommande(HttpServletRequest request){
        ModelMapper modelMapper = new ModelMapper();
        Utilisateur ur = outil.getCompanyUser(request);
        return commandeRepository.findAllOnGoingCommande(0, ur.getIdent()).
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
        IntStream li = listeCom.stream().mapToInt(Commande::getIdart).distinct();
        Date finalDte = dte;
        List<BeanArticleCommande> ret = new ArrayList<>();
        listeCom.stream().mapToInt(Commande::getIdart).distinct().boxed().forEach(
            d -> {
                // Idart :
                List<Commande> listeArticleCom =
                        commandeRepository.findAllByIduserAndDatesAndHeureAndIdart(idcli, finalDte, heure, d);
                Article ale = articleRepository.findByIdart(d);
                int prix = ale.getPrix();
                Lienpromotion ln = lienpromotionRepository.findByIdartAndEtat(d, 1);
                if(ln != null){
                    Promotion pn = promotionRepository.findByIdprn(ln != null ? ln.getIdpro() : 0);
                    if(pn != null){
                        prix = ale.getPrix() - ((ale.getPrix() * pn.getReduction()) / 100);
                    }
                }

                BeanArticleCommande be = new BeanArticleCommande();
                be.setLibelle(ale.getLibelle());
                be.setPrix(prix * listeArticleCom.size());
                be.setTotal(listeArticleCom.size());
                be.setLien(ale.getLienweb());
                be.setDisponibilite(ale.getQuantite());
                ret.add(be);
            }
        );

        return ret;
    }
}
