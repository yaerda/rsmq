package cn.fhou77.rsmq.message;

import cn.fhou77.rsmq.config.RedisPool;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class MessageProducer {

    private Jedis jedis = RedisPool.getJedis();

    public void sendMessage(String key, String content) {
        jedis.lpush(Message.genPendingKey(key), content);
    }

    public void sendMessage(String key, List<?> contentList) {
        jedis.lpush(Message.genPendingKey(key), contentList.stream().map(JSON::toJSONString).toArray(String[]::new));
    }

    public void sendImpotantMessage(String key, String content) {
        jedis.lpush(Message.genPendingKey(key), content);
    }
}
