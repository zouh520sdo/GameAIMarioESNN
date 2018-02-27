package gameai;
import ch.idsia.benchmark.tasks.BasicTask;

public class MyESTest 
{
    final static int generations = 100;
    final static int populationSize = 120;

    public static void main(String[] args)
    {
        ESnew es = new ESnew(populationSize);
        es.cmdLineOptions.setPauseWorld(false);
        System.out.println("Evolving ");
        for (int gen = 0; gen < generations; gen++)
        {
        	if(gen%10 == 0 && gen != 0)
        		es.cmdLineOptions.setVisualization(true);
            es.nextGeneration();
            double bestResult = es.getBestFitnesses();
            System.out.println("Generation " + gen + " best " + bestResult);
        }
        es.cmdLineOptions.setVisualization(true);
        es.nextGeneration();
    }
}