package com.zakiis.nacos.config.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/config")
@RefreshScope
public class ConfigController {

	@Value(value = "${useLocalCache:false}")
	private boolean useLocalCache;
	
	@RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
	public boolean get() {
		return useLocalCache;
	}
}
