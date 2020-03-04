
package factory;

import java.util.Vector;

import factory.Aircraft;

class Task {
    public String aircraftId;
    public int nParts;
}

public class Robot {
    private Vector<Task> works;
    private String partId; // set dans takePart, utilisé dans usePart
    private String status; // surement enum mais pas encore défini

    /** vérifier le status & l'Id + sleep + set partId + print message */
    public void takePart() {

    }

    /** le status + sleep + print message */
    public void goToPipeline(int pipelineId) {

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

    /** enleve la ligne (utilisé si bug) */
    public void resetWork(Aircraft aircraft) {

    }

    /** retourne le total du travail à faire (utilisé pour balanced le travail entre les robots) */
    public int workToDo() {
        return 0;
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