package com.bankguard.banking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = "com.bankguard.banking")
public class BankguardApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankguardApplication.class, args);
        System.out.println("🚀 BankGuard Banking System Started Successfully!");
    }
}