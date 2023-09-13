package com.ankk.ecommerce.controller;

import com.ankk.ecommerce.beans.*;
import com.ankk.ecommerce.mesobjets.TachesService;
import com.ankk.ecommerce.models.*;
import com.ankk.ecommerce.repositories.*;
import com.ankk.ecommerce.securite.JwtUtil;
import com.ankk.ecommerce.securite.UserDetailsServiceImp;
import com.ankk.ecommerce.service.FileService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
@Tag(name="ApiGen")
public class ApiCallController {

    // A T T R I B U T E S :
    // http://localhost:8080/backendcommerce/swagger-ui/index.html#/
    @PersistenceUnit
    EntityManagerFactory emf;
    @Autowired
    LienpromotionRepository lienpromotionRepository;
    @Autowired
    ProfilRepository profilRepository;
    @Autowired
    PartenaireRepository partenaireRepository;
    @Autowired
    FileService fileService;
    @Autowired
    PromotionRepository promotionRepository;
    @Autowired
    UtilisateurRepository utilisateurRepository;
    @Autowired
    CommandeRepository commandeRepository;
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
    CommentaireRepository commentaireRepository;
    @Autowired
    DetailmodaliteretourRepository detailmodaliteretourRepository;
    @Autowired
    ImagesupplementRepository imagesupplementRepository;
    @Autowired
    JwtUtil jwtUtil;
    @Value("${app.firebase-config}")
    private String firebaseConfig;
    FirebaseApp firebaseApp;
    @Autowired
    TachesService tachesService;


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
            //System.out.println("IOException : "+e.toString());
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
    @Operation(summary = "Obtenir la liste des profils")
    @GetMapping(value="/getprofiliste")
    private List<Profil> getprofiliste(HttpServletRequest request){
        List<Object[]> liste = getUserId(request);
        Utilisateur ur = null;
        if(liste.size() > 0) {
            ur = utilisateurRepository.findByEmail(String.valueOf(liste.get(0)[2]));
        }

        Integer[] tabSup = {1,2};
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


    @CrossOrigin("*")
    @PostMapping(value={"/getmobileallsousproduitsarticles"})
    private List<Beansousproduitarticle> getmobileallsousproduitsarticles(@RequestBody RequeteBean rn){

        // ret :
        List<Beansousproduitarticle> ret = new ArrayList<>();

        List<Sousproduit> lte = sousproduitRepository.findAllByIdprd(rn.getIdprd());
        if(!lte.isEmpty()){
            List<Detail> lteDetail = detailRepository.findAllByIdsprIn(
                lte.stream().map(Sousproduit::getIdspr).collect(Collectors.toList())
            );
            if(!lteDetail.isEmpty()){
                List<Article> lteArticle = articleRepository.findAllByIddetIn(
                    lteDetail.stream().map(Detail::getIddet).collect(Collectors.toList())
                );

                if(!lteArticle.isEmpty()){
                    // Group by 'SOUS-PRODUIT'
                    lte.forEach(
                        s -> {
                            // DETAIL :
                            List<Detail> ltDt = lteDetail.stream().
                                    filter( a -> s.getIdspr() == a.getIdspr()).
                                    collect(Collectors.toList());

                            ltDt.forEach(
                                    d -> {
                                        // Get related ARTICLE :
                                        List<Article> ltArticle = lteArticle.stream().
                                                filter( a -> d.getIddet() == a.getIddet()).
                                                collect(Collectors.toList());
                                        if(!ltArticle.isEmpty()){
                                            //
                                            Beansousproduitarticle be = new Beansousproduitarticle();
                                            be.setDetail(s.getLibelle());
                                            ltArticle.forEach(
                                                    l -> {
                                                        Beanresumearticle br = new Beanresumearticle();
                                                        br.setIdart(l.getIdart());
                                                        br.setLienweb(l.getLienweb());
                                                        br.setLibelle(l.getLibelle());
                                                        br.setPrix(l.getPrix());
                                                        be.getListe().add(br);
                                                    }
                                            );

                                            // Check :
                                            /*Optional<Beansousproduitarticle> bet =
                                                    ret.stream().filter(
                                                                    r -> r.getDetail().equals(s.getLibelle())).
                                                            findFirst();
                                            Beansousproduitarticle tp = bet.orElse(null);*/
                                           Beansousproduitarticle bet =
                                                    ret.stream().filter(
                                                                    r -> r.getDetail().equals(s.getLibelle())).
                                                            findAny().orElse(null);
                                            if(bet!=null){
                                                ret.remove(bet);
                                                bet.getListe().addAll(be.getListe());
                                                ret.add(bet);
                                            }
                                            else{
                                                ret.add(be);
                                            }
                                        }
                                    }
                            );
                        }
                    );
                }
            }
        }

        return ret;
    }


    @CrossOrigin("*")
    @PostMapping(value={"/getmobilealldetailsarticles"})
    private List<Beansousproduitarticle> getmobilealldetailsarticles(@RequestBody RequeteBean rn){

        // ret :
        List<Beansousproduitarticle> ret = new ArrayList<>();

        List<Detail> lteDt = detailRepository.findAllByIdspr(rn.getIdprd());
        if(!lteDt.isEmpty()){
            List<Article> lteArticle = articleRepository.findAllByIddetIn(
                    lteDt.stream().map(Detail::getIddet).collect(Collectors.toList())
            );

            if(!lteArticle.isEmpty()){
                // Group by 'DETAIL'
                lteDt.forEach(
                        s -> {
                            // Get related ARTICLE :
                            List<Article> ltArticle = lteArticle.stream().
                                    filter( a -> s.getIddet() == a.getIddet()).
                                    collect(Collectors.toList());
                            if(!ltArticle.isEmpty()){
                                //
                                Beansousproduitarticle be = new Beansousproduitarticle();
                                be.setDetail(s.getLibelle());
                                be.setIddet(s.getIddet());
                                ltArticle.forEach(
                                        l -> {
                                            Beanresumearticle br = new Beanresumearticle();
                                            br.setIdart(l.getIdart());
                                            br.setLienweb(l.getLienweb());
                                            br.setLibelle(l.getLibelle());
                                            br.setPrix(l.getPrix());
                                            be.getListe().add(br);
                                        }
                                );
                                ret.add(be);
                            }
                        }
                );
            }
        }

        return ret;
    }


    @CrossOrigin("*")
    @PostMapping(value={"/getmobileallsousproduitsbyidprd"})
    private List<Beansousproduit> getmobileallsousproduitsbyidprd(@RequestBody RequeteBean rn){
        List<Sousproduit> lte = sousproduitRepository.findAllByIdprd(rn.getIdprd());
        List<Beansousproduit> ret = new ArrayList<>();
        lte.forEach(
            d -> {
                Beansousproduit bt = new Beansousproduit();
                bt.setIdspr(d.getIdspr());
                bt.setLibelle(d.getLibelle());
                bt.setLienweb(d.getLienweb());
                bt.setProduit("");
                ret.add(bt);
            }
        );
        return ret;
    }


    @CrossOrigin("*")
    @PostMapping(value={"/getmobilealldetailsbyidspr"})
    private List<Detail> getmobilealldetailsbyidspr(@RequestBody RequeteBean rn){
        return detailRepository.findAllByIdspr(rn.getIdprd());
    }


    // Get ARTICLES based on iddet :
    @CrossOrigin("*")
    @PostMapping(value={"/getarticlesbasedoniddet"})
    private List<BeanResumeArticleDetail> getarticlesbasedoniddet(@RequestBody RequeteBean rn){
        List<Article> lte = articleRepository.findAllByIddetAndChoix(rn.getIdprd(), 1);
        List<BeanResumeArticleDetail> ret = new ArrayList<>();
        lte.forEach(
                d -> {
                    // For each ARTICLE, pick the number of those bought :
                    List<Achat> articleAchete = achatRepository.findAllByIdartAndActif(d.getIdart(), 0);
                    BeanResumeArticleDetail bl = new BeanResumeArticleDetail();
                    Beanarticledetail be = new Beanarticledetail();
                    be.setIddet(d.getIddet());
                    be.setIdart(d.getIdart());
                    be.setLienweb(d.getLienweb());
                    be.setLibelle(d.getLibelle());
                    be.setPrix(d.getPrix());
                    // Find a promotion :
                    Lienpromotion ln = lienpromotionRepository.findByIdartAndEtat(d.getIdart(), 1);
                    Promotion pn = promotionRepository.findByIdprn(ln != null ? ln.getIdpro() : 0);
                    be.setReduction(pn != null ? pn.getReduction() : 0);
                    // Set NOTE :
                    List<Commentaire> comments = commentaireRepository.findAllByIdart(d.getIdart());
                    double noteArt = 0;
                    int totalComment = comments.isEmpty() ? 0 : comments.size();
                    if(!comments.isEmpty()){
                        noteArt = comments.stream().mapToInt(Commentaire::getNote).average().orElse(0);
                    }
                    be.setNote(0);
                    be.setArticlerestant( d.getQuantite() - (articleAchete != null ? articleAchete.size() : 0) );
                    bl.setNoteart(noteArt);
                    bl.setTotalcomment(totalComment);
                    bl.setBeanarticle(be);

                    // Add
                    ret.add(bl);
                }
        );
        return ret;
    }


    @CrossOrigin("*")
    @PostMapping(value={"/getmobilearticlesBasedonLib"})
    private List<BeanResumeArticleDetail> getmobilearticlesBasedonLib(@RequestBody RequestBean rn){

        // Sous-Produit lib
        Sousproduit st = sousproduitRepository.findByLibelle(rn.getLib());
        // Now get DETAIL
        List<Detail> lesDet = detailRepository.findAllByIdspr(st.getIdspr());
        // Then, ARTICLE :
        List<Article> lesArt = articleRepository.findAllByChoixAndIddetIn(1,
                lesDet.stream().map(Detail::getIddet).collect(Collectors.toList()));
        List<BeanResumeArticleDetail> ret = new ArrayList<>();
        lesArt.forEach(
                d -> {
                    // For each ARTICLE, pick the number of those bought :
                    List<Achat> articleAchete = achatRepository.findAllByIdartAndActif(d.getIdart(), 0);
                    BeanResumeArticleDetail bl = new BeanResumeArticleDetail();
                    Beanarticledetail be = new Beanarticledetail();
                    be.setIddet(d.getIddet());
                    be.setIdart(d.getIdart());
                    be.setLienweb(d.getLienweb());
                    be.setLibelle(d.getLibelle());
                    be.setPrix(d.getPrix());
                    // Find a promotion :
                    Lienpromotion ln = lienpromotionRepository.findByIdartAndEtat(d.getIdart(), 1);
                    Promotion pn = promotionRepository.findByIdprn(ln != null ? ln.getIdpro() : 0);
                    be.setReduction(pn != null ? pn.getReduction() : 0);
                    // Set NOTE :
                    List<Commentaire> comments = commentaireRepository.findAllByIdart(d.getIdart());
                    double noteArt = 0;
                    int totalComment = comments.isEmpty() ? 0 : comments.size();
                    if(!comments.isEmpty()){
                        noteArt = comments.stream().mapToInt(Commentaire::getNote).average().orElse(0);
                    }
                    be.setNote(0);
                    be.setArticlerestant( d.getQuantite() - (articleAchete != null ? articleAchete.size() : 0) );
                    bl.setNoteart(noteArt);
                    bl.setTotalcomment(totalComment);
                    bl.setBeanarticle(be);

                    // Add
                    ret.add(bl);
                }
        );
        return ret;
    }


    // Get ARTICLES based on iddet :
    @CrossOrigin("*")
    @PostMapping(value={"/getarticledetails"})
    private List<BeanArticlestatusresponse> getarticledetails(@RequestBody BeanArticlestatusrequest data){
        //System.out.println("On rentre");
        List<Article> lte = articleRepository.findAllByIdartIn(data.getArticleid());
        List<BeanArticlestatusresponse> ret = new ArrayList<>();
        lte.forEach(
                d -> {
                    // Set ARTICLE RESTANTS :
                    BeanArticlestatusresponse be = new BeanArticlestatusresponse();
                    be.setIdart(d.getIdart());
                    be.setRestant(d.getQuantite());
                    // Comments :
                    List<Commentaire> comments = commentaireRepository.findAllByIdart(d.getIdart());
                    double noteArt = 0;
                    int totalComment = comments.isEmpty() ? 0 : comments.size();
                    if(!comments.isEmpty()){
                        noteArt = comments.stream().mapToInt(Commentaire::getNote).average().orElse(0);
                    }
                    be.setNote(noteArt);
                    be.setTotalcomment(totalComment);
                    // Add
                    ret.add(be);
                }
        );
        //System.out.println("Taille : "+String.valueOf(ret.size()));
        return ret;
    }


    // Get ARTICLES based on iddet :
    @CrossOrigin("*")
    @PostMapping(value={"/managecustomer"})
    private BeanCustomerCreation managecustomer(@RequestBody Client ct){

        // Check if EMAIL is not
        BeanCustomerCreation rt = null;

        Client clt = null;
        if(ct.getIdcli() == 0){
            // new CUSTOMER :
            clt = clientRepository.findByEmail(ct.getEmail());
            if(clt != null){
                // Warn the MAIL exist already :
                rt = new BeanCustomerCreation();
                rt.setFlag(0); // Le MAIL existe déjà
                rt.setClt(ct);
            }
            else if(clientRepository.findByNumero(ct.getNumero()) != null){
                // Warn the NUMBER exist already :
                rt = new BeanCustomerCreation();
                rt.setFlag(1); // Le NUMERO existe déjà
                rt.setClt(ct);
            }
        }
        if(rt == null){
            // Process :
            rt = new BeanCustomerCreation();
            clt = clientRepository.findByIdcli(ct.getIdcli());
            if(clt == null) clt = new Client();
            clt.setNom(ct.getNom());
            clt.setPrenom(ct.getPrenom());
            clt.setEmail(ct.getEmail());
            clt.setNumero(ct.getNumero());
            clt.setCommune(ct.getCommune());
            clt.setAdresse(ct.getAdresse());
            clt.setGenre(ct.getGenre());
            clt.setFcmtoken(ct.getFcmtoken());
            String heure = new SimpleDateFormat("HH:mm").format(new Date());
            clt.setPwd(heure.replace(":", ""));

            //
            rt.setClt( clientRepository.save(clt));
            rt.setFlag(2);
            // Send Email to user :
            tachesService.mailCreation("Création de compte", ct.getEmail(),
                    heure.replace(":", ""));
        }

        //
        return rt;
    }


    @CrossOrigin("*")
    @PostMapping(value={"/authenicatemobilecustomer"})
    private BeanCustomerAuth authenicatemobilecustomer(@RequestBody BeanAuthentification data){

        // Check
        BeanCustomerAuth rt = new BeanCustomerAuth();
        Client ct = clientRepository.findByEmailAndPwd(data.getMail().trim(), data.getPwd().trim());
        if(ct == null){
            rt.setFlag(0);
            rt.setClt(null);
        }
        else {
            rt.setFlag(1);
            // Update TOKEN :
            ct.setFcmtoken(data.getFcmtoken());
            clientRepository.save(ct);
            rt.setClt(ct);
        }

        // Pick 'Commune' :
        rt.setCommune(communeRepository.findAllByOrderByLibelleAsc());

        //
        return rt;
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
                                   @RequestParam(name="idspr") Integer idspr,
                                   @RequestParam(name="iddet") Integer iddet
    ) {
        Detail dl = null;
        if(iddet > 0){
            // Delete the previous FILE :
            Bucket bucket = StorageClient.getInstance().bucket("gestionpanneaux.appspot.com");
            dl = detailRepository.findByIddet(iddet);
            boolean result =  bucket.get(dl.getLienweb()).delete();
        }

        // Set DETAIL :
        if(dl == null) dl = new Detail();
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
    @PostMapping("/savearticleandpromotion")
    public Reponse savearticleandpromotion(@RequestParam(name="id") Integer idart,
        @RequestParam(name="article", required = false) MultipartFile imgArticle,
        @RequestParam(name="actif") Integer actif,
        @RequestParam(name="idprn") Integer idprn,
        @RequestParam(name="nombrearticle") Integer nombrearticle,
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

        if(idprn > 0) {
            Lienpromotion ln = lienpromotionRepository.findByIdartAndIdpro(idart, idprn);
            if (ln == null) ln = new Lienpromotion();
            ln.setIdart(idart);
            ln.setIdpro(idprn);
            ln.setEtat(1);
            lienpromotionRepository.save(ln);
        }

        // Refresh article 'quantite' if needed :
        if(nombrearticle > 0){
            Article ale = articleRepository.findByIdart(idart);
            ale.setQuantite(nombrearticle);
            articleRepository.save(ale);
        }

        if (imgArticle != null) {
            String heure = new SimpleDateFormat("HH:mm:ss").format(new Date());
            fileService.upload(imgArticle, heure.replaceAll(":",""),
                    4, idart, null, null);
        }

        Reponse re = new Reponse();
        re.setElement("OK");
        re.setIdentifiant("OK");
        re.setProfil("OK");
        return  re;
    }


    @CrossOrigin("*")
    @PostMapping("/savemodalites")
    public Reponse savemodalites(@RequestParam(name="id") Integer iddtr,
                                @RequestParam(name="iddet") Integer iddet,
                                @RequestParam(name="modalite") String modalite,
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

        Detailmodaliteretour dr = new Detailmodaliteretour();
        dr.setIdent(ur.getIdent());
        dr.setIddet(iddet);
        dr.setCommentaire(modalite);
        detailmodaliteretourRepository.save(dr);

        Reponse re = new Reponse();
        re.setElement("OK");
        re.setIdentifiant("OK");
        re.setProfil("OK");
        return  re;
    }


    @CrossOrigin("*")
    @PostMapping("/savepromotion")
    public Reponse savepromotion(@RequestParam(name="id") long idprn,
                                 @RequestParam(name="datedebut") String datedebut,
                                 @RequestParam(name="datefin") String datefin,
                                 @RequestParam(name="libellepromotion") String libellepromotion,
                                 @RequestParam(name="reduction") Integer reduction,
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

        //
        Promotion pn = promotionRepository.findByIdprn(idprn);
        if(pn == null) pn = new Promotion();
        pn.setLibelle(libellepromotion);
        pn.setIdent(ur.getIdent());
        pn.setReduction(reduction);
        //
        try {
            Date dateDeb = new SimpleDateFormat("yyyy-MM-dd").
                    parse(datedebut);
            pn.setDatedebut(dateDeb);
            Date dateFin = new SimpleDateFormat("yyyy-MM-dd").
                    parse(datefin);
            pn.setDatefin(dateFin);
        }
        catch (Exception exc){
            pn.setDatedebut(null);
            pn.setDatefin(null);
        }
        // Save :
        pn.setEtat(1);
        promotionRepository.save(pn);

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


    // Get ARTICLES based on iddet :
    @CrossOrigin("*")
    @PostMapping(value={"/getmobilearticleinformationbyidart"})
    private Beanarticledatahistory getmobilearticleinformationbyidart(
            @RequestBody RequeteBeanArticle rn){
        // Get images :
        List<Imagesupplement> imagesSup = imagesupplementRepository.findAllByIdart(rn.getIdart());
        // Get Comments :
        List<Commentaire> comments = commentaireRepository.findAllByIdart(rn.getIdart());
        List<BeanCommentaireContenu> lescomments = new ArrayList<>();
        comments.forEach(
            d -> {
                BeanCommentaireContenu bu = new BeanCommentaireContenu();
                bu.setNote(d.getNote());
                bu.setCommentaire(d.getCommentaire());
                Client clt = clientRepository.findByIdcli(d.getIdcli());
                bu.setClient(clt != null ? (clt.getNom()+" "+clt.getPrenom()) : "---");
                String dte = new SimpleDateFormat("yyyy-MM-dd").format(d.getDates());
                bu.setDates(dte);
                lescomments.add(bu);
            }
        );

        // Get Article :
        Article ale = articleRepository.findByIdart(rn.getIdart());
        Beanarticledatahistory by = new Beanarticledatahistory();
        by.setArticle(ale.getLibelle());
        Partenaire pe = partenaireRepository.findByIdent(ale.getIdent());
        by.setEntreprise(pe.getLibelle());
        by.setContact(pe.getContact());
        // Get MODALITE RETOUR :
        Detailmodaliteretour dl =
            detailmodaliteretourRepository.findByIdentAndIddet(ale.getIdent(), ale.getIddet());
        by.setModaliteretour(dl != null ? dl.getCommentaire() : "---");
        by.setDescriptionproduit(ale.getDetail());
        by.setPrix(ale.getPrix());
        //
        by.setIddet(ale.getIddet());
        // This ONE should be come from 'COME' from 'COMMENTAIRE' table :
        by.setNote(0);
        // Find a promotion :
        Lienpromotion ln = lienpromotionRepository.findByIdartAndEtat(ale.getIdart(), 1);
        Promotion pn = promotionRepository.findByIdprn(ln != null ? ln.getIdpro() : 0);
        by.setReduction(pn != null ? pn.getReduction() : 0);
        by.setNombrearticle( ale.getQuantite() );
        //by.setNombrearticle(ale.getQuantite());
        // Add origin image
        if(imagesSup == null) imagesSup = new ArrayList<>();
        imagesSup.add(new Imagesupplement(0L, ale.getLienweb(), ale.getIdart()));
        //
        by.setImages(imagesSup);
        by.setComments(lescomments);
        // Check if user has already bought this ARTICLE :
        Commande cde = commandeRepository.findAllByIduserAndIdartAndLivre(
                rn.getIduser(), rn.getIdart(), 1).stream().findFirst().
                orElse(null);
        by.setAutorisecommentaire( cde != null ? 1 : 0);
        // Check if user has made COMMENTS
        Commentaire cte = commentaireRepository.findAllByIdartAndIdcli(
                rn.getIdart(), rn.getIduser()).stream().findFirst().orElse(null);
        by.setCommentaireexiste(cte != null ? 1 : 0);
        return by;
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


    /*
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
    }*/


    /*  */
    @CrossOrigin("*")
    @GetMapping(value="/getwebdetailbycompany")
    private List<Detail> getwebdetailbycompany(
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

        // Define Return LIST :
        List<Detail> lesDet = new ArrayList<>();

                // Get all ARTICLEs related to 'ENTREPRISE' :
        List<Article> lesArticles = articleRepository.findAllByIdent(ur.getIdent());
        if(lesArticles != null){
            List<Integer> lesDetailsId =
                    lesArticles.stream().mapToInt(Article::getIddet).boxed().collect(Collectors.toList());
            // Now get Details
            lesDet = detailRepository.findAllByIddetIn(lesDetailsId);
        }

        return lesDet;
    }


    /*  */
    @CrossOrigin("*")
    @GetMapping(value="/getwebdetailmodalitebycompany")
    private List<BeanDetailModalite> getwebdetailmodalitebycompany(
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

        // Define Return LIST :
        List<BeanDetailModalite> lesDet = new ArrayList<>();

        // Get all ARTICLEs related to 'ENTREPRISE' :
        List<Detailmodaliteretour> lesCmt =
                detailmodaliteretourRepository.findAllByIdent(ur.getIdent());
        if(lesCmt != null){
            lesCmt.forEach(
                d -> {
                    BeanDetailModalite be = new BeanDetailModalite();
                    be.setDetail(detailRepository.findByIddet(d.getIddet()).getLibelle());
                    be.setModalite(d.getCommentaire());
                    be.setIddtr(d.getIddtr());
                    lesDet.add(be);
                }
            );
            //
        }

        return lesDet;
    }



    @CrossOrigin("*")
    @GetMapping(value="/getwebcompanypromotion")
    private List<Beanpromotion> getwebcompanypromotion(
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

        //  Return LIST :
        List<Beanpromotion> lesData = new ArrayList<>();
        // Set the defaul value :
        Beanpromotion btn = new Beanpromotion();
        btn.setIdprn(0);
        btn.setReduction(0);
        btn.setLibelle("---");
        // Debut
        btn.setDatedebut("");
        btn.setDatefin("");
        lesData.add(btn);

        // Get all PROMOTION related to 'ENTREPRISE' :
        List<Promotion> lesPrm =
                promotionRepository.findAllByIdent(ur.getIdent());
        if(lesPrm != null){
            lesPrm.forEach(
                d -> {
                    Beanpromotion bn = new Beanpromotion();
                    bn.setIdprn(d.getIdprn());
                    bn.setReduction(d.getReduction());
                    bn.setLibelle(d.getLibelle());
                    // Debut
                    String dte =
                        new SimpleDateFormat("yyyy-MM-dd").format(d.getDatedebut());
                    bn.setDatedebut(dte);
                    dte = new SimpleDateFormat("yyyy-MM-dd").format(d.getDatefin());
                    bn.setDatefin(dte);
                    lesData.add(bn);
                }
            );
            //
        }

        return lesData;
    }


    @CrossOrigin("*")
    @PostMapping("/getarticlepromotion")
    public BeanArticleUpdate getarticlepromotion(@RequestParam(name="id") int idart,
                                 HttpServletRequest request
    ) {
        List<Lienpromotion> liste = lienpromotionRepository.findAllByIdart(idart);
        List<Promotion> lteProm =
            promotionRepository.findAllByIdprnIn(
                    liste.stream().mapToLong(Lienpromotion::getIdpro).boxed().collect(Collectors.toList()));
        List<Beanpromotion> ret = new ArrayList<>();
        lteProm.forEach(
            d -> {
                Beanpromotion bn = new Beanpromotion();
                bn.setIdprn(d.getIdprn());
                bn.setReduction(d.getReduction());
                bn.setLibelle(d.getLibelle());
                // Debut
                String dte =
                        new SimpleDateFormat("dd-MM-yyyy").format(d.getDatedebut());
                bn.setDatedebut(dte);
                dte = new SimpleDateFormat("dd-MM-yyyy").format(d.getDatefin());
                bn.setDatefin(dte);
                ret.add(bn);
            }
        );

        BeanArticleUpdate bp = new BeanArticleUpdate();
        bp.setPromotion(ret);
        Article ar = articleRepository.findByIdart(idart);
        bp.setQuantite(ar.getQuantite());
        bp.setActif(ar.getChoix());

        //
        return bp;
    }


    // Get ARTICLES based on iddet :
    @CrossOrigin("*")
    @PostMapping(value={"/getmobilepromotedarticles"})
    private List<BeanResumeArticleDetail> getmobilepromotedarticles(@RequestBody RequeteBean rn){

        List<Lienpromotion> articlePromoted =
                lienpromotionRepository.findAllByEtat(1);
        // Get ARTICLES :
        List<Article> lesArticles = new ArrayList<>();
        if(rn.getIdprd() == 0) {
            // Get the last SIX ARTICLES
            lesArticles = articleRepository.findFirst6ByIdartInOrderByIdartDesc(
                    articlePromoted.stream().map(Lienpromotion::getIdart).collect(Collectors.toList()));
        }
        else{
            // Get all PROMOTED ARTICLEs :
            lesArticles = articleRepository.findAllByIdartIn(
                    articlePromoted.stream().map(Lienpromotion::getIdart).collect(Collectors.toList()));
        }

        List<BeanResumeArticleDetail> ret = new ArrayList<>();
        lesArticles.forEach(
                d -> {
                    // For each ARTICLE, pick the number of those bought :
                    List<Achat> articleAchete = achatRepository.findAllByIdartAndActif(d.getIdart(), 0);
                    BeanResumeArticleDetail bl = new BeanResumeArticleDetail();
                    Beanarticledetail be = new Beanarticledetail();
                    be.setIddet(d.getIddet());
                    be.setIdart(d.getIdart());
                    be.setLienweb(d.getLienweb());
                    be.setLibelle(d.getLibelle());
                    be.setPrix(d.getPrix());
                    // Find a promotion :
                    Lienpromotion ln = lienpromotionRepository.findByIdartAndEtat(d.getIdart(), 1);
                    Promotion pn = promotionRepository.findByIdprn(ln != null ? ln.getIdpro() : 0);
                    be.setReduction(pn != null ? pn.getReduction() : 0);
                    // Set NOTE :
                    List<Commentaire> comments = commentaireRepository.findAllByIdart(d.getIdart());
                    double noteArt = 0;
                    int totalComment = comments.isEmpty() ? 0 : comments.size();
                    if(!comments.isEmpty()){
                        noteArt = comments.stream().mapToInt(Commentaire::getNote).average().orElse(0);
                    }
                    be.setNote(0);
                    be.setArticlerestant( d.getQuantite() - (articleAchete != null ? articleAchete.size() : 0) );
                    bl.setNoteart(noteArt);
                    bl.setTotalcomment(totalComment);
                    bl.setBeanarticle(be);

                    // Add
                    ret.add(bl);
                }
        );
        return ret;
    }


    // Get 3 last ARTICLES posted :
    @CrossOrigin("*")
    @GetMapping(value={"/getmobilerecentarticles"})
    private List<Beanarticledetail> getmobilerecentarticles(){

        // Get ARTICLES :
        List<Article> lesArticles = articleRepository.findFirst6ByOrderByIdartDesc();

        List<Beanarticledetail> ret = new ArrayList<>();
        lesArticles.forEach(
                d -> {
                    // For each ARTICLE, pick the number of those bought :
                    List<Achat> articleAchete = achatRepository.findAllByIdartAndActif(d.getIdart(), 0);
                    Beanarticledetail be = new Beanarticledetail();
                    be.setIddet(d.getIddet());
                    be.setIdart(d.getIdart());
                    be.setLienweb(d.getLienweb());
                    be.setLibelle(d.getLibelle());
                    be.setPrix(d.getPrix());
                    // Find a promotion :
                    Lienpromotion ln = lienpromotionRepository.findByIdartAndEtat(d.getIdart(), 1);
                    Promotion pn = promotionRepository.findByIdprn(ln != null ? ln.getIdpro() : 0);
                    be.setReduction(pn != null ? pn.getReduction() : 0);
                    be.setNote(0);
                    be.setArticlerestant( d.getQuantite() - (articleAchete != null ? articleAchete.size() : 0) );
                    // Add
                    ret.add(be);
                }
        );
        return ret;
    }
}
