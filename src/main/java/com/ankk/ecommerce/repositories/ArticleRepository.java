package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Article;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ArticleRepository extends CrudRepository<Article, Integer> {

    List<Article> findAllByIdent(int ident);
    List<Article> findAllByIdentAndIddet(int ident, int iddet);
    List<Article> findAllByIddet(int iddet);
    List<Article> findAllByIddetAndChoix(int iddet, int choix);
    Article findByIdart(int id);

}
