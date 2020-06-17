package com.zxs.test;

import com.github.xiaoymin.swaggerbootstrapui.util.CommonUtils;
import com.zxs.test.interfce.BasePrint;
import com.zxs.test.interfce.BasePrintImp;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Test1 {
    public static void main(String[] args) throws IOException, NoSuchMethodException {

        String s1 = new StringBuilder().append("ja").append("va1").toString();
        String s2 = s1.intern();
        System.out.println(s2 == s1);

        String s5 = "dmz";
        String s3 = new StringBuilder().append("d").append("mz").toString();
        String s4 = s3.intern();
        System.out.println(s3 == s4);
    }

    private static void readFile(File file) throws IOException {
        String filePath2="F:\\dao1";
        boolean directory = file.isDirectory();
        if (directory){
            File[] files = file.listFiles();
            for (File file1 : files) {
                readFile(file1);
            }
        }else {
            String parent = file.getParent();
            String substring1="";
            if (parent.length()>"F:\\dao".length()){
                substring1 = parent.substring("F:\\dao".length() + File.separator.length(), parent.length());
            }
            BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String newFilePath =filePath2+File.separator+substring1;
            File newFile=new File(newFilePath);
            if (!newFile.exists()){
                newFile.mkdirs();
            }

            BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(newFile,file.getName()))));
            String text=null;
            StringBuilder lineTextBuilder=new StringBuilder();
            StringBuilder fieldTextBuilder=new StringBuilder();
            boolean stop=false;
            while ((text=reader.readLine())!=null){
              if (text.contains("\"")){
                  int index = text.indexOf('"');
                  lineTextBuilder.append(text.substring(0,index));
                  String substring = text.substring(index, text.length());
                  char[] chars = substring.toCharArray();
                  //判断双引号是否闭合，默认第一个字符是一个双引号未闭合
                  boolean closure=false;
                  for (int i=0;i<chars.length;i++) {
                      char aChar = chars[i];
                      if (i==0){
                          lineTextBuilder.append(aChar);
                          continue;
                      }
                      //遇到双引号则判断之前的状态，如果之前出现过一个则现在闭合
                      if (aChar=='"'){
                          if (closure){
                              closure=false;
                          }else {
                              closure=true;
                          }
                      }
                      boolean spaceChar = Character.isSpaceChar(chars[i-1]);
                      //遇到数据库字段开头
                      if (aChar>='A' && aChar<='Z' && (spaceChar || chars[i-1]=='.'|| chars[i-1]=='"'|| chars[i-1]==','|| chars[i-1]=='>'|| chars[i-1]=='(')){
                          if (i<text.length()-1 && !(chars[i+1]>='A' &&chars[i+1]<='Z') && !closure){
                              fieldTextBuilder.append(aChar);
                              stop=true;
                          }else {
                              lineTextBuilder.append(aChar);
                          }
                      }else {
                          //没有遇到数据库字段开头
                          if (stop){
                             //处于遍历数据库字段中,判断下一个是否是空格是的话则则设置stop为false
                              boolean spaceChar2 = Character.isSpaceChar(chars[i]);
                              if (spaceChar2 || aChar==','|| aChar=='='|| aChar=='"'){
                                  stop=false;
                                  String camelName = transferCamelName(fieldTextBuilder.toString());
                                  lineTextBuilder.append(camelName);
                                  lineTextBuilder.append(aChar);
                                  fieldTextBuilder.delete(0,fieldTextBuilder.length());
                              }else {
                                  fieldTextBuilder.append(aChar);
                              }
                          }else{
                              lineTextBuilder.append(aChar);
                          }
                      }
                  }
                  String s = lineTextBuilder.toString();
                  writer.write(s);
                  writer.flush();
                  writer.newLine();
                  lineTextBuilder.delete(0,lineTextBuilder.length());
              }else{
                  writer.write(text);
                  writer.flush();
                  writer.newLine();
              }
            }
            writer.close();
            reader.close();
        }
    }

    private static String transferCamelName(String name) {
        char[] chars = name.toCharArray();
        StringBuilder stringBuilder=new StringBuilder();
        boolean isFirst=true;
        for (char aChar : chars) {
            if (aChar>='A' && aChar<='Z'){
                if (isFirst){
                    stringBuilder.append(Character.toLowerCase(aChar));
                    isFirst=false;
                }else {
                    stringBuilder.append("_").append(Character.toLowerCase(aChar));
                }
            }else {
                stringBuilder.append(aChar);
            }
        }
        return stringBuilder.toString();
    }

}
