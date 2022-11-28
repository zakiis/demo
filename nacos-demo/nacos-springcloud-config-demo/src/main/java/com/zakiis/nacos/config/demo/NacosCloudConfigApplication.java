package com.zakiis.nacos.config.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * https://nacos.io/zh-cn/docs/quick-start-spring-cloud.html
 * dataId生成格式：${prefix}-${spring.profiles.active}.${file-extension}
 * - prefix 默认为 spring.application.name 的值，也可以通过配置项 spring.cloud.nacos.config.prefix来配置。
 * - spring.profiles.active 为空时，对应的连接符 - 也将不存在，dataId 的拼接格式变成 ${prefix}.${file-extension}
 * - file-exetension 为配置内容的数据格式，可以通过配置项 spring.cloud.nacos.config.file-extension 来配置。目前只支持 properties(默认) 和 yaml 类型
 * 1. 调用nacos api: curl -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs?dataId=nacos-springboot-config-demo.properties&group=DEFAULT_GROUP&content=useLocalCache=true"
 * 2. 浏览器访问 localhost:8080/config/get看效果
 * @author 10901
 */
@SpringBootApplication
public class NacosCloudConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(NacosCloudConfigApplication.class, args);
	}
}
