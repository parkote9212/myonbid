package com.pgc.myonbid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MyonbidApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyonbidApplication.class, args);
    }

}
