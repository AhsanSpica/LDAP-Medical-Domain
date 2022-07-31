package com.test.springldaptest.springldaptest;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

//@EnableWebMvc
//@ComponentScan("org.springframework.security.samples.mvc")
//@Configuration
public class WebMvcConfiguration implements  WebMvcConfigurer {

    // ...

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/newlogin").setViewName("newlogin");
       // registry.addViewController("/newlogin").setViewName("newlogin");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }
}