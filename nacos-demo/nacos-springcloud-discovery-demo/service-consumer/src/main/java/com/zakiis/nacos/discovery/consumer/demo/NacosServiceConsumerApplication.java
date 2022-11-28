package com.zakiis.nacos.discovery.consumer.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * https://nacos.io/zh-cn/docs/quick-start-spring-cloud.html
 * @author 10901
 */
@SpringBootApplication
@EnableDiscoveryClient
public class NacosServiceConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(NacosServiceConsumerApplication.class, args);
	}
	
	@LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
