import com.ankk.ecommerce.models.Commande;

import java.util.List;

public class TraiterCommande {


    public Integer prixTotalCommande(List<Commande> lte){
        return lte.stream().mapToInt(Commande::getPrix).sum();
    }
}
