
package factory;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors; 

import factory.Pipeline;
import factory.Storage;
import factory.Aircraft;

public class Factory {
    public static final int TICK_FREQUENCE = 500; // 0.1 sec
    private static final int NB_ROBOTS = 10;
    private static final int NB_THREADS = 1;
    private static final int NB_PIPELINES = 1;

    private static ExecutorService  threadPool = Executors.newFixedThreadPool(NB_THREADS);
    private Queue<Aircraft>         aircrafts;
    private Pipeline[]              pipelines;
    private Storage                 storage;
    private int                     linesRead = 0;

    public Factory() {
        aircrafts = new ArrayDeque<Aircraft>();
        pipelines = new Pipeline[NB_PIPELINES];
        storage = new Storage(this);
    }

    public void start() {
        for (int i = 0; i < NB_PIPELINES; ++i) {
            pipelines[i] = new Pipeline(this);
        }

        BufferedReader obj = new BufferedReader(new InputStreamReader(System.in));  
        String str;
        try {
            do {
                if (System.console() != null)
                    System.out.print("> ");
                str = obj.readLine();
                parseLine(str);
            } while(str != null && !str.equals("stop"));
        } catch (IOException e) {}
        // try {Thread.sleep(1000); } catch (InterruptedException e) {}
        closeFactory();
    }

    public synchronized void execute(Runnable r) {
        threadPool.execute(r);
    }

    public synchronized void pipelineEnd(Pipeline pipeline, Aircraft aircraft) {
        // print aircraft
        if (aircrafts.peek() != null) {
            pipeline.buildAircraft(aircrafts.poll());
        } else {
            pipeline.close();
            notify();
        }
    }

    public Storage getStorage() {
        return storage;
    }

    private void parseLine(String line) {
        if (line == null) return;
        ++linesRead;

        if (line.startsWith("sleep ")) { // sleep N

            try {
                int n = Integer.parseInt(line.substring(6).trim());
                if (n > 0) {
                    System.out.println("start sleep");
                    try { Thread.sleep(n*TICK_FREQUENCE); } catch (InterruptedException e) {}
                    System.out.println("wake up");
                } else {
                    System.out.println("error parse ligne "+linesRead+": `sleep' command take a non-zero positive integer as argument.");
                }
            } catch (NumberFormatException e) {
                System.out.println("error parse ligne "+linesRead+": `sleep' command take a non-zero positive integer as argument.");
            }

        } else if (line.startsWith("command ")) { // command Name N

            try {
                line = line.substring(8).trim();
                String name = line.substring(0, line.indexOf(' '));
                int nParts = Integer.parseInt(line.substring(line.indexOf(' ')).trim());
                if (nParts > 0) {
                    newCommand(name, nParts);
                } else {
                    System.out.println("error parse ligne "+linesRead+": `command' command take a non-zero positive integer as argument.");
                }
            } catch (NumberFormatException e) {
                System.out.println("error parse ligne "+linesRead+": `command' command take a non-zero positive integer as argument.");
            }

        } else if (line.startsWith("buy ")) { // buy N

            try {
                int n = Integer.parseInt(line.substring(4).trim());
                if (n > 0) {
                    storage.addParts(n);
                } else {
                    System.out.println("error parse ligne "+linesRead+": `buy' command take a non-zero positive integer as argument.");
                }
            } catch (NumberFormatException e) {
                System.out.println("error parse ligne "+linesRead+": `buy' command take a non-zero positive integer as argument.");
            }

        }
    }

    private void closeFactory() {
        for (Pipeline p : pipelines) {
            while (p.working()) {
                try { wait(); } catch (InterruptedException e) {}
            }
        }
        threadPool.shutdown();
    }

    private synchronized void newCommand(String name, int nParts) {
        System.out.println("new command for '"+name+"' with "+nParts+" part"+(nParts > 1 ? "s":"")+".");
    }
}