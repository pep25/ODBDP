package solver;

import java.io.File;
import java.util.Random;

import odbdp.setup.TextInputReader;

public final class TabuSearch implements Runnable {

    private static Random r = new Random();
    private TextInputReader t = new TextInputReader();
    private ParameterHolder p1,p2,p3;					//p1 = previous instance, p2 = current instance, p3 = best instance
    private TabuSolution ts;

    private int nConf;
    private int nIndexes;
    private int nQueries;
    private int memory;
    private int indexCosts [];
    private int indexMemory [];
    private int [][] confToIndexes;
    private int [][] confToQueries;
    private int [] tabu;
    private int [][] solution;
    private int [] confCost;	//beginning				//for each configuration we need to know what is its cost (sum of indexes cost)
    private int currentBest;	//the current best found so far

    public int getNConf() {
        return nConf;
    }

    public int getNIndexes() {
        return nIndexes;
    }

    public int getNQueries() {
        return nQueries;
    }

    public int getMemory() {
        return memory;
    }

    public int[] getIndexCosts() {
        return indexCosts;
    }

    public int[] getIndexMemory() {
        return indexMemory;
    }

    public int[][] getConfToIndexes() {
        return confToIndexes;
    }

    public int[][] getConfToQueries() {
        return confToQueries;
    }

    public int[][] getSolution() {
        return solution;
    }

    public int getCurrentBest() {
        return currentBest;
    }

    public TabuSearch (File f) throws Exception {
        setParameters(f);
    }

    private void setSolution() {
        for (int i = 0; i < nQueries; i++)
            solution[p3.queriesToConf[i]][i] = 1;
        p3.calculateIndexCost();
    }

    public void setFile(File f) throws Exception {
        setParameters(f);
    }

    public ParameterHolder getP3() {
        return p3;
    }

    private void setParameters(File f) throws Exception {
        t.readFile(f);
        nConf = t.getnConf();
        nIndexes = t.getnIndexes();
        nQueries = t.getnQueries();
        memory = t.getMemory();
        indexCosts = t.getIndexCosts();
        indexMemory = t.getIndexMemory();
        confToIndexes = t.getConfToIndexes();
        confToQueries = t.getConfToQueries();
        solution = new int[nConf][nQueries];
        confCost = new int [nConf];
        setConfCost();
        currentBest = 0;
        p1 = new ParameterHolder();
        p2 = new ParameterHolder();
        p3 = new ParameterHolder();
        ts = new TabuSolution(this);
        if (nConf < 10)
            tabu = new int [3];
        else if (nConf < 30)
            tabu = new int [5];
        else if (nConf < 100)
            tabu = new int [8];
        else if (nConf < 1000)
            tabu = new int [20];
        else
            tabu = new int [30];
        for (int i = 0; i < tabu.length; i++)
            tabu[i] = -1;						//we're saying that no tabu moves have been inserted yet
    }

    private void setConfCost() {
        for (int i = 0; i < nConf; i++) {
            int result;
            for (int j = 0; j < nIndexes; j++) {
                result = indexCosts[j]*confToIndexes[i][j];
                confCost[i] += result;
            }
        }
    }

    public String toString() {
        ts.toString();
        System.out.println();
        System.out.println("Soluzione trovata:");
        for (int i = 0; i < nConf; i++) {
            for (int j = 0; j < nQueries; j++)
                System.out.print(solution[i][j] + " ");
            System.out.println();
        }
        System.out.println();
        System.out.println();
        return "";
    }


    public class ParameterHolder {

        private int [] activeConf;	//first part	    	//at the very beginning all the configurations are deactivated
        private int [] usedConf;	//second part			//it says how many queries are linked to that particular configuration
        private int [] activeQueries;
        private int [] activeIndexes;  //first part	        //for each index we save the number of active configurations linked to it
        private int [] queriesToConf;  //second part		//it shows which configuration is currently linked to which query
        private int best, bestConf;
        private int indexCost;						//it represents the total cost of all active indexes

        public int[] getActiveConf() {
            return activeConf;
        }

        public int[] getUsedConf() {
            return usedConf;
        }

        public int[] getActiveIndexes() {
            return activeIndexes;
        }

