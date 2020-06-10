package com.zxs;

import com.zxs.pojo.Person;
import com.zxs.service.ApplicationAwareTest;
import com.zxs.service.BeanDITest;
import com.zxs.service.TestService1;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.DispatcherServlet;

@SpringBootApplication
public class ApplicationBootApp implements BeanDITest {
	public static void main(String[] args) {
        ConfigurableApplicationContext contextc = SpringApplication.run(ApplicationBootApp.class, args);
        contextc.getEnvironment().setActiveProfiles("dev");
        Fly bean = contextc.getBean(Fly.class);
        System.out.println(bean);

    }

    @Component
    class Fly{

    }
}
