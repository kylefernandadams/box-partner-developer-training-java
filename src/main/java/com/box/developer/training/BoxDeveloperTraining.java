package com.box.developer.training;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@SpringBootApplication
@EnableAutoConfiguration
public class BoxDeveloperTraining {

    public static void main(String[] args) {
        SpringApplication.run(BoxDeveloperTraining.class, args);
    }


    @RequestMapping("/box")
    public Map<Object, Object> index() {

        return Collections.singletonMap("response", "Welcome to Box Partner Developer Training!!!");
    }
}
