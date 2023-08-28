package com.ankk.ecommerce.mesobjets;

import com.ankk.ecommerce.models.Client;
import com.ankk.ecommerce.repositories.ClientRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TachesService {

    // A t t r i b u t e s :
    @Autowired
    ClientRepository clientRepository;


    // M e t h o d s   :
    @Async
    public void notifyCustomerForOngoingCommand(int idcli, String dates, String heure){
        //
        Client ct = clientRepository.findByIdcli(idcli);

        //
        Message me = Message.builder()
                //.setTopic("/topics/"+mie.getCode())
                .setToken(ct.getFcmtoken())
                .putData("objet", "1")  // COMMANDE en cours de traiement
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
}
