
package factory;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors; 

import factory.Pipeline;

public class Factory {
    private static final int NB_ROBOTS = 10;
    private static final int NB_THREADS = 1;
    private static final int NB_PIPELINES = 1;

    private static ExecutorService threadPool = Executors.newFixedThreadPool(NB_THREADS);
    private Queue<String>   airplane;
    private Pipeline[]      pipelines;

    public Factory() {
        airplane = new ArrayDeque<String>();
        pipelines = new Pipeline[NB_PIPELINES];
    }

    public void start() {
        for (int i = 0; i < NB_PIPELINES; ++i)
            pipelines[i] = new Pipeline();

        /**
        gérer les entrés des avions et des travaux des robots
         */
        try {Thread.sleep(1000); } catch (InterruptedException e) {}
        threadPool.shutdown();
    }

    public static synchronized void execute(Runnable r) {
        threadPool.execute(r);
    }
}