package com.chen.demo;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;

/**
 * Created by Chen on 16/5/29.
 */
public class SqlSessionFactoryUtil {

    private static SqlSessionFactory sSqlSessionFactory;

    static {
        //mybatis的配置文件
        String resource = "config.xml";
        //使用类加载器加载mybatis的配置文件（它也加载关联的映射文件）
        InputStream is = SqlSessionFactoryUtil.class.getClassLoader().getResourceAsStream(resource);
        //构建sqlSession的工厂
        sSqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
    }

    public static SqlSessionFactory getSqlSessionFactory() {
        return sSqlSessionFactory;
    }

}
