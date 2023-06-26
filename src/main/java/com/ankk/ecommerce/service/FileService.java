package com.ankk.ecommerce.service;

import com.ankk.ecommerce.models.Produit;
import com.ankk.ecommerce.models.Sousproduit;
import com.ankk.ecommerce.repositories.ProduitRepository;
import com.ankk.ecommerce.repositories.SousproduitRepository;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.UUID;

@Service
public class FileService {

    @Value("${app.firebase-config}")
    private String firebaseConfig;
    @Autowired
    SousproduitRepository sousproduitRepository;
    @Autowired
    ProduitRepository produitRepository;


    public void upload(MultipartFile multipartFile, String libproduit, int mode, int idprd) {
        try {
            String fileName = multipartFile.getOriginalFilename();                        // to get original file name
            fileName = UUID.randomUUID().toString().concat(this.getExtension(fileName));  // to generated random string values for file name.
            File file = this.convertToFile(multipartFile, fileName);                      // to convert multipartFile to File
            this.uploadFile(file, fileName, libproduit, mode, idprd);                                   // to get uploaded file link
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
            , int mode, int idprd) throws IOException {
        BlobId blobId = BlobId.of("gestionpanneaux.appspot.com", (fileName));
        //BlobId blobId = BlobId.of("gestionpanneaux.appspot.com", ("produits/"+fileName));
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("media").build();
        Credentials credentials = GoogleCredentials.fromStream(
                new FileInputStream("./src/main/resources/gestionpanneaux-firebase-adminsdk-q0rzg-0eef98bb76.json"));
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
                Sousproduit st = new Sousproduit();
                st.setLibelle(libproduit);
                st.setLienweb(lienweb);
                st.setIdprd(idprd);
                sousproduitRepository.save(st);
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
