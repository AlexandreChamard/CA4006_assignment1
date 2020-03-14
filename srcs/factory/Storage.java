
package factory;

import java.lang.InternalError;

import factory.Factory;

public class Storage {
    Factory factory;
    int parts = 0;

    public Storage(Factory factory) {
        this.factory = factory;
    }

    public synchronized void addParts(int n) {
        if (n <= 0) {
            throw new InternalError("Storage: addParts must get an positive integer as argument.");
        }
        parts += n;
        System.out.println("Restock "+n+" ("+parts+")");
        notify();
    }

    public synchronized void getPart(Robot robot) {
        while (parts == 0) {
            try { wait(); } catch (InterruptedException e) {}
        }
        --parts;
    }
}