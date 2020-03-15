
package factory;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors; 

import factory.Pipeline;
import factory.Robot;
import factory.Storage;
import factory.Aircraft;

public class Factory {
    public static final int  TICK_FREQUENCE = 10; // 0.1 sec
    private static final int NB_ROBOTS = 10;
    private static final int NB_PIPELINES = 1;
    private static final int NB_THREADS = NB_PIPELINES + 1; // NB_PIPELINES + 1 (because wait block)

    private static ExecutorService  threadPool = Executors.newFixedThreadPool(NB_THREADS);
    private Queue<Aircraft>         aircrafts;
    private Pipeline[]              pipelines;
    private Robot[]                 robots;
    private Storage                 storage;
    private int                     linesRead = 0;

    public Factory() {
        aircrafts = new ArrayDeque<Aircraft>();
        pipelines = new Pipeline[NB_PIPELINES];
        robots = new Robot[NB_ROBOTS];
        storage = new Storage(this);
    }

    public void start() {
        for (int i = 0; i < NB_PIPELINES; ++i) {
            pipelines[i] = new Pipeline(this);
        }
        for (int i = 0; i < NB_ROBOTS; ++i) {
            robots[i] = new Robot(this);
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
        closeFactory();
    }

    public synchronized void execute(Runnable r) {
        threadPool.execute(r);
    }

    // async
    public void givesRobots(Pipeline p, int n) {
        execute(() -> {
            synchronized (this) {
                Vector<Robot> v = new Vector<Robot>();
                for (Robot r : robots) {
                    if (r.inPipeline() == false) {
                        v.add(r);
                        if (v.size() == n) {
                            break;
                        }
                    }
                }
                if (v.isEmpty() == false) {
                    p.getRobots(v);
                } else {
                    // pas de chance
                    p.getRobots(null); // to not block
                }
            }
        });
    }

    public synchronized void pipelineEnd(Pipeline pipeline, Aircraft aircraft) {
        System.out.println(aircraft+" has exist the factory to fly away.");
        if (aircrafts.peek() != null) {
            pipeline.buildAircraft(aircrafts.poll());
        } else {
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
                    System.out.println("error parse ligne "+linesRead+": `sleep' command takes a non-zero positive integer as argument.");
                }
            } catch (NumberFormatException e) {
                System.out.println("error parse ligne "+linesRead+": `sleep' command takes a non-zero positive integer as argument.");
            }

        } else if (line.startsWith("command ")) { // command Name N

            try {
                line = line.substring(8).trim();
                if (line.indexOf(' ') == -1) {
                    System.out.println("error parse ligne "+linesRead+": `command' command takes an id and a non-zero positive integer as argument.");
                    return;
                }
                String name = line.substring(0, line.indexOf(' '));
                int nParts = Integer.parseInt(line.substring(line.indexOf(' ')).trim());
                if (nParts > 0) {
                    newCommand(name, nParts);
                } else {
                    System.out.println("error parse ligne "+linesRead+": `command' command takes an id and a non-zero positive integer as argument.");
                }
            } catch (NumberFormatException e) {
                System.out.println("error parse ligne "+linesRead+": `command' command takes an id and a non-zero positive integer as argument.");
            }

        } else if (line.startsWith("buy ")) { // buy N

            try {
                int n = Integer.parseInt(line.substring(4).trim());
                if (n > 0) {
                    storage.addParts(n);
                } else {
                    System.out.println("error parse ligne "+linesRead+": `buy' command takes a non-zero positive integer as argument.");
                }
            } catch (NumberFormatException e) {
                System.out.println("error parse ligne "+linesRead+": `buy' command takes a non-zero positive integer as argument.");
            }

        }
    }

    private synchronized void closeFactory() {
        System.out.println("Start closing the factory.");
        for (Pipeline p : pipelines) {
            while (p.working()) {
                try { wait(); } catch (InterruptedException e) {}
            }
            System.out.println(p+" has been closed.");
        }
        for (Robot r : robots) {
            while (r.inPipeline()) {
                try { wait(); } catch (InterruptedException e) {}
            }

        }
        System.out.println("Factory has been closed.");
        threadPool.shutdown();
    }

    private synchronized void newCommand(String name, int nParts) {
        Aircraft aircraft = new Aircraft(name, nParts);
        System.out.println("new command for '"+name+"' with "+nParts+" part"+(nParts > 1 ? "s":"")+".");
        for (Pipeline p : pipelines) {
            if (p.working() == false) {
                p.buildAircraft(aircraft);
                return;
            }
        }
        aircrafts.add(aircraft);
    }
}