package com.aalto.myBBS.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/test")
public class AlphaController {
    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello() {
        return "Hello";
    }
}
