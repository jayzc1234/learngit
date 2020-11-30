package zxs.test.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class NoJavaBean {
    public int id=1;

    public String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void test(){
        System.out.println("test");
    }
}
