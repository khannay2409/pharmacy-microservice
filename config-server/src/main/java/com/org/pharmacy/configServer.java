package com.org.pharmacy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class configServer {
    public static void main(String[] args) {
        SpringApplication.run(configServer.class, args);
    }
}