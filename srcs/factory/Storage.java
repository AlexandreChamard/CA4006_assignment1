
package factory;

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
        System.out.println("Restock "+n+" ("+parts+")");
        clearQueue();
    }

    public void getPart(Robot robot) {
        factory.execute(() -> {
            // System.out.println("Thread "+Thread.currentThread().getId()+": "+robot+" arrives on storage.");
            synchronized (this) {
                if (parts == 0) {
                    System.out.println("Thread "+Thread.currentThread().getId()+": "+robot+" storage is empty. Wait for new pieces");
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