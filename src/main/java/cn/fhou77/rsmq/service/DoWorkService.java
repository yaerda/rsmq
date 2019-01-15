package cn.fhou77.rsmq.service;

import cn.fhou77.rsmq.message.ConsumeInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DoWorkService implements ConsumeInterface {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * worker 的 实现
     *
     * @param content
     * @throws Exception
     */
    @Override
    public void dowork(String content) throws Exception {
        logger.info("Receive message ", Integer.valueOf(content));
    }
}
