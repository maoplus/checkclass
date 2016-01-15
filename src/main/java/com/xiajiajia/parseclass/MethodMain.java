package com.xiajiajia.parseclass;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import static com.xiajiajia.parseclass.MethodUtil.getFilePath ;
import static com.xiajiajia.parseclass.MethodUtil.getResut ;
public class MethodMain {
    public static void main(String[] args) throws Exception {
        Path path = getFilePath();
        if (Files.isDirectory(path)) {
            List<String> list = Files.list(path).map(v -> {
                return getResut(v);
            }).collect(Collectors.toList());
            Files.write(Paths.get("f:/t/result.java"), list,
                    StandardCharsets.UTF_8);
        }
        else {
            //getResut(path);
            System.out.println(getResut(path));
        }
        
    }
}
