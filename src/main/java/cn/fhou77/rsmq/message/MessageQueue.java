package cn.fhou77.rsmq.message;

import cn.fhou77.rsmq.util.SpringUtil;
import io.lettuce.core.RedisCommandTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.QueryTimeoutException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class MessageQueue implements Runnable {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String pendingKey;

    private String doingKey;

    private String key;

    private ConsumeInterface worker;

    private boolean block;

    private boolean blockOnError;

    public String getKey() {
        return key;
    }

    public String getPendingKey() {
        return pendingKey;
    }

    public String getDoingKey() {
        return doingKey;
    }

    public boolean isBlock() {
        return block;
    }

    public void setBlock(boolean block) {
        this.block = block;
    }

    public MessageQueue(QueueProperty queueProper) {
        this.key = queueProper.getKey();
        this.blockOnError = queueProper.getBlockOnError();
        this.block = false;
        this.pendingKey = queueProper.getPendingKey();
        this.doingKey = queueProper.getDoingKey();
        String workServName = queueProper.getWorkServName();
        this.worker = (ConsumeInterface) SpringUtil.getBean(workServName);
        MessageConsumer.runWorkList.add(this);
    }

    @Override
    public void run() {
        logger.info("启动Redis Queue [{}]：", this.key);
        JedisPool jedisPool = (JedisPool) SpringUtil.getBean("getJedisPool");
        while (!block) {
            Jedis jedis = jedisPool.getResource();
            try {
                String content = jedis.brpoplpush(pendingKey, doingKey, 0);
                logger.info("Message-Queue:[{}]  new Message: {}", key, content);
                doWork(content);
                logger.info("consume finished");
                jedis.lrem(doingKey, 0, content);
            } catch (RedisCommandTimeoutException | QueryTimeoutException e) {
                logger.error("redis connect timeout：{}", e.getMessage());
                throw e;
            } catch (JedisConnectionException e) {
                if (Thread.interrupted()) {
                    logger.error("Thread was interrupted");
                } else {
                    logger.error("There was a connection problem to redis, need to do something else.");
                }
                throw e;
            } catch (Exception e) {
                logger.error("RMQ consume error：{}", e.getMessage());
                block = blockOnError;
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
    }

    /**
     * 处理任务
     *
     * @param content
     * @throws Exception
     */
    protected void doWork(String content) throws Exception {
        worker.dowork(content);
    }

}
