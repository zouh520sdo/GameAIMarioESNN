package gameai;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MyESTest 
{
    final static int generations = 30000;
    final static int populationSize = 120;

    public static void main(String[] args)
    {
    	try{
    		FileWriter f = new FileWriter("2point_0_result.txt", false);
    		PrintWriter p = new PrintWriter(f);
    	
	        ESnew es = new ESnew(populationSize, "2point_0_start.txt");
	        es.cmdLineOptions.setPauseWorld(false);
	        es.cmdLineOptions.setLevelDifficulty(0);
	        System.out.println("Evolving ");
	        for (int gen = 0; gen < generations; gen++)
	        {
	            es.nextGeneration();
	            double bestResult = es.getBestFitnesses(0);
	            double bestResult2 = es.getBestFitnesses(1);
	            p.println("Generation " + gen + " best " + bestResult + ", second: " + bestResult2);
	            System.out.println("Generation " + gen + " best " + bestResult + ", second: " + bestResult2);
	            if(gen%10 == 0)
	        		es.cmdLineOptions.setVisualization(true);
	            p.flush();
	        }
	        es.cmdLineOptions.setVisualization(true);
	        es.nextGeneration();
	        double bestResult = es.getBestFitnesses(0);
	        double bestResult2 = es.getBestFitnesses(1);
	        p.println("Generation " + generations + " best " + bestResult + ", second: " + bestResult2);

	        p.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
}