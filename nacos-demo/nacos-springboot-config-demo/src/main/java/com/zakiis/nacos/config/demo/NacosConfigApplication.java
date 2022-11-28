package com.zakiis.nacos.config.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;

/**
 * https://nacos.io/zh-cn/docs/quick-start-spring-boot.html
 * curl -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs?dataId=nacos-demo&group=DEFAULT_GROUP&content=useLocalCache=true"
 * @author 10901
 */
@SpringBootApplication
@NacosPropertySource(dataId = "nacos-demo", autoRefreshed = true)
public class NacosConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(NacosConfigApplication.class, args);
	}
}
