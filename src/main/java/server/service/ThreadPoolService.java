package server.service;

import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author denghaowen
 * @date 2022/8/11 16:30
 */
@Service
public class ThreadPoolService {
    private static final ExecutorService pool = Executors.newFixedThreadPool(5);
    private void submit(Thread t) {
        pool.submit(t);
    }

    public void execute(Runnable r) {
        pool.execute(r);
    }

    @PreDestroy
    public void shutDown() {
        pool.shutdown();
    }
}
