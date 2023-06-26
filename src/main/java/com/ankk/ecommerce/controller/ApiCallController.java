package com.ankk.ecommerce.controller;

import com.ankk.ecommerce.beans.*;
import com.ankk.ecommerce.models.*;
import com.ankk.ecommerce.repositories.*;
import com.ankk.ecommerce.securite.JwtUtil;
import com.ankk.ecommerce.securite.UserDetailsServiceImp;
import com.ankk.ecommerce.service.FileService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ApiCallController {

    // Attribute :
    @PersistenceUnit
    EntityManagerFactory emf;
    @Autowired
    ProfilRepository profilRepository;
    @Autowired
    PartenaireRepository partenaireRepository;
    @Autowired
    FileService fileService;
    @Autowired
    UtilisateurRepository utilisateurRepository;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserDetailsServiceImp userDetailsServiceImp;
    @Autowired
    SousproduitRepository sousproduitRepository;
    @Autowired
    ProduitRepository produitRepository;
    @Autowired
    JwtUtil jwtUtil;
    @Value("${app.firebase-config}")
    private String firebaseConfig;
    FirebaseApp firebaseApp;


    // Methods
    @PostConstruct
    private void initialize() {
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            new ClassPathResource(firebaseConfig).
                                    getInputStream())).build();
            if (FirebaseApp.getApps().isEmpty()) {
                this.firebaseApp = FirebaseApp.initializeApp(options);
                //System.out.println("initializeApp");
            } else {
                this.firebaseApp = FirebaseApp.getInstance();
                //System.out.println("getInstance");
            }
        } catch (IOException e) {
            //log.error("Create FirebaseApp Error", e);
        }
    }

    @CrossOrigin("*")
    @PostMapping(value="/authentification")
    private ResponseEntity<?> authentification(
            @RequestBody UserLog userLog) throws Exception{

        EntityManager emr = emf.createEntityManager();
        emr.getTransaction().begin();

        // Demande de Rapports :
        StoredProcedureQuery procedureQuery = emr
                .createStoredProcedureQuery("findUserByCredentials");
        procedureQuery.registerStoredProcedureParameter("userid",
                String.class, ParameterMode.IN);
        procedureQuery.setParameter("userid", userLog.getIdentifiant());
        procedureQuery.registerStoredProcedureParameter("motpasse",
                String.class, ParameterMode.IN);
        procedureQuery.setParameter("motpasse", userLog.getMotdepasse());
        procedureQuery.registerStoredProcedureParameter("motcle",
                String.class, ParameterMode.IN);
        procedureQuery.setParameter("motcle", "K8_jemange");
        List<Object[]> resultat = procedureQuery.getResultList();

        // Close :
        emr.getTransaction().commit();
        emr.close();

        //
        Utilisateur mUser = null;
        List<Utilisateur> users = new ArrayList<>();

        if(resultat.size() > 0) {
            mUser = utilisateurRepository.findByIdentifiantAndMotdepasse(
                    userLog.getIdentifiant(),
                    userLog.getMotdepasse());
            users = utilisateurRepository.findAllByEmail(String.valueOf(resultat.get(0)[2]));
        }
        //
        Map<String, Object> stringMap = new HashMap<>();
        if(users.size() > 0) {

            /**/
            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                userLog.getIdentifiant(), userLog.getMotdepasse()
                        )
                );
            } catch (BadCredentialsException e) {
                throw new Exception("Nom d'utilisateur ou mot de passe incorrect !");
            }

            //
            UserDetails userDetails = userDetailsServiceImp.loadUserByUsername(userLog.getIdentifiant());
            String jwt = jwtUtil.generateToken(userDetails);
            stringMap.put("userexist", "1");
            stringMap.put("code", String.valueOf(HttpStatus.OK.value()));
            stringMap.put("data", jwt);
            // get USER details :
            Utilisateur ur = utilisateurRepository.findByIdentifiantAndMotdepasse(
                    userLog.getIdentifiant(), userLog.getMotdepasse()
            );
            Profil pl = profilRepository.findByIdpro(ur.getProfil());
            stringMap.put("profil", pl.getCode());
            stringMap.put("identifiant", ur.getIdentifiant());
            // Now, in case the password is still 4 characters long,
            //  redirect user to change the password :
            stringMap.put("paswordchange", (userLog.getMotdepasse().trim().length() == 4) ? 0 : 1);

            // Track the login :
            /*tachesService.trackJournal("Utilisateur connecté depuis l'application WEB",
                    ur.getIduser());*/
        }
        else stringMap.put("userexist", "0");
        return ResponseEntity.ok(stringMap);
    }

    @CrossOrigin("*")
    @GetMapping(value="/getprofiliste")
    private List<Profil> getprofiliste(HttpServletRequest request){

        List<Object[]> liste = getUserId(request);
        Utilisateur ur = null;
        if(liste.size() > 0) {
            ur = utilisateurRepository.findByEmail(String.valueOf(liste.get(0)[2]));
        }

        Integer[] tabSup = {1};
        Integer[] tabAdm = {2,3};
        List<Profil> retour = ur.getProfil() == 1 ?
                profilRepository.findAllByIdproIn(Arrays.asList(tabSup)) :
                profilRepository.findAllByIdproIn(Arrays.asList(tabAdm));
        return retour;
    }

    @CrossOrigin("*")
    @GetMapping(value="/getAllPartenaies")
    private List<Partenaire> getAllPartenaies(){
        return partenaireRepository.findAllByOrderByLibelleAsc();
    }


    @CrossOrigin("*")
    @GetMapping(value="/getAllProduits")
    private List<Produit> getAllProduits(){
        List<Produit> lte = produitRepository.findAll();
        lte.forEach(
                d -> {
                    String tp = d.getLienweb();
                    d.setLienweb("https://firebasestorage.googleapis.com/v0/b/gestionpanneaux.appspot.com/o/"+
                            tp+"?alt=media");
                }
        );
        return lte;
    }

    @CrossOrigin("*")
    @GetMapping(value="/gethistoriquesproduits")
    private List<Beansousproduit> gethistoriquesproduits(){
        List<Sousproduit> lte = sousproduitRepository.findAll();
        List<Beansousproduit> ret = new ArrayList<>();
        lte.forEach(
                d -> {
                    Beansousproduit bt = new Beansousproduit();
                    bt.setIdspr(d.getIdspr());
                    bt.setLibelle(d.getLibelle());
                    bt.setLienweb("https://firebasestorage.googleapis.com/v0/b/gestionpanneaux.appspot.com/o/"+
                            d.getLienweb()+"?alt=media");
                    Produit pt = produitRepository.findByIdprd(d.getIdprd());
                    bt.setProduit(pt.getLibelle());
                    ret.add(bt);
                }
        );
        return ret;
    }

    @CrossOrigin("*")
    @GetMapping(value="/enregistrerPartenaire")
    private Reponse enregistrerPartenaire(
            @RequestParam(value="ident") Integer ident,
            @RequestParam(value="libelle") String libelle,
            @RequestParam(value="contact") String contact,
            @RequestParam(value="email") String email
    ){
        Reponse rse = new Reponse();
        Partenaire pe = partenaireRepository.findByIdent(ident);
        if(pe == null){
            pe = new Partenaire();
        }
        pe.setLibelle(libelle);
        pe.setContact(contact);
        pe.setEmail(email);
        partenaireRepository.save(pe);

        rse.setElement("ok");
        rse.setProfil("ok");
        rse.setIdentifiant("ok");

        return rse;
    }


    @CrossOrigin("*")
    @GetMapping(value="/enregistrerAdminUser")
    private Reponse enregistrerAdminUser(
            @RequestParam(value="nom") String nom,
            @RequestParam(value="prenom") String prenom,
            @RequestParam(value="contact") String contact,
            @RequestParam(value="email") String email,
            @RequestParam(value="profil") Integer profil,
            @RequestParam(value="ident") Integer ident,
            HttpServletRequest request
    ){

        //
        Reponse rse = new Reponse();

        //
        List<Object[]> resultat = getUserId(request);
        Utilisateur ur = null;
        if(resultat.size() > 0) {
            ur = utilisateurRepository.findByEmail(String.valueOf(resultat.get(0)[2]));
        }
        /******************/

        Utilisateur usr = utilisateurRepository.findByEmail(email.trim()); //
        if(usr == null){
            // new ONE :
            usr = new Utilisateur();

            //
            String[] valeur = email.split("@");
            // Limit 'identifiant' length :
            usr.setIdentifiant(valeur[0].length() > 15 ? valeur[0].substring(0,14) : valeur[0]);
            String heure = new SimpleDateFormat("HH:mm:ss").format(new Date());
            //usr.setMotdepasse("0000");
            usr.setMotdepasse(heure.replace(":",""));
            usr.setNom(nom);
            usr.setPrenom(prenom);
            usr.setContact(contact);
            usr.setEmail(email);
            usr.setProfil(profil);
            usr.setToken("");
            usr.setFcmtoken("");
            usr.setIdent(ident);
            //
            utilisateurRepository.save(usr).getIduser();
            rse.setElement("ok");
            rse.setProfil("ok");
            rse.setIdentifiant("ok");
        }
        else{
            // exist. Warn :
            rse.setElement("pok");
            rse.setProfil("pok");
            rse.setIdentifiant("pok");
        }
        return rse;
    }


    @CrossOrigin("*")
    @GetMapping(value="/getAllusers")
    private List<ReponseUserFulNew> getAllusers(HttpServletRequest request){
        //
        List<Object[]> resultat = getUserId(request);
        Utilisateur ur = null;
        if(resultat.size() > 0) {
            ur = utilisateurRepository.findByEmail(String.valueOf(resultat.get(0)[2]));
        }

        // if Superadmin, pick 'Admin' users
        List<Utilisateur> liste = ur.getProfil() == 1 ?
                utilisateurRepository.findAllByProfilOrderByNomAsc(2) :
                utilisateurRepository.
                        findAllByProfilNotAndIdentOrderByNomAsc(1,
                                ur.getIdent());
        List<ReponseUserFulNew> retour = new ArrayList<>();
        liste.forEach(
                d -> {
                    ReponseUserFulNew rw = new ReponseUserFulNew();
                    rw.setContact(d.getContact());
                    rw.setNom(d.getNom());
                    rw.setPrenom(d.getPrenom());
                    rw.setEmail(d.getEmail());
                    rw.setProfil(profilRepository.findByIdpro(d.getProfil()).getLibelle());
                    rw.setIduser(String.valueOf(d.getIduser()));
                    rw.setIdmai(String.valueOf(d.getIdent()));
                    retour.add(rw);
                }
        );
        return retour;
    }

    @CrossOrigin("*")
    @PostMapping("/savepoducts")
    public Reponse savepoducts(@RequestParam("produit") MultipartFile multipartFile,
                          @RequestParam(name="libelle") String libelle) {
        //System.out.println("Libelle : "+libelle);
        fileService.upload(multipartFile, libelle, 0, 0);
        Reponse re = new Reponse();
        re.setElement("OK");
        re.setIdentifiant("OK");
        re.setProfil("OK");
        return  re;
    }

    @CrossOrigin("*")
    @PostMapping("/savesouspoducts")
    public Reponse savesouspoducts(@RequestParam("produit") MultipartFile multipartFile,
                          @RequestParam(name="libelle") String libelle,
                          @RequestParam(name="idprd") Integer idprd
    ) {
        //System.out.println("Libelle : "+libelle);
        fileService.upload(multipartFile, libelle, 1, idprd);
        Reponse re = new Reponse();
        re.setElement("OK");
        re.setIdentifiant("OK");
        re.setProfil("OK");
        return  re;
    }

    @CrossOrigin("*")
    @GetMapping(value="/parametresconnexion")
    private List<ReponseUser> parametresconnexion(@RequestParam(value="identifiant") String identifiant){
        EntityManager emr = emf.createEntityManager();
        emr.getTransaction().begin();

        //
        Utilisateur utr = utilisateurRepository.findByIduser(Integer.parseInt(identifiant));

        // Demande de Rapports :
        StoredProcedureQuery procedureQuery = emr
                .createStoredProcedureQuery("findUserByIdentifier");
        procedureQuery.registerStoredProcedureParameter("id",
                String.class, ParameterMode.IN);
        procedureQuery.setParameter("id", utr.getIdentifiant());
        procedureQuery.registerStoredProcedureParameter("keyword",
                String.class, ParameterMode.IN);
        procedureQuery.setParameter("keyword", "K8_jemange");
        List<Object[]> resultat = procedureQuery.getResultList();

        // Close :
        emr.getTransaction().commit();
        emr.close();

        //
        List<ReponseUser> userListe = new ArrayList<>();
        //
        ReponseUser rse = new ReponseUser();
        rse.setNom(String.valueOf(resultat.get(0)[0]));
        rse.setPrenom(String.valueOf(resultat.get(0)[1]));
        rse.setContact("");
        rse.setEmail("");
        // get profil ID :
        rse.setIdentifiant("");
        userListe.add(rse);
        return userListe;
    }

    @PostMapping("/profile/pic/{fileName}")
    public Object download(@PathVariable String fileName) throws IOException {
        //logger.info("HIT -/download | File Name : {}", fileName);
        return fileService.download(fileName);
    }


    private List<Object[]> getUserId(HttpServletRequest request){
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

        return resultat;
    }

    // Get Back the USERNAME of the current authenticated user :
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
