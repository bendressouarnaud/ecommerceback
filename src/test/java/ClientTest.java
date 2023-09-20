import com.ankk.ecommerce.models.Client;
import com.ankk.ecommerce.models.Commande;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//@ExtendWith(MockitoExtension.class)
public class ClientTest {

    // A t t r i b u t e s :
    //@Mock
    //TraiterCommande traiterCommande;

    // M E T H O D S :
    @Test
    public void nouveauClient(){
        // 9
        Client ct = new Client();
        ct.setFcmtoken("");
        ct.setAdresse("17 RUE SAINT MARTIN");
        ct.setGenre(1);
        ct.setEmail("azerty@gmail.com");
        ct.setNumero("06276798");
        ct.setNom("YAO");
        ct.setPrenom("Koffi");
        ct.setCommune(1);
        ct.setPwd("1234");

        // Set Commande :
        Commande cmd = new Commande();
        cmd.setTraite(0);
        cmd.setTotal(1);
        cmd.setDisponible(1);
        cmd.setLivre(0);
        cmd.setDates(null);
        cmd.setEtat(0);
        cmd.setEmission(0);
        cmd.setHeure(null);
        cmd.setIdart(1);
        cmd.setPrix(10000);

        //
        List<Commande> lte = new ArrayList<>();
        lte.add(cmd);

        //when(traiterCommande.coutCommande(lte)).thenReturn(10000);

        //verify(traiterCommande).coutCommande(lte);

        assertEquals(ct.getPwd(), "1234");

    }

}
