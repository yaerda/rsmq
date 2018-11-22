package cn.fhou77.rsmq.message;

import org.apache.commons.lang3.StringUtils;

public class Message {

    public static final String PENDING_HEAD = "RMQ-PENDING-"; // 消息队列
    public static final String DOING_HEAD = "RMQ-DOING-"; //消费队列

    public interface DAY {
        String TURN_DAY = "TURN_DAY"; //通知白天
        String TURN_NINGT = "TURN_NIGHT"; //通知夜晚
    }

    public static String genPendingKey(String key) {
        return PENDING_HEAD + key;
    }

    public static String genDoingKey(String key) {
        return DOING_HEAD + key;

    }

    public static String turnDoingKeyToPendingKey(String doing_key) throws Exception {
        if (StringUtils.isBlank(doing_key) || !doing_key.startsWith(DOING_HEAD)) {
            throw new Exception("parameer [" + doing_key + "] is not start with doing_key");
        }
        return genPendingKey(doing_key.split(DOING_HEAD)[1]);
    }
}
