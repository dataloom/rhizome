package com.geekbeast.rhizome.tests.pods;

import java.util.List;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

@Configuration
@ComponentScan(
        basePackages="com.geekbeast.rhizome.tests.controllers" , 
        includeFilters = @ComponentScan.Filter( 
                value = {
                    org.springframework.stereotype.Controller.class , 
                } ,
                type = FilterType.ANNOTATION 
                )
        )
public class RESTfulServletPod extends WebMvcConfigurationSupport {
    @Override
    protected void configureMessageConverters( List<HttpMessageConverter<?>> converters ) {
        super.addDefaultHttpMessageConverters( converters );
        for( HttpMessageConverter<?> converter : converters ) {
            if( converter instanceof MappingJackson2HttpMessageConverter ) {
                MappingJackson2HttpMessageConverter jacksonConverter = ( MappingJackson2HttpMessageConverter ) converter;
                ObjectMapper springMapper = jacksonConverter.getObjectMapper();
                springMapper.registerModule( new AfterburnerModule() );
                springMapper.registerModule( new GuavaModule() );
            }
        }
    }
    
}