        public int[] getActiveQueries() {
            return activeQueries;
        }

        public int[] getQueriesToConf() {
            return queriesToConf;
        }

        public int getBest() {
            return best;
        }

        private ParameterHolder() {
            setValues();
        }

        public int getBestConf() {
            return bestConf;
        }

        public int getIndexCost() {
            return indexCost;
        }

        private void setValues() {
            solution = new int [nConf][nQueries];
            activeConf = new int [nConf];
            usedConf = new int[nConf];
            activeIndexes = new int [nIndexes];
            activeQueries = new int [nQueries];
            best = 0;
            bestConf = -1;
            indexCost = 0;
            queriesToConf = new int[nQueries];		//we save the first and the second best configuration
            for (int i = 0; i < queriesToConf.length; i++)
                queriesToConf[i] = -1;					//we say that non configurations are linked to the i-th query
        }


        private void copy (ParameterHolder ph, int bestConf) {
            for (int i = 0; i < activeConf.length; i++)
                activeConf[i] = ph.activeConf[i];
            for (int i = 0; i < usedConf.length; i++)
                usedConf[i] = ph.usedConf[i];
            for (int i = 0; i < activeIndexes.length; i++)
                activeIndexes[i] = ph.activeIndexes[i];
            for (int i = 0; i < activeQueries.length; i++)
                activeQueries[i] = ph.activeQueries[i];
            for (int i = 0; i < queriesToConf.length; i++)
                queriesToConf[i] = ph.queriesToConf[i];
            best = ph.best;
            this.bestConf = bestConf;
        }

        private boolean checkSolution() {		//we check if the current feasible solution is better than the best found so far
            int totalCost = 0;
            int totalGain = 0;
            for (int i = 0; i < nConf; i++)
                if (usedConf[i] > 0)			//an active configuration which is not linked to any query just weighs on the memory occupation
                    totalCost += confCost[i];
            for (int i = 0; i < nQueries; i++)
                if (queriesToConf[i] != -1)
                    totalGain += activeQueries[i]*confToQueries[queriesToConf[i]][i];
            if (totalGain - totalCost > currentBest) {
                best = totalGain - totalCost;
                currentBest = best;
                return true;
            }
            return false;
        }

        public String toString() {
            System.out.print("Configurazioni attive: ");
            for (int i = 0; i < nConf; i++)
                System.out.print(activeConf[i] + " ");
            System.out.println();
            System.out.println();

            System.out.print("Configurazioni usate: ");
            for (int i = 0; i < nConf; i++)
                System.out.print(usedConf[i] + " ");
            System.out.println();
            System.out.println();

            System.out.print("Indici attivi: ");
            for (int i = 0; i < nIndexes; i++)
                System.out.print(activeIndexes[i] + " ");
            System.out.println();
            System.out.println();

            System.out.print("Configurazioni legate alle query: ");
            for (int i = 0; i < nQueries; i++)
                System.out.print(queriesToConf[i] + " ");
            System.out.println();
            System.out.println();

            System.out.println("Ottimo: " + best);
            return "";
        }

        private int checkMemory() {
            int total = 0;
            for (int i = 0; i < nIndexes; i++)
                if (activeIndexes[i] > 0)
                    total += indexMemory[i];
            return total;
        }

        private void calculateIndexCost() {
            for (int i = 0; i < nIndexes; i++)
                if (activeIndexes[i] > 0)
                    indexCost += indexCosts[i];
        }
    }

    public TabuSearch.ParameterHolder getBest() {
        return p3;
    }



    public class TabuSolution {

        private int nConf;
        private int nIndexes;
        private int nQueries;
        private int memory;
        private int indexCosts [];
        private int indexMemory [];
        private int [][] confToIndexes;
        private int [][] confToQueries;
        private int [][] solution;
        private int currentBest;							//the current best found so far


        private int [] activeConf;	//first part	    	//at the very beginning all the configurations are deactivated
        private int [] usedConf;	//second part			//it says how many queries are linked to that particular configuration
        private int [] activeIndexes;  //first part	        //for each index we save the number of active configurations linked to it
        private int [] activeQueries;						//list of active (1) and non active (0) queries
        private int [] queriesToConf;  //second part		//it shows which configuration is currently linked to which query
        private int best;
        private int indexCost;

