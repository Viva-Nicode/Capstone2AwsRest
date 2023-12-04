package com.azurelight.capstone_2.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/showMe")

public class StaticController {
    @GetMapping("/gimoring")
	public String moveSignupPage(Model model) {
		return "/gimoring";
	}
}
