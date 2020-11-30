package zxs.test.other;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternTest {
    public static void main(String[] args) {
        String reg=".*/hydra-human-construction";
        Pattern pattern=Pattern.compile(reg);
        Matcher matcher=pattern.matcher("/hydra-human-construction");
        boolean b = matcher.find();
        System.out.println(b);

    }
}
