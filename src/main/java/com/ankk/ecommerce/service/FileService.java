package com.ankk.ecommerce.service;

import com.ankk.ecommerce.models.*;
import com.ankk.ecommerce.repositories.*;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class FileService {

    @Value("${app.firebase-config}")
    private String firebaseConfig;
    @Autowired
    ArticleRepository articleRepository;
    @Autowired
    DetailRepository detailRepository;
    @Autowired
    SousproduitRepository sousproduitRepository;
    @Autowired
    ProduitRepository produitRepository;
    @Autowired
    ImagesupplementRepository imagesupplementRepository;
    @Autowired
    ResourceLoader resourceLoader;


    public void upload(MultipartFile multipartFile, String libproduit, int mode, int idprd,
                       Article articles, Detail detail) {
        try {
            String fileName = multipartFile.getOriginalFilename();                        // to get original file name
            fileName = UUID.randomUUID().toString().concat(this.getExtension(fileName));  // to generated random string values for file name.
            File file = this.convertToFile(multipartFile, fileName);                      // to convert multipartFile to File
            this.uploadFile(file, fileName, libproduit, mode, idprd, articles, detail);                                   // to get uploaded file link
            file.delete();                                                                // to delete the copy of uploaded file stored in the project folder
        } catch (Exception e) {
            System.out.println("Exception : " + e.toString());
        }
    }

    public String download(String fileName) throws IOException {
        String destFileName = UUID.randomUUID().toString().concat(this.getExtension(fileName));     // to set random strinh for destination file name
        String destFilePath = "Z:\\New folder\\" + destFileName;                                    // to set destination file path
        Credentials credentials = GoogleCredentials.fromStream(new FileInputStream(firebaseConfig));
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        Blob blob = storage.get(BlobId.of("gestionpanneaux.appspot.com", fileName));
        blob.downloadTo(Paths.get(destFilePath));
        return "OK";
    }


    private void uploadFile(File file, String fileName, String libproduit
            , int mode, int idprd, Article art, Detail det) throws IOException {
        BlobId blobId = BlobId.of("gestionpanneaux.appspot.com", (fileName));
        //BlobId blobId = BlobId.of("gestionpanneaux.appspot.com", ("produits/"+fileName));
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("media").build();
        /*Credentials credentials = GoogleCredentials.fromStream(
            new FileInputStream(
            "./src/main/resources/gestionpanneaux-firebase-adminsdk-q0rzg-0eef98bb76.json"));*/
        Credentials credentials = GoogleCredentials.fromStream(
                new ClassPathResource(firebaseConfig).getInputStream());
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

        // Get the file Web's name from FIREBASE STORAGE :
        String lienweb = storage.create(blobInfo, Files.readAllBytes(file.toPath())).getName();
        switch (mode){
            case 0:
                // Produit
                Produit pt = new Produit();
                pt.setLibelle(libproduit);
                pt.setLienweb(lienweb);
                produitRepository.save(pt);
                break;

            case 1:
                // Sous-Produit
                Sousproduit st = new Sousproduit();
                st.setLibelle(libproduit);
                st.setLienweb(lienweb);
                st.setIdprd(idprd);
                sousproduitRepository.save(st);
                break;

            case 2:
                // Article
                Article ar = new Article();
                ar.setLibelle(art.getLibelle());
                ar.setDetail(art.getDetail());
                ar.setIdent(art.getIdent());
                ar.setIddet(art.getIddet());//
                ar.setPrix(art.getPrix());
                ar.setLienweb(lienweb);
                ar.setQuantite(0);
                ar.setChoix(1);
                // Set date :
                try{
                    String dte = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    Date dateAujourdhui = new SimpleDateFormat("yyyy-MM-dd").parse(dte);
                    ar.setPublication(dateAujourdhui);
                }
                catch (Exception e){
                    ar.setPublication(null);
                }
                articleRepository.save(ar);
                break;

            case 3:
                // Detail :
                //Detail dl = new Detail();
                //dl.setIdspr(det.getIdspr());
                //dl.setLibelle(det.getLibelle());
                //dl.setLienweb(lienweb);
                //
                det.setLienweb(lienweb);
                detailRepository.save(det);
                break;

            case 4:
                // Add it in 'imagesupplement' table :
                Imagesupplement it = new Imagesupplement();
                it.setIdart(idprd);
                it.setLienweb(lienweb);
                imagesupplementRepository.save(it);
                break;
        }

        //return String.format("DOWNLOAD_URL", URLEncoder.encode(fileName, StandardCharsets.UTF_8));
    }

    private File convertToFile(MultipartFile multipartFile, String fileName) throws IOException {
        File tempFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
            fos.close();
        }
        return tempFile;
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
