package com.edu.springboot.configurepropertiesdemo;

import com.edu.springboot.configurepropertiesdemo.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class ConfigurePropertiesDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigurePropertiesDemoApplication.class, args);
    }

}
