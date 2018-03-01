package gameai;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import ch.idsia.benchmark.tasks.BasicTask;

public class MyESTest 
{
    final static int generations = 200;
    final static int populationSize = 150;

    public static void main(String[] args)
    {
    	String outfile = "LevelOutput.txt";
    	
    	try {
	    	FileWriter f = new FileWriter(outfile, false);
	    	PrintWriter p = new PrintWriter(f);
	    	
	    	
	        ESnew es = new ESnew(populationSize);//, "best.txt");
	        
	        es.cmdLineOptions.setPauseWorld(false);
	        System.out.println("Evolving ");
	        for (int gen = 0; gen < generations; gen++)
	        {
	            es.nextGeneration();
	            double bestResult = es.getBestFitnesses(0);
	            double secondResult = es.getBestFitnesses(1);
	            System.out.println("Generation " + gen + " best " + bestResult);
	            System.out.println("Generation " + gen + " best2 " + secondResult);
	            p.println("Generation " + gen + " best " + bestResult);
	            p.println("Generation " + gen + " best2 " + secondResult);
	            
	            p.flush();
	            if(gen%5 == 0)
	        		es.cmdLineOptions.setVisualization(true);
	        }
	        es.cmdLineOptions.setVisualization(true);
	        es.nextGeneration();
	        double bestResult = es.getBestFitnesses(0);
	        double secondResult = es.getBestFitnesses(1);
	        System.out.println("Generation " + generations + " best " + bestResult);
	        System.out.println("Generation " + generations + " best2 " + secondResult);
	        p.println("Generation " + generations + " best " + bestResult);
	        p.println("Generation " + generations + " best2 " + secondResult);
	        
	        p.close();
    	} catch (IOException e)
    	{
    		e.printStackTrace();
    	}
    }
}