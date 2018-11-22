package cn.fhou77.rsmq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RsmqApplication {

    public static void main(String[] args) {
        SpringApplication.run(RsmqApplication.class, args);
    }
}