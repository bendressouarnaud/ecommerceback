import com.ankk.ecommerce.models.Client;
import com.ankk.ecommerce.models.Commande;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClientTest {

    // A t t r i b u t e s :
    @Mock
    TraiterCommande traiterCommande;
    @InjectMocks
    TraiterCommandeTest traiterCommandeTest;


    /*@BeforeEach
    public void setUp() {
        //MockitoAnnotations.openMocks(this);

    }*/

    // M E T H O D S :
    @Disabled
    @Test
    public void nouveauClient(){

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

        when(traiterCommande.prixTotalCommande(lte)).thenReturn(10000);

        int result = traiterCommandeTest.computePrix(lte);

        verify(traiterCommande).prixTotalCommande(lte);
        assertEquals(result, 10000);

    }

}
