
package factory;

import CA4006_assignment1.App;

import java.lang.InternalError;
import java.util.Random;

import factory.Factory;
import factory.Pipeline;
import factory.Aircraft;

enum State {
    PENDING,
    ACTIVE,
    WORKING,
    STORAGE,
}

public class Robot {
    private static Random r = new Random();
    private static int idIt = 0;
    private int id;
    private Factory factory;
    private Pipeline pipeline;
    private int nParts;
    private State state;
    private boolean holding;

    public Robot(Factory factory) {
        this.id = ++idIt;
        this.factory = factory;
        this.state = State.PENDING;
        this.holding = false;
        System.out.println(this+" has been created.");
    }

    /** le status + sleep + print message */
    public synchronized void goToPipeline(Pipeline pipeline) {
        if (state != State.PENDING) {
            throw new InternalError(this+": already working in a pipeline.");
        }
        this.pipeline = pipeline;
        state = State.ACTIVE;
        // print work on pipeline
    }

    public synchronized boolean working() {
        return state == State.WORKING;
    }

    public synchronized boolean inPipeline() {
        return state != State.PENDING;
    }

    public synchronized void startWork(int n) {
        if (state == State.PENDING) {
            throw new InternalError(this+" is not assigned to any pipeline.");
        }
        takePart();
        manageWork(n);
    }

    // async
    public void takePart() {
        changeState(State.ACTIVE, State.STORAGE, "takePart");
        factory.execute(() -> {
            if (state != State.STORAGE) {
                throw new InternalError(this+": Invalid state on takePart.");
            }
            if (factory.redirected()) App.client.send("[6,\""+this+"\",1]");
            System.out.println("Thread "+Thread.currentThread().getId()+": "+this+" goes to storage.");
            try { Thread.sleep(2 * factory.getFrequence()); } catch (InterruptedException e) {}
            factory.getStorage().getPart(this);
        });
    }

    public void hasPart() {
        factory.execute(() -> {
            synchronized (this) {
                if (nParts != 0) {
                    holding = true;
                    changeState(State.STORAGE, State.ACTIVE, "hasPart");
                    if (factory.redirected()) App.client.send("[6,\""+this+"\",2]");
                    System.out.println("Thread "+Thread.currentThread().getId()+": "+this+" takes its part on storage.");
                    pipeline.placeRobot(this);
                } else {
                    resetState();
                }
            }
        });
    }

    // async
    public void usePart(Aircraft aircraft) {
        factory.execute(() -> {
            synchronized (this) {
                changeState(State.ACTIVE, State.WORKING, "start working");
            }
            System.out.println("Thread "+Thread.currentThread().getId()+": "+this+" works on "+pipeline+".");
            int n = r.nextInt(9)+2; // [2-10]
            try { Thread.sleep(n * factory.getFrequence()); } catch (InterruptedException e) {}
            aircraft.addPart();
            synchronized (this) {
                holding = false;
                --nParts;
                changeState(State.WORKING, State.ACTIVE, "end working");
                pipeline.advanceAircraft(this);
                if (nParts != 0) {
                    takePart();
                } else {
                    resetState();
                }
            }
        });
    }

    /** ajoute du travail au robot */
    public synchronized void manageWork(int n) {
        if (n == 0) {
            throw new InternalError(this+" cannot do no job when invoked.");
        }
        nParts = Math.max(nParts + n, 0);
        if (holding == true) {
            nParts = 1;
        }
        if (factory.redirected()) App.client.send("[5,\""+this+"\","+nParts+"]");
        System.out.println("Thread "+Thread.currentThread().getId()+": "+this+" has now "+nParts+" parts to place on "+pipeline+".");
    }

    /** retourne le total du travail à faire (utilisé pour balanced le travail entre les robots) */
    public synchronized int workToDo() {
        return nParts;
    }

    private synchronized void resetState() {
        if (factory.redirected()) App.client.send("[6,\""+this+"\",4]");
        System.out.println("Thread "+Thread.currentThread().getId()+": "+this+" has end its work.");
        if (holding == true) {
            factory.getStorage().addParts(1);
        }
        state = State.PENDING;
        pipeline.endOfWork(this);
        pipeline = null;
    }

    private synchronized void changeState(State from, State to, String msg) {
        if (state != from) {
            throw new InternalError(this+": Invalid state on "+msg+": should be "+from+" but was "+to+".");
        }
        state = to;
    }

    public String toString() {
        return "Robot "+id;
    }
}
