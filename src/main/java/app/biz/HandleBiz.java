package app.biz;

import app.service.Consumer;
import org.springframework.stereotype.Service;

@Service
public class HandleBiz {
    //启动消费者
    public static boolean Service() {
        try {
            new Service1().start();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

class Service1 extends Thread {
    @Override
    public void run() {
        Consumer.service();
    }
}
