package com.ankk.ecommerce.mesobjets;

import com.ankk.ecommerce.beans.BeanProcessMail;
import com.ankk.ecommerce.models.Client;
import com.ankk.ecommerce.models.Parametres;
import com.ankk.ecommerce.repositories.ArticleRepository;
import com.ankk.ecommerce.repositories.ClientRepository;
import com.ankk.ecommerce.repositories.ParametresRepository;
import com.ankk.ecommerce.repositories.PartenaireRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.List;

@Service
public class TachesService {

    // A t t r i b u t e s :
    @Autowired
    PartenaireRepository partenaireRepository;
    @Autowired
    ClientRepository clientRepository;
    @Autowired
    ArticleRepository articleRepository;
    @Autowired
    ParametresRepository parametresRepository;
    @Autowired
    JavaMailSender emailSender;
    @Value("${spring.mail.username}")
    private String expediteur;



    // M e t h o d s   :
    @Async
    public void notifyCustomerForOngoingCommand(int idcli, String objet, String dates, String heure){
        //
        Client ct = clientRepository.findByIdcli(idcli);
        if(objet.equals("4")){
            clientRepository.deleteByIdcli(ct.getIdcli());
        }

        //
        Message me = Message.builder()
                //.setTopic("/topics/"+mie.getCode())
                .setToken(ct.getFcmtoken())
                .putData("objet", objet)  // COMMANDE en cours de traiement
                .putData("dates", dates)
                .putData("heure", heure)
                .build();
        try {
            FirebaseMessaging.getInstance().send(me);
            //System.out.println("Notif envoyé");
        } catch (FirebaseMessagingException e) {
            //System.out.println("FirebaseMessagingException : "+e.toString());
        }
    }


    // MAIL :
    @Async
    public void mailCreation(String objet, String identifiant, String motpasse, int... args){
        Parametres parametres = parametresRepository.findByIdparam(1);
        if(parametres != null) {
            if (parametres.getAlertemail() == 1) {

                MimeMessage mimeMessage = emailSender.createMimeMessage();
                try {
                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true,
                            "utf-8");
                    StringBuilder contenu = new StringBuilder();
                    contenu.append("<h2> Informations relatives au compte </h2>");
                    contenu.append("<div><p>Identifiant : <span style='font-weight:bold;'>" + identifiant + "</span></p></div>");
                    contenu.append("<div><p>Mot de passe : <span style='font-weight:bold;'>" + motpasse + "</span></p></div>");
                    //if(args.length==0) contenu.append("<div><p>Lien de l'application : "+monUrl+"</p></div>");
                    //
                    helper.setText(String.valueOf(contenu), true);
                    helper.setTo(identifiant);
                    helper.setSubject(objet);
                    //helper.setBcc(carboncopie);
                    helper.setFrom(expediteur);
                    emailSender.send(mimeMessage);
                } catch (Exception exc) {
                    //
                }
            }
        }
    }


    @Async
    public void notifyCompany(List<BeanProcessMail> listeMail, Client client){
        Parametres parametres = parametresRepository.findByIdparam(1);
        if(parametres != null) {
            if (parametres.getAlertemail() == 1) {
                MimeMessage mimeMessage = emailSender.createMimeMessage();
                try {
                    for(BeanProcessMail bl : listeMail){
                        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true,
                                "utf-8");
                        StringBuilder contenu = new StringBuilder();
                        contenu.append("<h2>").append(client.getNom())
                                .append(" ").append(client.getPrenom()).append("</h2>");
                        contenu.append("<h3>Nouvelle commande</h3>");
                        contenu.append("<table border=\"1\"><tr><th>Illustration</th><th>libell&eacute;</th></tr>");
                        bl.getLienweb().forEach(
                            d -> {
                                contenu.append("<tr><td><img width=\"200\" height=\"180\" src='https://firebasestorage.googleapis.com/v0/b/gestionpanneaux.appspot.com/o/")
                                        .append(d.getImage()).append("?alt=media'/></td>");
                                contenu.append("<td>").append(d.getLibelle()).append("</td></tr>");
                            }
                        );
                        contenu.append("</table>");
                        helper.setText(String.valueOf(contenu), true);
                        // Get Compay :
                        //helper.setTo("ngbandamakonan@gmail.com");
                        helper.setTo(
                            partenaireRepository.findByIdent(bl.getIdent()).getEmail().trim());
                        helper.setSubject("Nouvelle commande");
                        helper.setBcc("bendressoukonan@gmail.com");
                        helper.setFrom(expediteur);
                        emailSender.send(mimeMessage);
                    }
                } catch (Exception exc) {
                    System.out.println("Exception : "+exc.getMessage());
                }
            }
        }
    }
}
