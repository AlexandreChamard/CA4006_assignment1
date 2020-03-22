
package factory;

import CA4006_assignment1.App;

import java.lang.InternalError;
import java.util.ArrayDeque;
import java.util.Queue;

import factory.Factory;
import factory.Robot;

public class Storage {
    Factory factory;
    Queue<Robot> waitingList;
    int parts = 0;

    public Storage(Factory factory) {
        this.factory = factory;
        waitingList = new ArrayDeque<Robot>();
    }

    public synchronized void addParts(int n) {
        if (n <= 0) {
            throw new InternalError("Storage: addParts must get an positive integer as argument.");
        }
        parts += n;
        if (factory.redirected()) App.client.send("[1,"+n+","+parts+"]");
        System.out.println("Restock "+n+" ("+parts+")");
        clearQueue();
    }

    public void getPart(Robot robot) {
        factory.execute(() -> {
            synchronized (this) {
                if (parts == 0) {
                    System.out.println("Thread "+Thread.currentThread().getId()+": "+robot+" storage is empty. Wait for new pieces...");
                    if (factory.running() == false) {
                        if (factory.redirected()) App.client.send("[8]");
                        System.out.println("Thread "+Thread.currentThread().getId()+": Due to covid-19, no new piece will arrive. The factory is forced to close.");
                        App.close();
                        System.exit(42);
                    }
                    waitingList.add(robot);
                } else {
                    --parts;
                    robot.hasPart();
                }
            }
        });
    }

    private synchronized void clearQueue() {
        while (waitingList.peek() != null) {
            getPart(waitingList.poll());
        }
    }
}
