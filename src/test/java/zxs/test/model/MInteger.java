package zxs.test.model;

import lombok.Data;

import java.lang.annotation.Native;
@Data
public class MInteger  {
    public int index;

    public  MInteger(int index){
        this.index=index;
    }
}
