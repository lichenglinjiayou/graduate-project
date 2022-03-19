package com.lichenglin.gulimallssoclient.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HelloController {



    @GetMapping("/hello")
    @ResponseBody
    public String hello(){
        return "hello";
    }

    @GetMapping("/employees")
    public String getEmployees(Model model, HttpSession session, HttpServletRequest request,
                               @RequestParam(value = "token",required = false) String token){
        if(token != null){
            List<Employee> employeeList = new ArrayList();
            employeeList.add(new Employee("张三",20));
            employeeList.add(new Employee("李四",20));
            model.addAttribute("data",employeeList);
            return "list";
        }else{
            //若未登录，则跳转登录服务器进行登录
            return "redirect:http://localhost:30001/login.html"+"?redirect_url="+request.getRequestURL();
        }
    }
}

@Data
@AllArgsConstructor
class Employee{
    public String name;
    public Integer age;
}


@Data
@AllArgsConstructor
class User implements Serializable {
    public String username;
    public String password;
}

