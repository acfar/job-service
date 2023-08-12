package com.fauzi.jobservice.configuration.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(getApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.fauzi.jobservice"))
                .build();
    }

    public ApiInfo getApiInfo(){
        return new ApiInfoBuilder()
                .title("Job Management System API")
                .description("This Page is for list documentation API in job management system")
                .version("1.0.0")
                .contact(new Contact("Achmad Fauzi R", "http://google.com","achmadfauzi41ui@gmail.com"))
                .license("Open API")
                .build();
    }
}

