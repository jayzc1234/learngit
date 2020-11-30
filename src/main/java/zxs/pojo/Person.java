package zxs.pojo;

import org.springframework.core.Ordered;

public class Person implements Ordered {
  private int id;
  
  private String name;

  public Person(){

  }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Person){
          return name.equals(((Person) obj).name);
      }
        return false;
    }

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

  @Override
  public int getOrder() {
    return 1;
  }
}
