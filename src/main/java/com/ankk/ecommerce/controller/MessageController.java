package com.ankk.ecommerce.controller;

import com.ankk.ecommerce.beans.BeanLigneOccurence;
import com.ankk.ecommerce.beans.BeanPageOccurence;
import com.ankk.ecommerce.beans.Reponse;
import com.ankk.ecommerce.models.Article;
import com.ankk.ecommerce.models.Utilisateur;
import com.ankk.ecommerce.service.FileService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@RestController
public class MessageController {

    // Attribute:
    @Autowired
    FileService fileService;
    //File directoryPath = null;
    File[] fichiers = null;
    Iterator<File> iterator = null;


    @CrossOrigin("*")
    @PostMapping("/sendWordToRead")
    public List<BeanLigneOccurence> sendWordToRead(
            //@RequestParam("brochure") MultipartFile multipartFile,
                                @RequestParam(name="expression") String libelle,
                                HttpServletRequest request
    ) {
        List<BeanLigneOccurence> retour = new ArrayList<>();
        try {
            for(File file : fichiers) {
                PdfReader reader = new PdfReader(file.getAbsolutePath().toString());
                int pages = reader.getNumberOfPages();

                for (int i = 1; i <= pages; i++) {
                    // New one :
                    BeanLigneOccurence ble = new BeanLigneOccurence();

                    if(libelle.length() > 81){
                        String tp = PdfTextExtractor.getTextFromPage(reader, i);
                        if(tp.replaceAll("\n", "").contains(libelle)){
                            ble.setPage("Page "+String.valueOf(i));
                            ble.setLigne("---");
                            ble.setTitre(file.getName());
                            ble.setContenu(libelle);
                            // Track :
                            retour.add(ble);
                        }
                    }
                    else{
                        // Now get 'lines' for current page
                        String[] lignes = PdfTextExtractor.getTextFromPage(reader, i).split("\n");
                        int cptLigne = 0;

                        for(String line : lignes){
                            cptLigne++;
                            if(line.contains(libelle)){
                                ble.setPage("Page "+String.valueOf(i));
                                ble.setLigne("Ligne "+String.valueOf(cptLigne));
                                ble.setTitre(file.getName());
                                ble.setContenu(line);
                                // Track :
                                retour.add(ble);
                            }
                        }
                    }
                }
                reader.close();
            }
        }
        catch (Exception exc){
            System.out.println("exception : "+exc.toString());
        }

        if(retour.isEmpty()){
            BeanLigneOccurence ble = new BeanLigneOccurence();
            ble.setPage("");
            ble.setLigne("");
            ble.setContenu("");
            retour.add(ble);
        }
        return retour;
    }


    @PostConstruct
    private void initializeObjects() {
        try {

            File directoryPath = new File("C:\\Users\\ngbandamakonan\\Documents\\pdf");
            //List of all files and directories
            //File[] filesList = directoryPath.listFiles();

            //ClassLoader loader = Thread.currentThread().getContextClassLoader();
            //URL url = loader.getResource("pdf");
            //String path = url.getPath();
            //fichiers = new File(path).listFiles();
            fichiers = directoryPath.listFiles();
            System.out.println("Fichiers : "+ String.valueOf(fichiers.length));
        } catch (Exception e) {
            System.out.println("Exception : "+e.toString());
        }
    }

}
