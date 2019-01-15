package cn.fhou77.rsmq;

import cn.fhou77.rsmq.message.MessageConsumer;
import cn.fhou77.rsmq.message.MessageQueue;
import cn.fhou77.rsmq.message.QueueProperty;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Objects;

@RestController
public class MessController {

    @Autowired
    MessageConsumer messageConsumer;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping("/active")
    public String block(@RequestParam("key") String key) {
        MessageQueue messageQueue = getRunWorker(key);
        Thread existThread = getThread(key);
        if (messageQueue != null && existThread != null) {
            logger.info("线程正在工作：{}", existThread.toString());
            messageConsumer.reloadDoingToPending(messageQueue);
            return JSON.toJSONString(existThread.toString()) + "\n" + JSON.toJSONString(messageQueue);
        } else if (existThread == null) {
            if (messageQueue == null) {
                logger.info("处理线程状态:{}", existThread.toString());
                logger.info("work实体不存在");
                //create runWork
                QueueProperty queueProperty = JSON.parseObject("[{\"mainKey\":\"ORDER_FINISH\",\"subkey\":\"YUAN_LIN\",\"blockOnError\":false,\"workServName\":\"doWorkService\"}]").toJavaObject(QueueProperty.class);
                messageQueue = new MessageQueue(queueProperty);
            }
            //create thread
            logger.info("处理线程{}不存在", key);
            //reconsume doing_queue before active
            messageConsumer.reloadDoingToPending(messageQueue);
            //create thread
            //init block = false
            messageQueue.setBlock(false);
            Thread thread = new Thread(messageQueue);
            logger.info("启动队列消费线程：{}", messageQueue.getKey());
            thread.setName(messageQueue.getKey());
            thread.start();
            existThread = getThread(key);
            return JSON.toJSONString(existThread.toString()) + "\n" + JSON.toJSONString(messageQueue);
        }
        return "null";
    }

    @RequestMapping("/find")
    public String start(@RequestParam("key") String name) {
        Thread thread = getThread(name);
        return thread == null ? "null" : thread.toString();
    }


    private Thread getThread(String name) {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup parentThreadGroup;
        while ((parentThreadGroup = threadGroup.getParent()) != null) {
            threadGroup = parentThreadGroup;
        }
        // List all active Threads
        final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        int nAllocated = threadMXBean.getThreadCount();
        int n = 0;
        Thread[] threads;
        do {
            nAllocated *= 2;
            threads = new Thread[nAllocated];
            n = threadGroup.enumerate(threads, true);
        } while (n == nAllocated);
        threads = Arrays.copyOf(threads, n);
        // Get Thread by name
        for (Thread thread : threads) {
            if (name.equals(thread.getName())) {
                logger.info("find the thread :{}", thread.toString());
                return thread;
            }
        }
        return null;
    }

    private MessageQueue getRunWorker(String key) {
        for (MessageQueue runWork : MessageConsumer.runWorkList) {
            if (Objects.equals(runWork.getKey(), key)) {
                return runWork;
            }
        }
        return null;
    }
}
