package zxs.profile;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestProfile {

    public TestProfile(){
        System.out.println("TestProfile");
    }
}
