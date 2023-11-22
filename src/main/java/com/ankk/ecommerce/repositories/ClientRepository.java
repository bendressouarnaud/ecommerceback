package com.ankk.ecommerce.repositories;

import com.ankk.ecommerce.models.Client;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface ClientRepository extends CrudRepository<Client, Integer> {

    List<Client> findAll();
    List<Client> findAllByOrderByNomAsc();
    Client findByEmail(String email);
    Client findByEmailAndPwd(String email, String pwd);
    @Transactional
    long deleteByEmailAndPwd(String email, String pwd);
    @Transactional
    long deleteByIdcli(int idcli);
    Client findByNumero(String Numero);
    Client findByIdcli(int idcli);

}
