
package factory;

public class Aircraft {
    private String id;
    private int size;
    private int nbPart;

    public Aircraft(String id, int size) {
        this.id = id;
        this.size = size;
    }

    /** pas besoin d'être syncronized car il est construit pièce par pièce dans la pipeline */
    public void addPart() {
        /** vérifier le nombre */
        ++nbPart;
    }

    public boolean Built() {
        return size == nbPart;
    }

    public String getId() {
        return id;
    }

    /** stringification */
    public String toString() {
        return "TODO";
    }
}