package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.beans.Beansousproduitarticle;
import com.ankk.ecommerce.models.Article;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ArticleRepository extends CrudRepository<Article, Integer> {

    List<Article> findAllByIdent(int ident);
    List<Article> findAllByIdentAndIddet(int ident, int iddet);
    List<Article> findAllByIddet(int iddet);
    List<Article> findAllByIddetIn(List<Integer> liste);
    List<Article> findAllByIddetAndChoix(int iddet, int choix);
    Article findByIdart(int id);

    /*@Query(value = "select a.idspr,a.libelle as libsousprod,c.idart,c.libelle,c.lienweb,c.prix from " +
            "sousproduit a inner join detail b on a.idspr=b.idspr inner join article c on " +
            "c.iddet=b.iddet where a.idprd  = ?1 order by idart desc", nativeQuery = true)
    List<Beansousproduitarticle> findAllArticlesByIdprod(int idprd);*/

}
