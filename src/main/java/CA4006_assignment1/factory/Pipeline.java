
package factory;

import CA4006_assignment1.App;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Vector;

import factory.Factory;
import factory.Robot;
import factory.Aircraft;

public class Pipeline {
    private static int      idIt = 0;
    private int             id;
    private Factory         factory;
    private Queue<Robot>    queue;
    private Vector<Robot>   robots;
    private Aircraft        aircraft = null;

    public Pipeline(Factory factory) {
        this.id = ++idIt;
        this.factory = factory;
        robots = new Vector<Robot>();
        queue = new ArrayDeque<Robot>();
        System.out.println(this+" has been created.");
    }

    public synchronized boolean working() {
        return aircraft != null || robots.isEmpty() == false;
    }

    public synchronized void buildAircraft(Aircraft aircraft) {
        if (this.aircraft != null) {
            throw new InternalError(this+": there is already an aircraft to build.");
        }
        if (factory.redirected()) App.client.send("[3,\""+aircraft+"\",\""+this+"\"]");
        System.out.println("Thread "+Thread.currentThread().getId()+": "+this+" starts to build "+aircraft);
        this.aircraft = aircraft;
        factory.givesRobots(this, aircraft.missingWork());
    }

    // async
    public void getRobots(Vector<Robot> v) {
        factory.execute(() -> {
            synchronized (this) {
                if (v == null) { // to not block
                    System.out.println("Thread "+Thread.currentThread().getId()+": "+this+" abort the construction of "+aircraft+". We apologise for this.");
                    aircraft = null;
                    return;
                }
                robots.addAll(v);
                System.out.println("Thread "+Thread.currentThread().getId()+": "+this+" has get "+v.size()+" new robots.");

                //!!\ algo ne marche que si pas déjà de travail assigné
                int n = aircraft.missingWork() / v.size();
                for (Robot r : v) {
                    r.startWork(n);
                }
                if (aircraft.missingWork() - n * v.size() != 0) {
                    v.firstElement().manageWork(aircraft.missingWork() - n * v.size());
                }
            }
        });
    }

    // async
    public void advanceAircraft(Robot r) {
        factory.execute(() -> {
            synchronized (this) {
                if (r != queue.peek()) {
                    throw new InternalError(this+": advance command was sent from the corrupted "+r+".");
                }
                queue.remove();
                if (aircraft.isBuilt() == false) {
                    if (queue.isEmpty() == false) {
                        queue.peek().usePart(aircraft);
                    }
                    if (factory.redirected()) App.client.send("[7,\""+this+"\",1]");
                    System.out.println("Thread "+Thread.currentThread().getId()+": "+this+" advance the aircraft ("+aircraft.missingWork()+" parts left).");

                    try { Thread.sleep(1 * factory.getFrequence()); } catch (InterruptedException e) {}

                } else {
                    endPipeline();
                }
            }
        });
    }

    // async
    public void placeRobot(Robot r) {
        factory.execute(() -> {
            synchronized (this) {
                if (robots.contains(r) == false) {
                    throw new InternalError(this+": place command was sent from the corrupted "+r+".");
                }
                if (queue.contains(r) == true) {
                    throw new InternalError(this+": the robot "+r+" is already in the queue.");
                }
            }

            if (factory.redirected()) App.client.send("[6,\""+r+"\",3]");
            System.out.println("Thread "+Thread.currentThread().getId()+": "+r+" goes to "+this+".");
            try { Thread.sleep(4 * factory.getFrequence()); } catch (InterruptedException e) {}

            synchronized (this) {
                queue.add(r);
                if (queue.size() == 1) {
                    r.usePart(aircraft);
                }
            }
        });
    }

    // async
    public void endOfWork(Robot r) {
        factory.execute(() -> {
            synchronized (this) {
                if (robots.remove(r) == false) {
                    throw new InternalError(this+": endOfWork command was sent from the corrupted "+r+".");
                }
                notify();
                System.out.println("Thread "+Thread.currentThread().getId()+": "+r+" has left the "+this);
                if (robots.isEmpty() == true && aircraft.isBuilt() == false) {
                    factory.givesRobots(this, aircraft.missingWork());
                }
            }
        });
    }

    // async
    private void endPipeline() {
        factory.execute(() -> {
            synchronized (this) {
                if (factory.redirected()) App.client.send("[7,\""+this+"\",2]");
                System.out.println("Thread "+Thread.currentThread().getId()+": "+this+" has successfully build "+aircraft);
                for (Robot robot : robots) {
                    if (robot.workToDo() > 0) {
                        System.out.println("Thread "+Thread.currentThread().getId()+": "+this+" il reste "+robot+" dans la pipeline.");
                        robot.manageWork(-robot.workToDo());
                    }
                }
                while (robots.isEmpty() == false) {
                    try { wait(); } catch (InterruptedException e) {} //!!\ bloquant
                }
                Aircraft tmp = aircraft;
                aircraft = null;
                factory.pipelineEnd(this, tmp);
            }
        });
    }

    public String toString() {
        return "Pipeline "+id;
    }
}
