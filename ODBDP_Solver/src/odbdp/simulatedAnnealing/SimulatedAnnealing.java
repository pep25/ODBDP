package odbdp.simulatedAnnealing;

import java.util.ArrayList;
import java.util.Random;

public class SimulatedAnnealing {

    private int temperature;
    private int plateu;
    private double alpha;
    private double testProbability;
    private int[][] bestSolution;
    private int bestObjectiveFunction;
    private int[][] initialSolution;
    private int[][] currentSolution;
    private int[][] confGains;
    private int[][] confToIndexes;
    private int initialObjectiveFunction;
    private int currentObjectiveFunction;
    private int nConf;
    private int nQueries;
    private int nIndex;

//    private int randIndexNeighbour;
    private int[] queryServed;
    private int[] confUsed;
    private int[] indexUsed;
    private int[] indexCosts;
    private int[] indexMemory;
    private int totalMemory;
    private int initialCost;

//    private ArrayList<int[][]> neighbourhood = new ArrayList<>();
//    private ArrayList<Integer> neighbourhoodObj = new ArrayList<>();



    public SimulatedAnnealing(int nConf, int nQueries, int nIndex,int[][] initialSolution, int initialObjectiveFunction, int initialCost,
                               int[][] confGains,int[] queryServed,int[] confUsed, int[] indexUsed,int[][] confToIndexes, int[] indexCosts, int[] indexMemory, int totalMemory  ){
        this.temperature = 500;
        this.plateu = 1;
        this.alpha = 0.90;
        this.nConf = nConf;
        this.nQueries = nQueries;
        this.nIndex = nIndex;
        this.confGains = confGains;
        this.bestSolution = new int[nConf][nQueries];
        this.bestObjectiveFunction = initialObjectiveFunction;
        this.testProbability = Math.random();
        this.initialSolution = initialSolution;
        this.currentSolution = new int[nConf][nQueries];
        for(int i = 0; i<nConf; i++){
            System.arraycopy(initialSolution[i],0,currentSolution[i],0,nQueries);
            System.arraycopy(initialSolution[i],0,bestSolution[i],0,nQueries);
        }
        this.initialObjectiveFunction = initialObjectiveFunction;
        this.currentObjectiveFunction = initialObjectiveFunction;
        this.queryServed = queryServed;
        this.confUsed = confUsed;
        this.indexUsed = indexUsed;
        this.confToIndexes = confToIndexes;
        this.indexCosts = indexCosts;
        this.indexMemory = indexMemory;
        this.totalMemory = totalMemory;
        this.initialCost = initialCost;
    }

    // starting algorithm method
    public void start(){
        int i = 0;
        while(i<10000000) {
            int[][] neighbour = this.generateNeighbour();
            currentSolution = neighbour;
            if (this.bestObjectiveFunction - this.currentObjectiveFunction < 0) {
                bestObjectiveFunction = currentObjectiveFunction;
                bestSolution = neighbour;
                temperature *= alpha;

            } else if (this.calculateProbability(this.currentObjectiveFunction) > this.testProbability) {
                bestObjectiveFunction = currentObjectiveFunction;
                bestSolution = neighbour;
                temperature *= alpha;
            }
            i++;
        }

        this.printSolution();
    }

    private double calculateProbability(int objectiveFunction){
        return Math.exp(((float)-this.currentObjectiveFunction - objectiveFunction)/this.temperature);
    }

    private boolean acceptance(){
        return false;
    }

    private int[][] generateNeighbour(){
        int[][] currentNeighbour = new int[nConf][nQueries];

        int objectiveFunction = this.currentObjectiveFunction;
        boolean feaseble = false;
        int i;
        int tmp;
        for( i = 0; i<nConf; i++){
            System.arraycopy(currentSolution[i],0,currentNeighbour[i],0,nQueries);
        }
        while(!feaseble) {

            int iRand1 = 0;
            int iRand2 = 0;
            int jRand = 0;
            int cost = 0;
            int memory = 0;
            boolean taken1 = false,taken2 = false;

            while (currentNeighbour[iRand1][jRand] == currentNeighbour[iRand2][jRand]) {

                iRand1 = Math.abs(new Random().nextInt() % nConf);
                iRand2 = Math.abs(new Random().nextInt() % nConf);
                jRand = Math.abs(new Random().nextInt() % nQueries);


            }

            tmp = currentNeighbour[iRand1][jRand];
            currentNeighbour[iRand1][jRand] = currentNeighbour[iRand2][jRand];
            currentNeighbour[iRand2][jRand] = tmp;


            for (int j = 0; j < nQueries; j++) {
                if (currentNeighbour[iRand1][j] == 1) {
                    if (confUsed[iRand1] == 0) {
                        confUsed[iRand1]++;
                        for(i = 0; i<nIndex; i++){
                            if (confToIndexes[iRand1][i] == 1) {
                                indexUsed[i]++;
                            }

                        }
                    }
                    taken1 = true;

                }
                if (currentNeighbour[iRand2][j] == 1) {
                    if (confUsed[iRand2] == 0) {
                        confUsed[iRand2]++;
                        for(i = 0; i<nIndex; i++){
                            if (confToIndexes[iRand2][i] == 1) {
                                indexUsed[i]++;
                            }

                        }

                    }
                    taken2 = true;
                }
            }

            if (!taken1) {
                confUsed[iRand1]--;
                for (i = 0; i < nIndex; i++) {
                    if (confToIndexes[iRand1][i] == 1) {
                        indexUsed[i]--;
                    }

                }
            }

            if (!taken2) {
                confUsed[iRand2]--;
                for (i = 0; i < nIndex; i++) {
                    if (confToIndexes[iRand2][i] == 1) {
                        indexUsed[i]--;
                    }

                }
            }

            for(i=0; i<nIndex;i++){
                if(indexUsed[i] != 0){
                    cost+= indexCosts[i];
                    memory+= indexMemory[i];
                }
            }

            if(memory<totalMemory){
                feaseble = true;
            }

            objectiveFunction -= cost - initialCost;
            objectiveFunction += currentNeighbour[iRand1][jRand] ==  0 ? -confGains[iRand1][jRand] : confGains[iRand1][jRand];
            objectiveFunction += currentNeighbour[iRand2][jRand] ==  0 ? -confGains[iRand2][jRand] : confGains[iRand2][jRand];

        }


        return currentNeighbour;
    }

    private void printSolution(){
        for (int i = 0 ; i< nConf; i++){
           for (int j = 0; j<nQueries;j++){
               System.out.print(this.bestSolution[i][j]);

           }
           System.out.print("\n");
        }
        System.out.println("Obj : " + bestObjectiveFunction);
    }


}
