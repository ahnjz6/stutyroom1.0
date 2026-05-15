package org.example.studyroom1;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.studyroom1.mapper")
public class Studyroom1Application {

    public static void main(String[] args) {
        SpringApplication.run(Studyroom1Application.class, args);
    }

}
