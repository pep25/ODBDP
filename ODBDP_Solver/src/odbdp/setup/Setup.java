package odbdp.setup;
import odbdp.simulatedAnnealing.SimulatedAnnealing;

import java.util.Random;

public class Setup {

	public static void main(String[] args) {
		if(!args[1].equals("-t")){
			// check command line syntax
			System.out.println("Sintassi comando non corretta");
			System.exit(3);
		}
		// get execution time in seconds
		int executionTime = Integer.parseInt(args[2]);
		String filename = args[0];


		TextInputReader t = new TextInputReader();
		try{
			t.readFile(filename);
		}catch (Exception e){
			e.printStackTrace();
		}
		int[][] test = {{1,1,0},{0,0,1},{0,0,0},{0,0,0}};
		int[] queryServed = {1,1,1};
		int[] confUsed = {1,1,0,0};
		int[] indexUsed = {0,2,2,0,1};
		SimulatedAnnealing algorithm = new SimulatedAnnealing(t.getnConf(),t.getnQueries(),t.getnIndexes(),test
				,3,38,t.getConfToQueries(),queryServed,confUsed,indexUsed,
				t.getConfToIndexes(),t.getIndexCosts(),t.getIndexMemory(),t.getMemory());
		algorithm.start();

	}

}
