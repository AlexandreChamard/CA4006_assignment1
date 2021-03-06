
import factory.Factory;

class Main {

    public static void help() {
        System.out.println("");
        System.out.println("USAGE:");
        System.out.println("\tjava Main [-f n] [-t n] [-p n] [-r n]");
        System.out.println("");
        System.out.println("-f --frequence: set the tick frequence (in ms) (n > 0).");
        System.out.println("-t --threads  : set the number of threads (n > 0).");
        System.out.println("-p --pipelines: set the number of pipelines (n > 0).");
        System.out.println("-r --robots   : set the number of robots (n > 0).");
        System.out.println("");
        System.out.println("Read the input stream. Commands available:");
        System.out.println("stop        : stop to the factory (close the input stream).");
        System.out.println("buy n       : buy n aircraft parts (n > 0).");
        System.out.println("command Id n: command an aircraft to the factory (Id is a string) (n > 0).");
        System.out.println("sleep n     : sleep n ticks (n > 0) (useful for scripting).");
        System.out.println("frequence n : modify the ticks frequence (n > 0).");
        System.out.println("");
        System.exit(0);
    }

    public static void parseArgs(String[] args, int[] values) {
        int n = -1;
        for (String a : args) {
            if (n != -1) {
                try {
                    int m = Integer.parseInt(a);
                    if (m > 0) {
                        values[n] = m;
                    } else {
                        System.out.println("failt to parse "+a+". default value was taken.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("failt to parse "+a+". default value was taken.");
                }
                n = -1;
            } else {
                switch (a) {
                    case "-h": case "--help":
                        help();

                    case "-f": case "--frequence":
                        n = 0;
                        break;
                    case "-t": case "--threads":
                        n = 1;
                        break;
                    case "-p": case "--pipelines":
                        n = 2;
                        break;
                    case "-r": case "--robots":
                        n = 3;
                        break;
                    default:
                        System.out.println("Invalid argument "+a);
                }
            }
        }
        if (n != -1) {
            System.out.println("Missing number argument to "+args[args.length-1]+". Default value was taken.");
        }
    }

    public static void main(String[] args) {
        int[] values = {Factory.DEFAULT_TICK_FREQUENCE,
                        Factory.DEFAULT_NB_THREADS,
                        Factory.DEFAULT_NB_PIPELINES,
                        Factory.DEFAULT_NB_ROBOTS};

        parseArgs(args, values);
        System.out.println("Tick Frequence: "+values[0]);
        System.out.println("Nb Threads:     "+values[1]);
        System.out.println("Nb Pipelines:   "+values[2]);
        System.out.println("Nb Robots:      "+values[3]);
        Factory f = new Factory(values[0], values[1], values[2], values[3]);

        f.start();
    }
}