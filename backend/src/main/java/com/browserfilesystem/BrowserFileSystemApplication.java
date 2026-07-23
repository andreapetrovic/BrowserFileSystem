package com.browserfilesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
/** Entry point that bootstraps the Spring Boot API and its MongoDB configuration. */
public class BrowserFileSystemApplication {

    /** Starts the application using Spring Boot's standard lifecycle. */
    public static void main(String[] args) {
        SpringApplication.run(BrowserFileSystemApplication.class, args);
    }
}
