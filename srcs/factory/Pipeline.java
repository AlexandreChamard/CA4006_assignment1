
package factory;

import java.util.concurrent.ExecutorService; 

import factory.Factory;
import factory.Robot;
import factory.Aircraft;
import factory.State;

public class Pipeline {
    private State state;
    private Factory factory;
    private Aircraft aircraft = null;

    public Pipeline(Factory factory) {
        this.factory = factory;
        state = State.PENDING;
        // factory.execute(() -> factory.execute(() -> System.out.println("coucou")));
    }

    public boolean working() {
        return aircraft != null;
    }

    public void buildAircraft(Aircraft aircraft) {
        state = State.WORKING;
        if (this.aircraft != null) {
            throw new InternalError("Pipeline: there is already an aircraft to build.");
        }
        this.aircraft = aircraft;
        // begin algo
    }

    public void close() {
        state = State.PENDING;
    }
}