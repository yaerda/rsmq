package cn.fhou77.rsmq;

import cn.fhou77.rsmq.message.Message;
import cn.fhou77.rsmq.message.MessageProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestRsmq {

    @Autowired
    MessageProducer messageProducer;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void sendMessage() throws InterruptedException {
        sendDayMesg();
        sendNightMesg();
        // 观察 monitor doing_key
        TimeUnit.MINUTES.sleep(2);
    }

    public void sendDayMesg() throws InterruptedException {

        //发送正确消息
        for (int i = 8; i < 12; i++) {
            messageProducer.sendMessage(Message.DAY.TURN_DAY, String.valueOf(i));
            TimeUnit.MILLISECONDS.sleep(500);
        }

        //发送错误消息
        messageProducer.sendMessage(Message.DAY.TURN_DAY, "八点");
    }

    public void sendNightMesg() throws InterruptedException {
        for (int j = 20; j < 24; j++) {
            messageProducer.sendMessage(Message.DAY.TURN_NINGT, String.valueOf(j));
            TimeUnit.SECONDS.sleep(1);
        }
    }
}