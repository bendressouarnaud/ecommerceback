import com.ankk.ecommerce.models.Commande;
import lombok.RequiredArgsConstructor;

import java.util.List;


public class TraiterCommandeTest {

    // Attribute
    TraiterCommande traiterCommande;

    public TraiterCommandeTest() {
        traiterCommande = new TraiterCommande();
    }

    // Methode
    public int computePrix(List<Commande> liste){
        return traiterCommande.prixTotalCommande(liste);
    }


}
