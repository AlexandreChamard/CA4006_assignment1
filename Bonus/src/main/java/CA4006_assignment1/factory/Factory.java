
package factory;

import CA4006_assignment1.App;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.*;

import factory.Pipeline;
import factory.Robot;
import factory.Storage;
import factory.Aircraft;

public class Factory {
    public static final int DEFAULT_TICK_FREQUENCE = 100; // 0.1 sec
    public static final int DEFAULT_NB_THREADS = 2;
    public static final int DEFAULT_NB_PIPELINES = 1;
    public static final int DEFAULT_NB_ROBOTS = 10;

    private static int              tickFrequence;
    private static ExecutorService  threadPool;

    private boolean                 _running;
    private boolean                 _redirected;
    private Lock                    lock = new ReentrantLock(); // use to lock when robots are sent to pipelines
    private Queue<Aircraft>         aircrafts;
    private Pipeline[]              pipelines;
    private Robot[]                 robots;
    private Storage                 storage;
    private int                     linesRead = 0;

    public Factory(int tickFrequence, int nbThreads, int nbPipelines, int nbRobots, boolean _redirected) {
        if (tickFrequence <= 0 || nbThreads <= 0 || nbPipelines <= 0 || nbRobots <= 0) {
            throw new InternalError("Factory creation: invalid arguments.");
        }
        this._redirected = _redirected;
        setFrequence(tickFrequence);
        if (threadPool == null) {
            threadPool = Executors.newFixedThreadPool(nbThreads);
        }
        aircrafts = new ArrayDeque<Aircraft>();
        pipelines = new Pipeline[nbPipelines];
        robots = new Robot[nbRobots];
        storage = new Storage(this);
    }

    public synchronized void setFrequence(int tickFrequence) {
        this.tickFrequence = tickFrequence;
    }

    public synchronized int getFrequence() {
        return tickFrequence;
    }

    public synchronized boolean running() {
        return _running;
    }

    public synchronized boolean redirected() {
        return _redirected;
    }

    public void start() {
        _running = true;
        for (int i = 0; i < pipelines.length; ++i) {
            pipelines[i] = new Pipeline(this);
        }
        for (int i = 0; i < robots.length; ++i) {
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

    public void execute(Runnable r) {
        threadPool.execute(r);
    }

    // async
    public void givesRobots(Pipeline p, int n) {
        execute(() -> {
            int max_robots = Math.min(n, Math.max(robots.length / pipelines.length, 3));
            Vector<Robot> v = new Vector<Robot>();
            lock.lock();
            for (Robot r : robots) {
                if (r.inPipeline() == false) {
                    if (_redirected) App.client.send("[4,\""+p+"\",\""+r+"\"]");
                    System.out.println("add "+r+" to the "+p);
                    v.add(r);
                    r.goToPipeline(p);
                    if (v.size() == max_robots) {
                        break;
                    }
                }
            }
            lock.unlock();
            if (v.isEmpty() == false) {
                p.getRobots(v);
            } else {
                // unlucky
                p.getRobots(null); // to not block
            }
        });
    }

    public void pipelineEnd(Pipeline pipeline, Aircraft aircraft) {
        execute(() -> {
            synchronized (this) {
                System.out.println(aircraft+" has left the factory to fly away.");
                if (aircrafts.peek() != null) {
                    pipeline.buildAircraft(aircrafts.poll());
                } else {
                    if (_running == false) {
                        closeFactory();
                    }
                }
            }
        });
    }

    public Storage getStorage() {
        return storage;
    }

    private void parseLine(String line) {
        if (line == null) return;
        ++linesRead;

        line = line.trim();
        if (line.startsWith("sleep ")) { // sleep N

            try {
                int n = Integer.parseInt(line.substring(6).trim());
                if (n > 0) {
                    System.out.println("start sleep");
                    try { Thread.sleep(n*getFrequence()); } catch (InterruptedException e) {}
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

        } else if (line.startsWith("frequence ")) {

            try {
                int n = Integer.parseInt(line.substring(10).trim());
                if (n > 0) {
                    setFrequence(n);
                } else {
                    System.out.println("error parse ligne "+linesRead+": `frequence' command takes a non-zero positive integer as argument.");
                }
            } catch (NumberFormatException e) {
                System.out.println("error parse ligne "+linesRead+": `frequence' command takes a non-zero positive integer as argument.");
            }

        }
    }

    private synchronized void closeFactory() {
        if (_running == true)
            System.out.println("Start closing the factory.");
        _running = false;
        if (aircrafts.isEmpty() == false)
            return;
        for (Pipeline p : pipelines) {
            if (p.working() == true) {
                return;
            }
            if (_redirected) App.client.send("[7,\""+p+"\",3]");
            System.out.println(p+" has been closed.");
        }
        if (_redirected) App.client.send("[8]");
        System.out.println("Factory has been closed.");

        threadPool.shutdown();

        App.close();
        System.exit(0);
    }

    private void newCommand(String name, int nParts) {
        Aircraft aircraft = new Aircraft(name, nParts);
        if (_redirected) App.client.send("[2,\""+aircraft+"\","+nParts+"]");
        System.out.println("new command for '"+name+"' with "+nParts+" part"+(nParts > 1 ? "s":"")+".");

        // find if at least one robot is available
        lock.lock();
        for (Robot r : robots) {
            if (r.inPipeline() == false) {
                // find an available pipeline
                for (Pipeline p : pipelines) {
                    if (p.working() == false) {
                        p.buildAircraft(aircraft);
                        lock.unlock();
                        return;
                    }
                }
                break;
            }
        }
        lock.unlock();
        // if neither a robot nor a pipeline is available, put the command in the waiting list
        synchronized (this) {
            aircrafts.add(aircraft);
        }
    }
}
