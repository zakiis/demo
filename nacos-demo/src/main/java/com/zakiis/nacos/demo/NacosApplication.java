package com.zakiis.nacos.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;

@SpringBootApplication
@NacosPropertySource(dataId = "nacos-demo", autoRefreshed = true)
public class NacosApplication {

	public static void main(String[] args) {
		SpringApplication.run(NacosApplication.class, args);
	}
}
