package com.ankk.ecommerce.mesobjets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling  // permet d'activer la planification des taches
public class TachesPanifiees {

    @Bean
    public MesTaches tache(){
        return new MesTaches();
    }

}
