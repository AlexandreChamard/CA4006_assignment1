
package factory;

import java.util.concurrent.ExecutorService; 

import factory.Aircraft;
import factory.Robot;
import factory.Factory;

public class Pipeline {
    public Pipeline() {
        Factory.execute(() -> Factory.execute(() -> System.out.println("coucou")));
    }

}