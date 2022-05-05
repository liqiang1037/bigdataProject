package com.example.fancy.service;


import com.example.fancy.bean.User;
import com.example.fancy.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Service
public class UserService {
    @Autowired
    UserMapper userMapper;

    public ModelAndView login(User user) {
        try {
            User userExistN = userMapper.findByName(user.getUsername());
            if (userExistN != null) {
                String userExistP = userMapper.findPswByName(user.getUsername());
                if (userExistP.equals(user.getPassword())) {
                    System.out.println(user.getUsername() + "用户登录成功，欢迎您！");
                    return new ModelAndView("family");

                } else {
                    return new ModelAndView("login");

                }
            } else {
                return new ModelAndView("regist");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ModelAndView("login");

    }



    public String regist(User user){
        try {
            User userExist=userMapper.findByName(user.getUsername());
            if (user.getUsername().equals("")){
                return "账户名不能为空";
            }else if (user.getPassword().equals("")){
                return "密码不能为空";
            }else if (userExist!=null) {
                return "账户已经存在";
            }else {
                userMapper.save(user);
                return "注册成功";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }


    public List<User> findAll(){
        List<User> list=userMapper.findAll();
        return list;
    }

    public void show(){

    }
}

