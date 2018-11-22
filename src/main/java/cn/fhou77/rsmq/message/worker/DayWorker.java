package cn.fhou77.rsmq.message.worker;

import cn.fhou77.rsmq.message.Message;
import cn.fhou77.rsmq.message.worker.base.RunWork;
import cn.fhou77.rsmq.message.worker.base.WorkerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class DayWorker extends RunWork {

    @Autowired
    public DayWorker(@Qualifier("morningService") WorkerImpl worker) {
        super(worker, Message.DAY.TURN_DAY);
    }
}
