package cn.fhou77.rsmq;

import cn.fhou77.rsmq.message.MessageProducer;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestRsmq {

    @Autowired
    MessageProducer messageProducer;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

}