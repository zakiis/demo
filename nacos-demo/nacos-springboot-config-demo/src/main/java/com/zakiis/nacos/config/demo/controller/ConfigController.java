package com.zakiis.nacos.config.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.nacos.api.config.annotation.NacosValue;

@Controller
@RequestMapping("/config")
public class ConfigController {

	@NacosValue(value = "${useLocalCache:false}", autoRefreshed = true)
	private boolean useLocalCache;
	
	/**
	 * curl -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs?dataId=nacos-demo&group=DEFAULT_GROUP&content=useLocalCache=true"
	 * @return
	 */
	@RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
	public boolean get() {
		return useLocalCache;
	}
}
