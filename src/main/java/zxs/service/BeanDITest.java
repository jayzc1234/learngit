package zxs.service;

import zxs.pojo.Person;
import org.springframework.context.annotation.Bean;

/**
 * @author zc
 */
public interface BeanDITest {

    /**
     * 接口bean测试
     * @return person
     * @return
     */
    @Bean
    default Person person(){
        return new Person();
    }
}
