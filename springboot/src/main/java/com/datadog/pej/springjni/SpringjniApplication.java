package com.datadog.pej.springjni;


import datadog.opentracing.DDTracer;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class SpringjniApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringjniApplication.class, args);
    }



    @Bean(name="jniTracer")
    public Tracer tracer(){
        return DDTracer.builder().build();
    }



}
