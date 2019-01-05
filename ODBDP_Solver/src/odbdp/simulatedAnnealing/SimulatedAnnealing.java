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
    private int neighbourObjectiveFunction;
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
    private int[] neighbourConfUsed;
    private int[] neighbourIndexUsed;
    private int[] neighbourQueryServed;
    private int currentMemory;
    private int[] currentQueryServed;
    private int neighbourMemory;
    private int neighbourCost;
    private int bestMemory;


    public SimulatedAnnealing(int nConf, int nQueries, int nIndex, int[][] initialSolution, int initialObjectiveFunction, int initialCost,
                              int[][] confGains, int[] confUsed, int[] indexUsed, int[][] confToIndexes, int[] indexCosts, int[] indexMemory, int totalMemory) {
        this.temperature = 900;
        this.plateu = 6;
        this.alpha = 0.99;
        this.nConf = nConf;
        this.nQueries = nQueries;
        this.nIndex = nIndex;
        this.confGains = confGains;
        this.bestSolution = new int[nConf][nQueries];
        this.bestObjectiveFunction = initialObjectiveFunction;
        this.testProbability = Math.random();
        this.currentSolution = new int[nConf][nQueries];
        for (int i = 0; i < nConf; i++) {
            System.arraycopy(initialSolution[i], 0, currentSolution[i], 0, nQueries);
            System.arraycopy(initialSolution[i], 0, bestSolution[i], 0, nQueries);
        }

        this.currentObjectiveFunction = initialObjectiveFunction;
        this.confUsed = confUsed;
        this.indexUsed = indexUsed;
        this.confToIndexes = confToIndexes;
        this.indexCosts = indexCosts;
        this.indexMemory = indexMemory;
        this.totalMemory = totalMemory;
        this.bestCost = initialCost;
        this.currentCost = initialCost;
        this.currentConfUsed = new int[nConf];
        this.currentIndexUsed = new int[nIndex];
        this.neighbourConfUsed = new int[nConf];
        this.neighbourIndexUsed = new int[nIndex];
        this.neighbourQueryServed = new int[nQueries];
        System.arraycopy(confUsed, 0, currentConfUsed, 0, nConf);
        System.arraycopy(indexUsed, 0, currentIndexUsed, 0, nIndex);
        this.queryServed = new int[nQueries];
        this.currentQueryServed = new int[nQueries];
        for(int i = 0; i<nConf;i++){
            for(int j = 0; j<nQueries;j++){
                if(initialSolution[i][j] == 1){
                    this.queryServed[j] = i;
                    this.currentQueryServed[j] = i;
                }
            }
        }


    }

    // starting algorithm method
    public void start() {
        int i = 0;
        while (i < 1000000) {
            int[][] neighbour = this.generateNeighbour();
            if (this.currentObjectiveFunction - this.neighbourObjectiveFunction < 0) {
                this.currentObjectiveFunction = this.neighbourObjectiveFunction;
                for (int j = 0; j < nConf; j++) {
                    System.arraycopy(neighbour[j], 0, this.currentSolution[j], 0, nQueries);
                }
                this.currentCost = this.neighbourCost;
                this.currentMemory = this.neighbourMemory;
                System.arraycopy(neighbourIndexUsed, 0, currentIndexUsed, 0, nIndex);
                System.arraycopy(neighbourConfUsed, 0, currentConfUsed, 0, nConf);
                System.arraycopy(neighbourQueryServed, 0, currentQueryServed, 0, nQueries);
                if(i % plateu == 0){
                    this.temperature *= alpha;
                }
                this.acceptance();

            } else if (this.calculateProbability(this.neighbourObjectiveFunction) > this.testProbability) {
                this.currentObjectiveFunction = this.neighbourObjectiveFunction;
                for (int j = 0; j < nConf; j++) {
                    System.arraycopy(neighbour[j], 0, this.currentSolution[j], 0, nQueries);
                }
                this.currentCost = this.neighbourCost;
                this.currentMemory = this.neighbourMemory;
                System.arraycopy(neighbourIndexUsed, 0, currentIndexUsed, 0, nIndex);
                System.arraycopy(neighbourConfUsed, 0, currentConfUsed, 0, nConf);
                System.arraycopy(neighbourQueryServed, 0, currentQueryServed, 0, nQueries);
                if(i % plateu == 0){
                    this.temperature *= alpha;
                }
                this.acceptance();
            }
            i++;
        }

        this.printSolution();
    }

    private double calculateProbability(int objectiveFunction) {
        return Math.exp(((float) -(this.currentObjectiveFunction - objectiveFunction)) / this.temperature);
    }

    private void acceptance() {
        if (this.currentMemory < totalMemory) {
            this.bestMemory = currentMemory;
            this.bestObjectiveFunction = currentObjectiveFunction;
            System.arraycopy(currentIndexUsed, 0, indexUsed, 0, nIndex);
            System.arraycopy(currentConfUsed, 0, confUsed, 0, nConf);
            System.arraycopy(currentQueryServed, 0, queryServed, 0, nQueries);
            this.bestCost = currentCost;
            for (int i = 0; i < nConf; i++) {
                System.arraycopy(this.currentSolution[i], 0, this.bestSolution[i], 0, nQueries);
            }

        }
    }

    private int[][] generateNeighbour() {

        int[][] currentNeighbour = new int[nConf][nQueries];
        int objectiveFunction;
        int cost = 0;
        int iRand1;
        int iRand2;
        int jRand;
        int memory = 0;
        int i;

        for (i = 0; i < nConf; i++) {
            System.arraycopy(currentSolution[i], 0, currentNeighbour[i], 0, nQueries);
        }

        System.arraycopy(currentQueryServed,0,neighbourQueryServed,0,nQueries);
        System.arraycopy(currentConfUsed, 0, neighbourConfUsed, 0, nConf);
        System.arraycopy(currentIndexUsed, 0, neighbourIndexUsed, 0, nIndex);
        objectiveFunction = this.currentObjectiveFunction;

        boolean taken1 = false, taken2 = false;

        jRand = Math.abs(new Random().nextInt() % nQueries);
        iRand1 = Math.abs(new Random().nextInt() % nConf);
        iRand2 = neighbourQueryServed[jRand];
        neighbourQueryServed[jRand] = iRand1;

        currentNeighbour[iRand2][jRand] = 0;
//        neighbourConfUsed[iRand2] --;
        currentNeighbour[iRand1][jRand] = 1;
//        neighbourConfUsed[iRand1] ++;

//        for(i = 0; i<nIndex; i++){
//            if(confToIndexes[iRand1][i] == 1){
//                neighbourIndexUsed[i]++;
//            }
//        }
//
//        if(neighbourConfUsed[iRand2] == 0){
//            for (i = 0 ;i<nIndex;i++){
//                if(confToIndexes[iRand2][i] == 1){
//                    neighbourIndexUsed[i]--;
//                }
//            }
//        }

        for (int j = 0; j < nQueries; j++) {
            if (currentNeighbour[iRand1][j] == 1) {
                if (neighbourConfUsed[iRand1] == 0) {
                    neighbourConfUsed[iRand1]++;
                    for (i = 0; i < nIndex; i++) {
                        if (confToIndexes[iRand1][i] == 1) {
                            neighbourIndexUsed[i]++;
                        }

                    }
                }
                taken1 = true;

            }
            if (currentNeighbour[iRand2][j] == 1) {
                if (neighbourConfUsed[iRand2] == 0) {
                    neighbourConfUsed[iRand2]++;
                    for (i = 0; i < nIndex; i++) {
                        if (confToIndexes[iRand2][i] == 1) {
                            neighbourIndexUsed[i]++;
                        }

                    }

                }
                taken2 = true;
            }
        }

        if (!taken1) {
            neighbourConfUsed[iRand1]--;
            for (i = 0; i < nIndex; i++) {
                if (confToIndexes[iRand1][i] == 1) {
                    neighbourIndexUsed[i]--;
                }

            }
        }

        if (!taken2) {
            neighbourConfUsed[iRand2]--;
            for (i = 0; i < nIndex; i++) {
                if (confToIndexes[iRand2][i] == 1) {
                    neighbourIndexUsed[i]--;
                }

            }
        }

        for (i = 0; i < nIndex; i++) {
            if (neighbourIndexUsed[i] != 0) {
                cost += indexCosts[i];
                memory += indexMemory[i];
            }
        }

        objectiveFunction -= cost - currentCost;
        objectiveFunction +=  confGains[iRand1][jRand];
        objectiveFunction -=  confGains[iRand2][jRand];

        neighbourObjectiveFunction = objectiveFunction;
        neighbourCost = cost;
        neighbourMemory = memory;

        return currentNeighbour;
    }

    private void printSolution() {
        for (int i = 0; i < nConf; i++) {
            for (int j = 0; j < nQueries; j++) {
                System.out.print(this.bestSolution[i][j]);

            }
            System.out.print("\n");
        }
        System.out.println("Obj : " + bestObjectiveFunction);
        System.out.println("Memory : " + bestMemory);
    }


}
