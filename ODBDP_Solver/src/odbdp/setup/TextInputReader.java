package odbdp.setup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public final class TextInputReader {
	
	private int nConf, nIndexes, nQueries, memory;
	
	private int[] indexCosts, indexMemory;
	
	private int[][] confToIndexes,confToQueries;

	public int[] confTotalCost;
	public int[] confTotalMemory;

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

	public int[] getConfTotalCost() {
		return confTotalCost;
	}

	public int[] getConfTotalMemory() {
		return confTotalMemory;
	}

	public void readFile (String f) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(f));
		String s;
		
		//number of queries
		s = br.readLine();
		int i = s.lastIndexOf(" ");
		nQueries = Integer.parseInt(s.substring(i+1, s.length()));
		
		
		//number of indexes
		s = br.readLine();
		i = s.lastIndexOf(" ");
		nIndexes = Integer.parseInt(s.substring(i+1, s.length()));
		
		
		//number of configurations
		s = br.readLine();
		i = s.lastIndexOf(" ");
		nConf = Integer.parseInt(s.substring(i+1, s.length()));
		
		
		//total available memory
		s = br.readLine();
		i = s.lastIndexOf(" ");
		memory = Integer.parseInt(s.substring(i+1, s.length()));
		
		
		//initialization of variables 
		indexCosts = new int[nIndexes];
		indexMemory = new int[nIndexes];
		confToIndexes = new int[nConf][nIndexes];
		confToQueries = new int[nConf][nQueries];
		
		br.readLine();	//infoless line
		
		
		//configuration indexes matrix
		for (int j = 0; j < nConf; j++) {
			s = br.readLine();
			for (int z = 0; z < s.length(); z = z+2)
				confToIndexes[j][z/2] = Integer.parseInt(s.substring(z, z+1));
		}
		
		
		//indexes fixed cost
		br.readLine();	//infoless line
		for (int j = 0; j < nIndexes; j++) {	
			s = br.readLine();
			indexCosts[j] = Integer.parseInt(s);
		}
		
		
		//indexes memory occupation
		br.readLine();	//infoless line
		for (int j = 0; j < nIndexes; j++) {	
			s = br.readLine();
			indexMemory[j] = Integer.parseInt(s);
		}
		
		br.readLine();	//infoless line
		
		for (int j = 0; j < nConf; j++) {
			s = br.readLine();
			int upper;            //we save an upper index value in order to create substrings
			int column = 0;        //we save the column index of the incoming value
			int z = 0;            //loop index
			while (z < s.length()) {
				upper = z;
				while (upper < s.length() - 1 && s.charAt(upper) != ' ')
					upper++;
				if (upper != z && upper != s.length() - 1)
					confToQueries[j][column] = Integer.parseInt(s.substring(z, upper));
				else
					confToQueries[j][column] = Integer.parseInt(s.substring(z, upper + 1));
				z = upper + 1;
				column++;
			}
		}
		
		br.close();
	}


}