package com.chen.demo.rest;

import ch.qos.logback.classic.Logger;
import com.chen.demo.SqlSessionFactoryUtil;
import com.chen.demo.model.User;
import com.google.gson.Gson;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.http.HttpStatus;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Chen on 16/5/29.
 */
@RestController
public class UserResourse {

    public static final String MAPPING = "com.chen.demo.mapping.userMapping";
    public static final String PAGE_COUNT = "100";

    @RequestMapping("/users/{id}")
    public User getUsersById(@PathVariable() String id) {
        SqlSession session = SqlSessionFactoryUtil.getSqlSessionFactory().openSession();
        String statement = MAPPING + ".getUser";//映射sql的标识字符串
        User user = session.selectOne(statement, id);
        LoggerFactory.getLogger(UserResourse.class).info("id = " + id);
        return user;
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json", value = "/users")
    public List<User> getAllUser(@RequestParam(value = "start", defaultValue = "0") int start, @RequestParam(value = "count", defaultValue = PAGE_COUNT) int count, @RequestParam(value = "sort", defaultValue = "id") String sort) {
        SqlSession session = SqlSessionFactoryUtil.getSqlSessionFactory().openSession();
        String statement = MAPPING + ".allUsers";//映射sql的标识字符串
        RowBounds bounds = new RowBounds(start, count);
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("sort", sort);
        return session.selectList(statement, hashMap, bounds);
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json", value = "/users")
    public void addUser(HttpServletResponse repsonse,@RequestBody() String body) {
        SqlSession session = SqlSessionFactoryUtil.getSqlSessionFactory().openSession();

        String statement = MAPPING + ".addUser";
        User user = new Gson().fromJson(body,User.class);
        session.insert(statement, user);
        session.commit();
        repsonse.setStatus(HttpStatus.CREATED.value());
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/users/{id}")
    public void setUser(@PathVariable() String id, @RequestBody() String body, HttpServletResponse response) {
        SqlSession session = SqlSessionFactoryUtil.getSqlSessionFactory().openSession();
        String statement = MAPPING + ".getUser";
        String updateStatement = MAPPING + ".updateUser";
        User temp = new Gson().fromJson(body,User.class);
        User user = session.selectOne(statement, String.valueOf(temp.getId()));
        if (user != null) {
            response.setStatus(HttpStatus.OK.value());
            if (!StringUtils.isEmpty(temp.getUsername())) {
                user.setUsername(temp.getUsername());
            }
            if (!StringUtils.isEmpty(temp.getSex())) {
                user.setSex(temp.getSex());
            }
            session.update(updateStatement, user);
            session.commit();
        } else {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/users/{id}")
    public void deleteUser(@PathVariable() int id) {
        SqlSession sqlSession = SqlSessionFactoryUtil.getSqlSessionFactory().openSession();
        String statement = MAPPING + ".deleteUser";
        sqlSession.delete(statement, id);
        sqlSession.commit();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/upload")
    public String uploadFile(@RequestParam("name") String name, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (name.contains("/")) {
            redirectAttributes.addFlashAttribute("message", "Folder separators not allowed");
            return "redirect:/";
        }
        if (name.contains("/")) {
            redirectAttributes.addFlashAttribute("message", "Relative pathnames not allowed");
            return "redirect:/";
        }

        if (!file.isEmpty()) {
            try {
                BufferedOutputStream stream = new BufferedOutputStream(
                        new FileOutputStream(new File(this.getClass().getClassLoader().getResource("").getPath() + "/" + name)));
                FileCopyUtils.copy(file.getInputStream(), stream);
                stream.close();
                redirectAttributes.addFlashAttribute("message",
                        "You successfully uploaded " + name + "!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("message",
                        "You failed to upload " + name + " => " + e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("message",
                    "You failed to upload " + name + " because the file was empty");
        }

        return "redirect:/";
    }
}
