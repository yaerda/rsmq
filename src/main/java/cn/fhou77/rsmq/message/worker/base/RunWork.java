package cn.fhou77.rsmq.message.worker.base;

import cn.fhou77.rsmq.config.RedisPool;
import cn.fhou77.rsmq.message.Message;
import cn.fhou77.rsmq.message.MessageConsumer;
import cn.fhou77.rsmq.message.MessageProducer;
import io.lettuce.core.RedisCommandTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.QueryTimeoutException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public abstract class RunWork {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String PENDING_KEY;

    private String DOING_KEY;

    private String key;

    private WorkerImpl worker;

    @Autowired
    MessageProducer messageProducer;

    public RunWork() {
    }

    public RunWork(WorkerImpl worker, String key) {
        this.key = key;
        this.PENDING_KEY = Message.genPendingKey(key);
        this.DOING_KEY = Message.genDoingKey(key);
        this.worker = worker;
        MessageConsumer.DOING_LIST.add(DOING_KEY);
    }


    /**
     * 启动任务
     */
    public void start() {
        logger.info("启动Redis Queue [{}]：", this.key);
        new Thread("cosumber [" + key + "] thread") {
            @Override
            public void run() {
                while (true) {
                    Jedis jedis = RedisPool.getJedis();
                    try {
                        String content = jedis.brpoplpush(PENDING_KEY, DOING_KEY, 0);
                        logger.info("key:[{}]  new Message: {}", key, content);
                        logger.info("开始消费");
                        doWork(worker, content);
                        logger.info("消费完毕，删除消息·");
                        jedis.lrem(DOING_KEY, 0, content);
                    } catch (RedisCommandTimeoutException | QueryTimeoutException e) {
                        logger.info("redis 连接超时：{}", e.getMessage());
                    } catch (JedisConnectionException e) {
                        if (Thread.interrupted()) {
                            logger.info("Thread was interrupted");
                        } else {
                            logger.info("There was a connection problem to redis, need to do something else.");
                        }
                    } catch (Exception e) {
                        logger.info("其他错误：{}", e.getMessage());
                    } finally {
                        logger.info("finally close redis connect");
                        if (jedis != null) {
                            jedis.close();
                        }
                    }
                }
            }
        }.start();
    }

    /**
     * 处理任务
     *
     * @param content
     * @throws Exception
     */
    protected void doWork(WorkerImpl worker, String content) throws Exception {
        worker.gogogo(content);
    }

}
