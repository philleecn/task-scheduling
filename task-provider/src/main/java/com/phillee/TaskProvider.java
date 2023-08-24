package com.phillee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TaskProvider {
    public static void main(String[] args) {
        SpringApplication.run(TaskProvider.class, args);
    }
}