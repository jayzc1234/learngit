package zxs.test.model;

import lombok.Data;

@Data
public class Person {
    private int id;

    private String name;

    public  Person(){
        this.id=1;
    }
}
