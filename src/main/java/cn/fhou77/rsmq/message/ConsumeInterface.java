package cn.fhou77.rsmq.message;

public interface ConsumeInterface {
    void dowork(String content) throws Exception;
}
