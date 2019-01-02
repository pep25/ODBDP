package odbdp.setup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public final class TextOutputWriter {


	public static File writeSolution (int[][] sol, String path) throws Exception {
		File f = new File(path);
		f.createNewFile();
		String s = new String();
		BufferedWriter br = new BufferedWriter(new FileWriter(f));
		for(int i = 0; i < sol.length; i++) {
			for (int j = 0; j < sol[0].length; j++) {
				s += String.valueOf(sol[i][j]);
				s += " ";
			}							
			br.write(s);
			br.newLine();			//carriage return
			s = new String();
		}
		br.close();
		return f;
	}
}
