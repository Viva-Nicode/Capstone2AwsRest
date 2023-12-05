package com.azurelight.capstone_2.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Rest")
public class RestApiTestController {
    
    @GetMapping("/getHelloworld")
	public String moveSignupPage() {
		return "hello world";
	}
}