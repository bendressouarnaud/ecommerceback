package com.ankk.ecommerce.mesobjets;

import com.ankk.ecommerce.models.Lienpromotion;
import com.ankk.ecommerce.models.Promotion;
import com.ankk.ecommerce.repositories.LienpromotionRepository;
import com.ankk.ecommerce.repositories.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.StoredProcedureQuery;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MesTaches {

    // A T T R I B U T E S :
    @Autowired
    JavaMailSender emailSender;
    @Autowired
    PromotionRepository promotionRepository;
    @Autowired
    LienpromotionRepository lienpromotionRepository;


    // M E T H O D S :
    // Tous les jours à MINUIT
    @Scheduled(cron="0 0 0 * * *", zone="Africa/Nouakchott")  //
    public void finPromotion(){
        try {
            // Get DATE :
            String dte = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Date date = new SimpleDateFormat("yyyy-MM-dd").
                    parse(dte);
            List<Promotion> lesProms = promotionRepository.findAllByDatefinIsLessThan(date);

            // Browse :
            for(Promotion pn : lesProms){
                // Update PROMOTION:
                pn.setEtat(0);
                promotionRepository.save(pn);
                // Update LIENS :
                List<Lienpromotion> lesLiens =
                        lienpromotionRepository.findAllByEtatAndIdpro(1, Math.toIntExact(pn.getIdprn()));
                for (Lienpromotion ln : lesLiens){
                    ln.setEtat(0);
                    lienpromotionRepository.save(ln);
                }
            }
        }
        catch (Exception exc){}
    }
}
