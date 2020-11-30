package zxs.profile;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("pro")
public class ProProfile {

    public ProProfile(){
        System.out.println("ProProfile");
    }
}
