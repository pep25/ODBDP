package odbdp.setup;

public class Setup {

	public static void main(String[] args) {
		if(!args[2].equals("-t")){
			// check command line syntax
			System.out.println("Sintassi comando non corretta");
			System.exit(3);
		}
		// get execution time in seconds
		int executionTime = Integer.parseInt(args[3]);
		String filename = args[1];


		TextInputReader t = new TextInputReader();
		try{
			t.readFile(filename);
		}catch (Exception e){
			e.printStackTrace();
		}

	}

}