        public int getnConf() {
            return nConf;
        }

        public int getnIndexes() {
            return nIndexes;
        }

        public int getnQueries() {
            return nQueries;
        }

        public int getMemory() {
            return memory;
        }

        public int[] getIndexCosts() {
            return indexCosts;
        }

        public int[] getIndexMemory() {
            return indexMemory;
        }

        public int[][] getConfToIndexes() {
            return confToIndexes;
        }

        public int[][] getConfToQueries() {
            return confToQueries;
        }

        public int[][] getSolution() {
            return solution;
        }

        public int getCurrentBest() {
            return currentBest;
        }

        public int[] getActiveConf() {
            return activeConf;
        }

        public int[] getUsedConf() {
            return usedConf;
        }

        public int[] getActiveIndexes() {
            return activeIndexes;
        }

        public int[] getActiveQueries() {
            return activeQueries;
        }

        public int[] getQueriesToConf() {
            return queriesToConf;
        }

        public int getBest() {
            return best;
        }

        public int getIndexCost() {
            return indexCost;
        }

        private TabuSolution (TabuSearch ts) {
            nConf = ts.getNConf();
            nIndexes = ts.getNIndexes();
            nQueries = ts.getNQueries();
            memory = ts.getMemory();
            indexCosts = ts.getIndexCosts();
            indexMemory = ts.getIndexMemory();
            confToIndexes = ts.getConfToIndexes();
            confToQueries = ts.getConfToQueries();
            solution = ts.getSolution();
            currentBest = ts.currentBest;
        }

        private void setSolution(ParameterHolder ph) {
            activeConf = ph.getActiveConf();
            usedConf = ph.getUsedConf();
            activeIndexes = ph.getActiveIndexes();
            activeQueries = ph.getActiveQueries();
            queriesToConf = ph.getQueriesToConf();
            best = ph.getBest();
            indexCost = ph.getIndexCost();
        }

        public String toString() {
            System.out.println("Indici attivi:");
            for (int i = 0; i < nIndexes; i++)
                System.out.print(activeIndexes[i] + " ");

            System.out.println();
            System.out.println();

            System.out.println("Configurazioni attive:");
            for (int i = 0; i < nConf; i++)
                System.out.print(activeConf[i] + " ");

            System.out.println();
            System.out.println();

            System.out.println("Configurazioni usate:");
            for (int i = 0; i < nConf; i++)
                System.out.print(usedConf[i] + " ");

            System.out.println();
            System.out.println();

            System.out.println("Query attive:");
            for (int i = 0; i < nQueries; i++)
                System.out.print(activeQueries[i] + " ");

            System.out.println();
            System.out.println();

            System.out.println("Ottimo: " + best);

            System.out.println();
            System.out.println();

            System.out.println("Il costo totale degli indici attivi è: " + indexCost);

            return "";

        }

    }



