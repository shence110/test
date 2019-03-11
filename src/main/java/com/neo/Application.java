package com.neo;

import com.neo.util.SpringContextUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {

		ApplicationContext app = SpringApplication.run(Application.class, args);
		SpringContextUtil.setApplicationContext(app);
//		String str1=app.getEnvironment().getProperty("spring.datasource.bimall.driverClassName");
//		System.out.println(str1);
	}
}
