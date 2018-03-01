package gameai;
import ch.idsia.benchmark.tasks.BasicTask;

public class MyESTest 
{
    final static int generations = 200;
    final static int populationSize = 150;

    public static void main(String[] args)
    {

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
            if(gen%5 == 0)
        		es.cmdLineOptions.setVisualization(true);
        }
        es.cmdLineOptions.setVisualization(true);
        es.nextGeneration();
        double bestResult = es.getBestFitnesses(0);
        double secondResult = es.getBestFitnesses(1);
        System.out.println("Generation " + generations + " best " + bestResult);
        System.out.println("Generation " + generations + " best2 " + secondResult);
    }
}