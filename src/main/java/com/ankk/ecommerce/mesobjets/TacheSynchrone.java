package com.ankk.ecommerce.mesobjets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class TacheSynchrone {

    @Bean(name="taskExecutor")
    public Executor taskExecutor(){
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(30); /* number of THREADS that will be running
        tasks in the pool */
        executor.setQueueCapacity(100);
        executor.setMaxPoolSize(200);/* number of tasks that  */
        executor.setThreadNamePrefix("MesThreads");
        executor.initialize();
        return executor;
    }

}
