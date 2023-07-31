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
    ArticleRepository articleRepository;
    @Autowired
    CommuneRepository communeRepository;
    @Autowired
    ProduitRepository produitRepository;
    @Autowired
    ClientRepository clientRepository;
    @Autowired
    AchatRepository achatRepository;
    @Autowired
    DetailRepository detailRepository;
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
    @GetMapping(value={"/getAllDetails","/getmobileAllDetails"})
    private List<Detail> getAllDetails(){
        return detailRepository.findAll();
    }


    @CrossOrigin("*")
    @GetMapping(value={"/getAllProduits","/getmobileAllProduits"})
    private List<Produit> getAllProduits(){
        return produitRepository.findAll();
        /*lte.forEach(
                d -> {
                    String tp = d.getLienweb();
                    d.setLienweb("https://firebasestorage.googleapis.com/v0/b/gestionpanneaux.appspot.com/o/"+
                            tp+"?alt=media");
                }
        );
        return lte;*/
    }

    @CrossOrigin("*")
    @GetMapping(value={"/getAllCommunes","/getmobileAllCommunes"})
    private List<Commune> getAllCommunes(){
        System.out.println("Entrée");
        return communeRepository.findAllByOrderByLibelleAsc();
    }

    @CrossOrigin("*")
    @GetMapping(value={"/getsousproduitdata"})
    private List<Sousproduit> getsousproduitdata(){
        return sousproduitRepository.findAll();
    }


    @CrossOrigin("*")
    @GetMapping(value={"/getsousproduitlib"})
    private List<Beansousproduit> getsousproduitlib(){
        List<Sousproduit> lte = sousproduitRepository.findAll();
        List<Beansousproduit> ret = new ArrayList<>();
        lte.forEach(
                d -> {
                    Beansousproduit bt = new Beansousproduit();
                    bt.setIdspr(d.getIdspr());
                    bt.setLibelle(d.getLibelle());
                    bt.setLienweb("");
                    Produit pt = produitRepository.findByIdprd(d.getIdprd());
                    bt.setProduit(String.valueOf(pt.getIdprd()));
                    ret.add(bt);
                }
        );
        return ret;
    }


    @CrossOrigin("*")
    @GetMapping(value={"/gethistoriquesproduits"})
    private List<Beansousproduit> gethistoriquesproduits(){
        List<Sousproduit> lte = sousproduitRepository.findAll();
        List<Beansousproduit> ret = new ArrayList<>();
        lte.forEach(
                d -> {
                    Beansousproduit bt = new Beansousproduit();
                    bt.setIdspr(d.getIdspr());
                    bt.setLibelle(d.getLibelle());
                    bt.setLienweb(d.getLienweb());
                    Produit pt = produitRepository.findByIdprd(d.getIdprd());
                    bt.setProduit(pt.getLibelle());
                    ret.add(bt);
                }
        );
        return ret;
    }


    @CrossOrigin("*")
    @GetMapping(value={"/gethistoriquesdetails"})
    private List<Beandetail> gethistoriquesdetails(){
        List<Detail> lte = detailRepository.findAll();
        List<Beandetail> ret = new ArrayList<>();
        lte.forEach(
                d -> {
                    Beandetail bl = new Beandetail();
                    bl.setIddet(d.getIddet());
                    bl.setLibelle(d.getLibelle());
                    bl.setLienweb(d.getLienweb());
                    Sousproduit st = sousproduitRepository.findByIdspr(d.getIdspr());
                    bl.setSousproduit(st.getLibelle());
                    Produit pt = produitRepository.findByIdprd(st.getIdprd());
                    bl.setProduit(pt.getLibelle());
                    ret.add(bl);
                }
        );
        return ret;
    }


    @CrossOrigin("*")
    @PostMapping(value={"/getmobileallsousproduits"})
    private List<Beancategorie> getmobileallsousproduits(@RequestBody RequeteBean rn){
        List<Sousproduit> lte = sousproduitRepository.findAllByIdprd(rn.getIdprd());
        List<Beancategorie> ret = new ArrayList<>();
        lte.forEach(
                d -> {
                    Beancategorie be = new Beancategorie();
                    be.setSousproduit(d.getLibelle());
                    // For each SOUS-PRODUIT, get its details :
                    List<Detail> lesDets = detailRepository.findAllByIdspr(d.getIdspr());
                    lesDets.forEach(
                        s -> {
                            be.getDetails().add(s);
                        }
                    );

                    // Check before adding :
                    if(!be.getDetails().isEmpty()){
                        ret.add(be);
                    }
                }
        );
        return ret;
    }

    // Get ARTICLES based on iddet :
    @CrossOrigin("*")
    @PostMapping(value={"/getarticlesbasedoniddet"})
    private List<Beanarticledetail> getarticlesbasedoniddet(@RequestBody RequeteBean rn){
        List<Article> lte = articleRepository.findAllByIddetAndChoix(rn.getIdprd(), 1);
        List<Beanarticledetail> ret = new ArrayList<>();
        lte.forEach(
                d -> {
                    // For each ARTICLE, pick the number of those bought :
                    List<Achat> articleAchete = achatRepository.findAllByIdartAndActif(d.getIdart(), 0);
                    Beanarticledetail be = new Beanarticledetail();
                    be.setIddet(d.getIddet());
                    be.setIdart(d.getIdart());
                    be.setLienweb(d.getLienweb());
                    be.setLibelle(d.getLibelle());
                    be.setPrix(d.getPrix());
                    be.setReduction(0);
                    be.setNote(0);
                    be.setArticlerestant( d.getQuantite() - (articleAchete != null ? articleAchete.size() : 0) );
                    // Add
                    ret.add(be);
                }
        );
        return ret;
    }


    // Get ARTICLES based on iddet :
    @CrossOrigin("*")
    @PostMapping(value={"/managecustomer"})
    private Client managecustomer(@RequestBody Client ct){
        Client clt = clientRepository.findByEmail(ct.getEmail());
        if(clt == null) clt = new Client();
        clt.setNom(ct.getNom());
        clt.setPrenom(ct.getPrenom());
        clt.setEmail(ct.getEmail());
        clt.setNumero(ct.getNumero());
        clt.setCommune(ct.getCommune());
        clt.setAdresse(ct.getAdresse());
        clt.setGenre(ct.getGenre());
        clt.setFcmtoken(ct.getFcmtoken());

        //
        return clientRepository.save(clt);
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
        fileService.upload(multipartFile, libelle, 0, 0, null, null);
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
        fileService.upload(multipartFile, libelle, 1, idprd, null, null);
        Reponse re = new Reponse();
        re.setElement("OK");
        re.setIdentifiant("OK");
        re.setProfil("OK");
        return  re;
    }


    @CrossOrigin("*")
    @PostMapping("/savedetails")
    public Reponse savedetails(@RequestParam("detail") MultipartFile multipartFile,
                                   @RequestParam(name="libelle") String libelle,
                                   @RequestParam(name="idspr") Integer idspr
    ) {
        // Set DETAIL :
        Detail dl = new Detail();
        dl.setIdspr(idspr);
        dl.setLibelle(libelle);

        fileService.upload(multipartFile, libelle, 3, 0, null, dl);
        Reponse re = new Reponse();
        re.setElement("OK");
        re.setIdentifiant("OK");
        re.setProfil("OK");
        return  re;
    }


    @CrossOrigin("*")
    @PostMapping("/savearticles")
    public Reponse savearticles(@RequestParam("article") MultipartFile multipartFile,
        @RequestParam(name="id") Integer idart,
        @RequestParam(name="iddet") Integer iddet,
        @RequestParam(name="libelle") String libelle,
        @RequestParam(name="prix") Integer prix,
        @RequestParam(name="publication") String publication,
        @RequestParam(name="detail") String detail,
        HttpServletRequest request
    ) {

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

        Article ae = new Article();
        ae.setLibelle(libelle);
        ae.setDetail(detail);
        ae.setIdent(ur.getIdent());
        ae.setIddet(iddet); //
        ae.setPrix(prix);

        fileService.upload(multipartFile, libelle, 2, 0, ae, null);
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


    /* enregistrerUser */
    @CrossOrigin("*")
    @GetMapping(value="/enregistrerUser")
    private Reponse enregistrerUser(
            @RequestParam(value="nom") String nom,
            @RequestParam(value="prenom") String prenom,
            @RequestParam(value="contact") String contact,
            @RequestParam(value="email") String email,
            @RequestParam(value="profil") Integer profil,
            HttpServletRequest request
    ){
        //
        Reponse rse = new Reponse();
        //
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
            usr.setIdent(ur.getIdent());

            //
            int iduser = utilisateurRepository.save(usr).getIduser();

            rse.setElement("ok");
            rse.setProfil("ok");
            rse.setIdentifiant("ok");

            // By default, hit 'Parametresmob' table for each new USER :
            /*Parametresmob pb = new Parametresmob();
            pb.setEmail(email);
            pb.setDelai(120);
            pb.setIduser(iduser);
            pb.setParun("");
            pb.setPardeux(0);
            parametresmobRepository.save(pb);

            // Send a mail :
            if(profil != 2) {
                tachesService.mailCreationNew("Création de compte", usr.getIdentifiant(),
                        heure.replace(":", ""), email);
            }
            else tachesService.mailCreationNew("Création de compte", usr.getIdentifiant(),
                    heure.replace(":", ""), email, 0);

            // Track  :
            tachesService.trackJournal("Utilisateur a créé un compte utilisateur",
                    ur.getIduser());*/
        }
        else{
            // exist. Warn :
            rse.setElement("pok");
            rse.setProfil("pok");
            rse.setIdentifiant("pok");
        }

        return rse;
    }

    /* enregistrerUser */
    @CrossOrigin("*")
    @GetMapping(value="/getcompanyarticles")
    private List<Beanarticle> getcompanyarticles(
            HttpServletRequest request
    ) {
        //
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

        // Now pick articles :
        List<Beanarticle> ret = new ArrayList<>();
        List<Article> listArticle = articleRepository.findAllByIdent(ur.getIdent());
        listArticle.forEach(
                d -> {
                    Beanarticle be = new Beanarticle();
                    be.setIdart(d.getIdart());
                    be.setLibelle(d.getLibelle());
                    be.setPrix(d.getPrix());
                    be.setLienweb(d.getLienweb());
                    // DETAIL :
                    Detail dl = detailRepository.findByIddet(d.getIddet());
                    be.setAppartenance(dl.getLibelle());
                    //
                    be.setQuantite(d.getQuantite());
                    be.setChoix(d.getChoix());
                    ret.add(be);
                }
        );

        return ret;
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
