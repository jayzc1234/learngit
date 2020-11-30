package zxs.common;


import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by corbett on 2018/9/17.
 */


/**
 * @author Created by jgw136 on 2018/05/23.
 */
public class Md5Util {

    /**
     * 小写md5返回值
     * @param str
     * @return
     */
    public static String md5ToLower(String str) throws RuntimeException {

        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("获取md5对象失败");
        }
        try {
            md5.update((str).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException("待转字符串转字节数组失败");
        }
        byte b[] = md5.digest();
        int i;
        StringBuffer buf = new StringBuffer("");

        for(int offset=0; offset<b.length; offset++){
            i = b[offset];
            if(i<0){
                i+=256;
            }
            if(i<16){
                buf.append("0");
            }
            buf.append(Integer.toHexString(i));
        }

        return buf.toString();
    }

    /**
     * 获取大写md5值
     * @param str
     * @return
     * @throws RuntimeException
     */
    public static String md5ToUpper(String str) throws RuntimeException {
        return md5ToLower(str).toUpperCase();
    }


    public static void main(String[] args) {
        BigDecimal bigDecimal = new BigDecimal(9);
        BigDecimal bigDecimal2 = new BigDecimal(8.5);
        BigDecimal subtract = bigDecimal2.subtract(bigDecimal);
        double i = subtract.doubleValue();
        System.out.println(i);
    }
}

