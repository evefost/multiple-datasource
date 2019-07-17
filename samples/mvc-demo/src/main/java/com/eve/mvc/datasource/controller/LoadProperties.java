package com.eve.mvc.datasource.controller;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;


public class LoadProperties implements ApplicationContextInitializer {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {

        try {
            ResourcePropertySource rps = new ResourcePropertySource(
                    applicationContext.getResource("classpath:application.properties"));
            applicationContext.getEnvironment().getPropertySources().addFirst(rps);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
