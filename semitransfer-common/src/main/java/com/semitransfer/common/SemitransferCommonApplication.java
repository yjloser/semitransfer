package com.semitransfer.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.semitransfer"})
public class SemitransferCommonApplication {

    public static void main(String[] args) {
        SpringApplication.run(SemitransferCommonApplication.class, args);
    }
}
