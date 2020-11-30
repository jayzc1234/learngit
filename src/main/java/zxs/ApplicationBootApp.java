package zxs;

import zxs.config.ApolloConfigBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

@EnableAspectJAutoProxy
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
//@EnableApolloConfig
public class ApplicationBootApp {

	public static void main(String[] args)  {
        ConfigurableApplicationContext contextc = SpringApplication.run(ApplicationBootApp.class, args);
        ApolloConfigBean bean = contextc.getBean(ApolloConfigBean.class);
    }

    @Component
    class Fly{
    }
}
