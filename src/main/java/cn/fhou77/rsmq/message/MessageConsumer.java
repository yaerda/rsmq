package cn.fhou77.rsmq.message;

import cn.fhou77.rsmq.util.SpringUtil;
import com.alibaba.fastjson.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageConsumer implements ApplicationListener<ContextRefreshedEvent> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static List<MessageQueue> runWorkList = new ArrayList<>();

    @Autowired
    JedisPool jedisPool;

    @Autowired
    MessageProducer messageProducer;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        SpringUtil.setApplicationContext(contextRefreshedEvent.getApplicationContext());
        List<QueueProperty> queuePropertyList = JSONArray.parseArray("[{\"mainKey\":\"ORDER_FINISH\",\"subkey\":\"ZYD\",\"blockOnError\":true,\"workServName\":\"doWorkService\"}]").toJavaList(QueueProperty.class);

        //先初始化所有队列
        for (QueueProperty queueProperty : queuePropertyList) {
            new MessageQueue(queueProperty);
        }
        //先 重新将消费队列中的消息按进入队列的顺序重新 放入等待队列中
        reConsume();
        //再 启动消费队列线程
        for (MessageQueue messageQueue : MessageConsumer.runWorkList) {
            Thread thread = new Thread(messageQueue);
            thread.setName(messageQueue.getKey());
            thread.start();
        }
    }

    /**
     * 将 长时间积压(连接挂掉，消费缓慢等原因)在 doing_key 队列中的消息 重新释放到 pending_key 队列中消费
     *
     * @param queue
     */
    public void reloadDoingToPending(MessageQueue queue) {
        Jedis jedis = null;
        String doingKey = queue.getDoingKey();
        String pendingKey = queue.getPendingKey();
        Integer curIndex = null;
        try {
            jedis = jedisPool.getResource();
            Long messageSize = jedis.llen(queue.getDoingKey());
            logger.info("[{}]消费队列数量[{}]", queue.getKey(), messageSize);

            if (messageSize > 0) {
                for (int i = 0; i < messageSize; i++) {
                    String message = jedis.lrange(doingKey, i, i).get(0);
                    logger.info("[{}]重新消费消息:{}", pendingKey, message);
                    curIndex = i;
                    jedis.rpush(pendingKey, message);
                }
                jedis.ltrim(doingKey, messageSize, -1);
            }
        } catch (Exception e) {
            logger.error("{}重新发送消息错误:{}", queue.getDoingKey(), e.getMessage());
            if (curIndex == null) {
                logger.warn("重新发送消息时curIndex为null");
            } else {
                jedis.ltrim(doingKey, curIndex, -1);
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }


    /**
     * 重新将消费队列中的消息按进入队列的顺序重新消费
     */
    private void reConsume() {
        for (MessageQueue queue : runWorkList) {
            logger.info("重新消费 执行队列{}", queue.getKey());
            if (queue.isBlock()) {
                logger.info("队列{}已阻塞，不允许重新消费", queue.getKey());
            } else {
                reloadDoingToPending(queue);
            }
        }
    }

}
