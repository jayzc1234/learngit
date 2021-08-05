package com.zxs.test;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.xhtmlrenderer.swing.Java2DRenderer;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;

public class FreemarkerUtils {

    private static String getTemplate(String template, Map<String,Object> map) throws IOException, TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);
        String templatePath = FreemarkerUtils.class.getResource("/").getPath()+ "/templates/";
        cfg.setDirectoryForTemplateLoading(new File(templatePath));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        Template temp = cfg.getTemplate(template);
        StringWriter stringWriter = new StringWriter();
        temp.process(map, stringWriter);
        stringWriter.flush();
        stringWriter.close();
        String resutl = stringWriter.getBuffer().toString();
        return resutl;
    }

    public static void turnImage(String template, Map<String,Object> map, HttpServletResponse response) throws Exception {
        String html = getTemplate(template, map);

        byte[] bytes=html.getBytes();
        ByteArrayInputStream bin=new ByteArrayInputStream(bytes);
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        DocumentBuilder builder=factory.newDocumentBuilder();
        Document document=builder.parse(bin);
        Java2DRenderer renderer = new Java2DRenderer(document,600,800);
        BufferedImage img = renderer.getImage();
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", outputStream);
            byte[] bytes1 = outputStream.toByteArray();
            String encodeBase64String = Base64.encodeBase64String(bytes1);
        }finally {
            if (null != outputStream){
                outputStream.close();
            }
        }


    }
}