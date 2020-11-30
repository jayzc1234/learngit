package zxs.algorithm;

import zxs.service.PersonService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Component
public class Test {

    private static PersonService personService;

    public Test(PersonService personService){
        Test.personService =personService;
    }
    @PostConstruct
    public void init(){
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        System.out.println(personService);
    }
}
