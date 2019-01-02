package odbdp.simulatedAnnealing;

public class SimulatedAnnealing {

    private int temperature;
    private int plateu;
    private double alpha;
    private double testProbability;
    private int[][] bestSolution;



    public SimulatedAnnealing(int nConf, int nQueries){
        this.temperature = 500;
        this.plateu = 1;
        this.alpha = 0.99;
        this.bestSolution = new int[nConf][nQueries];
        this.testProbability = Math.random() + 0.1;
    }

    // starting algorithm method
    public int[] start(){
        //todo implement
      return null;
    }

    private double calculateProbability(){
        return 0.0;
    }

    private int calculateObjectiveFunction(){
        return 0;
    }

    private boolean acceptance(){
        return false;
    }


}
