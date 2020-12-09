package com.zxs.test.jvm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CustomClassLoader extends ClassLoader {

    private static final String FILE_SEPARATOR= File.separator;

    private static  String CUSTOM_CLASS_PATH ;

    private static final String DEFAULT_CLASS_PATH = "F:\\eworkspace\\zxs-springboot\\target\\classes";

    public CustomClassLoader(String customClassPath) {
        CUSTOM_CLASS_PATH = customClassPath;
        if (null == CUSTOM_CLASS_PATH){
            CUSTOM_CLASS_PATH = DEFAULT_CLASS_PATH;
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> aClass = findClass(name);
        if (null == aClass){
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            aClass = contextClassLoader.loadClass(name);
        }
        return aClass;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.contains("com.pojo")){
            try {
                String replace = name.replace(".", FILE_SEPARATOR)+".class";
                Path path = Paths.get(CUSTOM_CLASS_PATH, replace);
                byte[] bytes = Files.readAllBytes(path);
                return defineClass(name, bytes, 0, bytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
