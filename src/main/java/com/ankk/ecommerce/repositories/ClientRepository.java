package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Client;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ClientRepository extends CrudRepository<Client, Integer> {

    List<Client> findAll();
    Client findByEmail(String email);
    Client findByIdcli(int idcli);

}