    public void run() {
        int nIterations = 1;
        int currentMemory = 0;		//at the very beginning all the indexes are off
        boolean feasibility = true; //we avoid possible infeasibilities
        boolean tabuMove = false;	//if a move is tabu, we cannot activate/deactivate that particular configuration
        int lastTabu = 0;			//index of the last tabu move inserted
        p2.copy(p1,-1);                //we initialize the actual neighbor as it was the initial one
        while (nIterations < 10000) {	//the exit condition will be given by the external main thraed
            for(int i = 0; i < nConf; i++) {
                tabuMove = false;		//we're assuming we can move to that neighbor
                currentMemory = p1.checkMemory();		//we calculate the total memory after we modify a configuration
                for (int t = 0; t < tabu.length; t++) {
                    if (tabu[t] == i) {	//we're doing a tabu move. We signal it.
                        tabuMove = true;
                    }
                }
                if (!(tabuMove) && p1.activeConf[i] == 0) {		//we're activating the i-th configuration
                    feasibility = true;		//at the very beginning we have no infeasibilities
                    p2.activeConf[i] = 1;
                    for (int j = 0; j < nIndexes; j++)
                        p2.activeIndexes[j] += confToIndexes[i][j];	//we increase by 1 each index used by the i-th configuration
                    for (int j = 0; j < nIndexes; j++)
                        if (p2.activeIndexes[j] > 0)
                            currentMemory += indexMemory[j];			//each active index contributes for the total memory occupation
                    if (currentMemory > memory) {					//we have an infeasible situation. Backtracking
                        feasibility = false;						//we lost feasibility. We signal it.
                    }
                    if (feasibility) {					//we continue if and only if the actual situation is feasible

                        int query = r.nextInt(nQueries);		//we start from a random query
                        int ind = 0;
                        for (int q = query; ind < nQueries; q = (q+1) % nQueries) {
                            if (p2.queriesToConf[q] == -1) {	//no configuration linked to the q-th query
                                p2.queriesToConf[q] = i;
                                p2.usedConf[i]++;
                                p2.activeQueries[q] = 1;
                            }
                            else {						//the q-th query is already linked to an another configuration
                                if (confToQueries[i][q]- confCost[i] > confToQueries[p2.queriesToConf[q]][q] - confCost[p2.queriesToConf[q]]) {
                                    p2.usedConf[p2.queriesToConf[q]] --;
                                    if (p2.usedConf[p1.queriesToConf[q]] == 0) {		//the discarded configuration is not linked to any query anymore
                                        p2.activeConf[p2.queriesToConf[q]] = 0;
                                        for (int p = 0; p < nIndexes; p++) {
                                            if (confToIndexes[p1.queriesToConf[q]][p] == 1) {		//decrease the utilization of that index
                                                p2.activeIndexes[p]--;
                                                if (p2.activeIndexes[p] == 0)
                                                    currentMemory -= indexMemory[p];		//that index was used only by the deactivating configuration
                                            }
                                        }
                                    }
                                    p2.queriesToConf[q] = i;
                                    p2.usedConf[i]++;
                                }
                            }
                            ind++;
                        }
                    }
                }
                else if (p2.activeConf[i] == 1 && !(tabuMove)) {		//we're deactivating th i-th configuration
                    p2.activeConf[i] = 0;
                    for (int j = 0; j < nIndexes; j++) {
                        p2.activeIndexes[j] -= confToIndexes[i][j];	//we decrease by 1 each index used by the i-th configuration
                        if (p2.activeIndexes[j] == 0 && confToIndexes[i][j] == 1)	//decrease the total memory used
                            currentMemory -= indexMemory[j];
                    }
                    for (int c = 0; c < nQueries; c++) {
                        int max = 0;								//we're finding the second best configuration in terms of gain
                        int index = 0;
                        if (p2.queriesToConf[c] == i) {			//we're deactivating the configuration of the query
                            max = 0;
                            for (int q = 0; q < nConf; q++) {	//we find the second best one
                                if (q != i && p2.activeConf[q] == 1 && confToQueries[q][c] > max) {		//we're not considering the i-th configuration
                                    max = confToQueries[q][c];
                                    index = q;
                                }
                            }
                        }
                    }
                }
                boolean check = p2.checkSolution();
                if (!(tabuMove) && feasibility && check) {		//we have found a better solution
                    p3.copy(p2,i);            								//we copy the current solution into the best solution and the index of the configuration
                }
                p2.copy(p1,-1);  		//each time we finish analyzing a neighbor, we must come back to the initial status
            }
            int i = p3.getBestConf();
            tabu[lastTabu] = i;
            lastTabu = (lastTabu+1)%tabu.length;
            p1.copy(p3,1);        //after analyzing a neighborhood, we'll start from the best neighbor next time.
            nIterations++;		//we chose the best neighbor in the current neighborhood. The next iteration will start from that neighbor
        }
        setSolution();     //we save the last best solution found
        ts.setSolution(p3);
        toString();
    }


    public static void main (String [] args) {
        File f = new File("C:\\Users\\Antonio\\Desktop\\Documento.txt");
        try{
            TabuSearch ts = new TabuSearch(f);
            Thread t = new Thread(ts);
            t.start();
        } catch (Exception e) {System.out.println(e);}
    }
}

