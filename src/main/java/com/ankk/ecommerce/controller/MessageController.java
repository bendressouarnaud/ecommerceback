package com.ankk.ecommerce.controller;

import com.ankk.ecommerce.beans.BeanLigneOccurence;
import com.ankk.ecommerce.beans.BeanPageOccurence;
import com.ankk.ecommerce.beans.Reponse;
import com.ankk.ecommerce.models.Article;
import com.ankk.ecommerce.models.Utilisateur;
import com.ankk.ecommerce.service.FileService;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RestController
public class MessageController {

    // Attribute:
    @Autowired
    FileService fileService;


    @CrossOrigin("*")
    @PostMapping("/sendWordToRead")
    public List<BeanLigneOccurence> sendWordToRead(@RequestParam("brochure") MultipartFile multipartFile,
                                @RequestParam(name="expression") String libelle,
                                HttpServletRequest request
    ) {
        List<BeanLigneOccurence> retour = new ArrayList<>();
        try {
            File file = fileService.convertToFile(multipartFile,
                    multipartFile.getOriginalFilename());

            PdfReader reader = new PdfReader(file.getName());
            int pages = reader.getNumberOfPages();

            for (int i = 1; i <= pages; i++) {

                // New one :
                BeanPageOccurence be = new BeanPageOccurence();
                BeanLigneOccurence ble = new BeanLigneOccurence();

                // Now get 'lines' for current page
                String[] lignes = PdfTextExtractor.getTextFromPage(reader, i).split("\n");
                int cptLigne = 0;

                for(String line : lignes){
                    cptLigne++;
                    if(line.contains(libelle)){
                        ble.setPage("Page "+String.valueOf(i));
                        ble.setLigne("Ligne "+String.valueOf(cptLigne));
                        ble.setContenu(line);
                        // Track :
                        retour.add(ble);
                    }
                }
            }
            reader.close();
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

}
