package cn.fhou77.rsmq;

import cn.fhou77.rsmq.util.SpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RsmqApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(RsmqApplication.class, args);
        SpringUtil.setApplicationContext(context);
    }
}