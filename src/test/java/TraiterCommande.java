import com.ankk.ecommerce.models.Commande;

import java.util.List;

public class TraiterCommande {


    public Integer coutCommande(int lte){
        //return lte.stream().mapToInt(Commande::getPrix).sum();
        return lte;
    }
}
