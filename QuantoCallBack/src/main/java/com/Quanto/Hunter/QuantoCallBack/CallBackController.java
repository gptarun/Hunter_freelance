package com.Quanto.Hunter.QuantoCallBack;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CallBackController {
	@RequestMapping("/hello")
	public String hello() {
		return "Hello!";
	}
}
