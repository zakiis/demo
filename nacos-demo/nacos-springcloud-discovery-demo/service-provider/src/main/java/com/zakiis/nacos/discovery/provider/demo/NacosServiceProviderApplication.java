package com.zakiis.nacos.discovery.provider.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * https://nacos.io/zh-cn/docs/quick-start-spring-cloud.html
 * @author 10901
 */
@SpringBootApplication
@EnableDiscoveryClient
public class NacosServiceProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(NacosServiceProviderApplication.class, args);
	}
	
}
