package cn.fhou77.rsmq.message;

import org.apache.commons.lang3.StringUtils;

public class QueueProperty {

    /**
     * 消息会先进入等待队列，等待消费;
     * 开始消费后，该消息会在消费队列留存;
     * 正常消费完成后，缓存在 消费队列中的消息被清除
     * 错误发生后，启用了错误阻塞的队列会阻塞 等待队列
     * 错误消息留存在消费队列中
     */
    private static final String PENDING_HEAD = "RMQ-PENDING-"; // 等待队列
    private static final String DOING_HEAD = "RMQ-DOING-"; // 消费队列

    private String name;

    /**
     * 主键+子键共同组成该队列的唯一标识
     */
    private String mainKey;
    /**
     * 子键 可以为空
     */
    private String subkey;
    /**e
     * 是否启用错误阻塞。
     * 启用后，如果有错误消息，将会阻塞等待队列
     */
    private Boolean blockOnError;

    /**
     * 消费者，需实现 ConsumeInterface 接口
     */
    private String workServName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMainKey() {
        return mainKey;
    }

    public void setMainKey(String mainKey) {
        this.mainKey = mainKey;
    }

    public String getSubkey() {
        return subkey;
    }

    public void setSubkey(String subkey) {
        this.subkey = subkey;
    }

    public String getWorkServName() {
        return workServName;
    }

    public void setWorkServName(String workServName) {
        this.workServName = workServName;
    }

    public Boolean getBlockOnError() {
        return blockOnError;
    }

    public void setBlockOnError(Boolean blockOnError) {
        this.blockOnError = blockOnError;
    }

    public String getKey() {
        if (StringUtils.isBlank(this.subkey)) {
            return this.mainKey;
        } else {
            return this.mainKey.trim() + "-" + this.subkey.trim();
        }
    }


    public String getPendingKey() {
        return PENDING_HEAD + this.getKey();
    }

    public String getDoingKey() {
        return DOING_HEAD + this.getKey();

    }
}
