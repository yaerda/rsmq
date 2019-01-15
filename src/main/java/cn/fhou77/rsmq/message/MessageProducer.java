package cn.fhou77.rsmq.message;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

@Service
public class MessageProducer {

    @Autowired
    JedisPool jedisPool;


    public void sendMessage(String key, String content) {
        Jedis jedis = jedisPool.getResource();
        jedis.lpush(key, content);
        jedis.close();
    }

    public void sendMessage(String key, List<?> contentList) {
        Jedis jedis = jedisPool.getResource();
        jedis.lpush(key, contentList.stream().map(JSON::toJSONString).toArray(String[]::new));
        jedis.close();
    }

    public void sendImpotantMessage(String key, String content) {
        Jedis jedis = jedisPool.getResource();
        jedis.lpush(key, content);
        jedis.close();
    }
}
