package com.zakiis.nacos.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.alibaba.nacos.spring.context.annotation.discovery.EnableNacosDiscovery;

/**
 * https://nacos.io/zh-cn/docs/quick-start-spring-boot.html
 * 1. simulate a service register in nacos server: curl -X POST 'http://127.0.0.1:8848/nacos/v1/ns/instance?serviceName=example&ip=127.0.0.1&port=8080'
 * 2. Browser request: http://localhost:8080/discovery/get?serviceName=example
 * @author 10901
 */
@SpringBootApplication
@EnableNacosDiscovery
public class NacosDiscoveryApplication {

	public static void main(String[] args) {
		SpringApplication.run(NacosDiscoveryApplication.class, args);
	}
}
