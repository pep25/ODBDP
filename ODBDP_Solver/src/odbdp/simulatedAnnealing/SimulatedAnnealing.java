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
    private int[][] currentSolution;
    private int[][] confGains;
    private int[][] confToIndexes;
    private int currentObjectiveFunction;
    private int nConf;
    private int nQueries;
    private int nIndex;
    private int[] queryServed;
    private int[] confUsed;
    private int[] indexUsed;
    private int[] indexCosts;
    private int[] indexMemory;
    private int totalMemory;
    private int currentCost;
    private int bestCost;
    private int[] currentConfUsed;
    private int[] currentIndexUsed;


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
        this.currentSolution = new int[nConf][nQueries];
        for(int i = 0; i<nConf; i++){
            System.arraycopy(initialSolution[i],0,currentSolution[i],0,nQueries);
            System.arraycopy(initialSolution[i],0,bestSolution[i],0,nQueries);
        }
        this.currentObjectiveFunction = initialObjectiveFunction;
        this.queryServed = queryServed;
        this.confUsed = confUsed;
        this.indexUsed = indexUsed;
        this.confToIndexes = confToIndexes;
        this.indexCosts = indexCosts;
        this.indexMemory = indexMemory;
        this.totalMemory = totalMemory;
        this.bestCost = initialCost;
        this.currentConfUsed = new int[nConf];
        this.currentIndexUsed = new int[nIndex];
    }

    // starting algorithm method
    public void start(){
        int i = 0;
        while(i<10000000) {
            int[][] neighbour = this.generateNeighbour();
            currentSolution = neighbour;
            if (this.bestObjectiveFunction - this.currentObjectiveFunction < 0) {
                this.bestObjectiveFunction = currentObjectiveFunction;
                this.bestSolution = neighbour;
                this.bestCost = this.currentCost;
                System.arraycopy(currentIndexUsed,0,indexUsed,0,nIndex);
                System.arraycopy(currentConfUsed,0,confUsed,0,nConf);
                this.temperature *= alpha;

            } else if (this.calculateProbability(this.currentObjectiveFunction) > this.testProbability) {
                this.bestObjectiveFunction = currentObjectiveFunction;
                this.bestSolution = neighbour;
                this.bestCost = this.currentCost;
                System.arraycopy(currentIndexUsed,0,indexUsed,0,nIndex);
                System.arraycopy(currentConfUsed,0,confUsed,0,nConf);
                this.temperature *= alpha;
            }
            i++;
        }

        this.printSolution();
    }

    private double calculateProbability(int objectiveFunction){
        return Math.exp(((float)-this.bestObjectiveFunction - objectiveFunction)/this.temperature);
    }

    private boolean acceptance(){
        return false;
    }

    private int[][] generateNeighbour(){
        int[][] currentNeighbour = new int[nConf][nQueries];
        int objectiveFunction = 0;
        int cost = 0;
        boolean feaseble = false;
        int i;
        int tmp;


        while(!feaseble) {
            for( i = 0; i<nConf; i++){
                System.arraycopy(bestSolution[i],0,currentNeighbour[i],0,nQueries);
            }
            System.arraycopy(confUsed,0,currentConfUsed,0,nConf);
            System.arraycopy(indexUsed,0,currentIndexUsed,0,nIndex);
            cost = 0;
            objectiveFunction = this.bestObjectiveFunction;
            int iRand1 = 0;
            int iRand2 = 0;
            int jRand = 0;
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
                    if (currentConfUsed[iRand1] == 0) {
                        currentConfUsed[iRand1]++;
                        for(i = 0; i<nIndex; i++){
                            if (confToIndexes[iRand1][i] == 1) {
                                currentIndexUsed[i]++;
                            }

                        }
                    }
                    taken1 = true;

                }
                if (currentNeighbour[iRand2][j] == 1) {
                    if (currentConfUsed[iRand2] == 0) {
                        currentConfUsed[iRand2]++;
                        for(i = 0; i<nIndex; i++){
                            if (confToIndexes[iRand2][i] == 1) {
                                currentIndexUsed[i]++;
                            }

                        }

                    }
                    taken2 = true;
                }
            }

            if (!taken1) {
                currentConfUsed[iRand1]--;
                for (i = 0; i < nIndex; i++) {
                    if (confToIndexes[iRand1][i] == 1) {
                        currentIndexUsed[i]--;
                    }

                }
            }

            if (!taken2) {
                currentConfUsed[iRand2]--;
                for (i = 0; i < nIndex; i++) {
                    if (confToIndexes[iRand2][i] == 1) {
                        currentIndexUsed[i]--;
                    }

                }
            }

            for(i=0; i<nIndex;i++){
                if(currentIndexUsed[i] != 0){
                    cost+= indexCosts[i];
                    memory+= indexMemory[i];
                }
            }

            if(memory<totalMemory){
                feaseble = true;
                objectiveFunction -= cost - bestCost;
                objectiveFunction += currentNeighbour[iRand1][jRand] ==  0 ? -confGains[iRand1][jRand] : confGains[iRand1][jRand];
                objectiveFunction += currentNeighbour[iRand2][jRand] ==  0 ? -confGains[iRand2][jRand] : confGains[iRand2][jRand];
            }



        }

        currentObjectiveFunction = objectiveFunction;
        currentCost = cost;
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
