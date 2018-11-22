package cn.fhou77.rsmq.service;

import cn.fhou77.rsmq.message.worker.base.WorkerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NightService implements WorkerImpl {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     *  worker 的 实现
     * @param content
     * @throws Exception
     */
    @Override
    public void gogogo(String content) throws Exception {
        logger.info("Receive Turn_Night message at {} o'clock.", content);
    }
}
