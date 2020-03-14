
package factory;

import java.lang.InternalError;

public class Aircraft {
    private String id;
    private int size;
    private int nbPart;

    public Aircraft(String id, int size) {
        this.id = id;
        this.size = size;
    }

    public synchronized void addPart() {
        if (size == nbPart) {
            throw new InternalError("aircraft "+id+" is already build.");
        }
        ++nbPart;
    }

    public synchronized boolean isBuilt() {
        return size == nbPart;
    }

    public synchronized int missingWork() {
        return size - nbPart;
    }

    public String getId() {
        return id;
    }

    /** stringification */
    public String toString() {
        return "TODO";
    }
}