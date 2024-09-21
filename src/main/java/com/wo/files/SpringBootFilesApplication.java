package com.wo.files;

import com.wo.files.storage.FileStorageService;
import com.wo.files.storage.StorageProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class SpringBootFilesApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootFilesApplication.class, args);
    }

    @Bean // Delete files and create folder
    CommandLineRunner init(FileStorageService fileStorageService) {
        return args -> {
            fileStorageService.deleteAll();
            fileStorageService.initializeStorage();
        };
    }

}
