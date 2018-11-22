package cn.fhou77.rsmq.message;

import cn.fhou77.rsmq.config.RedisPool;
import cn.fhou77.rsmq.message.worker.DayWorker;
import cn.fhou77.rsmq.message.worker.NightWorker;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

@Component
public class MessageConsumer implements ApplicationRunner {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static List<String> DOING_LIST = new ArrayList<>();

    private Jedis jedis = RedisPool.getJedis();

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    DayWorker dayWorker;

    @Autowired
    NightWorker nightWorker;

    @Scheduled(cron = "${redis.mq.monitor}")
    private void monitor() {
        logger.info("监控执行队列{}：", JSON.toJSONString(DOING_LIST));
        for (String doing_key : DOING_LIST) {
            reloadDoingToPending(doing_key);
        }
    }

    /**
     * 将 长时间积压(连接挂掉，消费缓慢等原因)在 doing_key 队列中的消息 重新释放到 pending_key 队列中消费
     *
     * @param doing_key
     */
    private void reloadDoingToPending(String doing_key) {
        try {
            List<String> doingList = jedis.lrange(doing_key, 0, -1);
            logger.info("执行队列数量[{}]: {}", doing_key, doingList.size());
            logger.info("执行队列内容[{}]: {}", doing_key, JSON.toJSONString(doingList));

            String pending_key = Message.turnDoingKeyToPendingKey(doing_key);
            if (!doingList.isEmpty()) {
                logger.info("[Pending_Key][{}]重新消费消息", pending_key);
                doingList.forEach(o -> jedis.rpoplpush(doing_key, pending_key));
            }
        } catch (Exception e) {
            logger.error("消息重新发送出错:{}", e.getMessage());
        }
    }


    /**
     * 程序启动时，启动以下消费者
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        dayWorker.start();
        nightWorker.start();
    }
}
