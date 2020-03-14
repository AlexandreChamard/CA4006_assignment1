
package factory;

import java.lang.InternalError;

import factory.Factory;
import factory.Pipeline;
import factory.Aircraft;
import factory.State;

public class Robot {
    private Factory factory;
    private Pipeline pipeline;
    private String aircraftId;
    private int nParts;
    private State state;

    public Robot(Factory factory) {
        this.factory = factory;
        this.state = State.PENDING;
    }

    /** le status + sleep + print message */
    public void goToPipeline(Pipeline pipeline) {
        if (this.pipeline != null) {
            throw new InternalError("Robot: already working in a pipeline.");
        }
        this.pipeline = pipeline;
    }

    /** vérifier le status & l'Id + sleep + set partId + print message */
    public void takePart() {
        factory.getStorage().getPart(this);
    }

    /** vérifier le status + vérifié l'Id + sleep + set addPart sur l'aircraft + decrementer le works + print message */
    public void usePart(Aircraft aircraft) {

    }

    /** vérifié si aircraft.id est dans works */
    public boolean canBuild(Aircraft aircraft) {
        return false;
    }

    /** ajoute du travail au robot */
    public void addWork(Aircraft aircraft, int n) {

    }

    /** retourne le total du travail à faire (utilisé pour balanced le travail entre les robots) */
    public int workToDo() {
        return nParts;
    }

    /** manage la variable works:
      * si aircraft.id est dans la liste:
      *   si n > 0: works[aircraft.id] += n
      *   si n < 0: works[aircraft.id] -= n
      * sinon:
      *   si n > 0: works[aircraft.id] = n
      * si works[aircraft.id] <= 0:
      *   enlever la ligne
      */
    private synchronized void managePart(Aircraft aircraft, int n) {

    }
}