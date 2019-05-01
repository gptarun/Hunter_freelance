package com.quanto.extrace;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 
 * @author tarun
 * @since 10-Apr-2019
 *
 */
@Controller
public class CallBackController {

	@RequestMapping("/")
	public String homePage() {
		return "login";
	}
	
	@RequestMapping("/instantor")
	public String instantorPage() {
		return "instantor";
	}
	
	@RequestMapping("/hunter")
	public String hunterPage() {
		return "hunter";
	}

}
