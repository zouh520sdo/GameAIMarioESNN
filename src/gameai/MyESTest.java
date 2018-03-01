package gameai;
import ch.idsia.benchmark.tasks.BasicTask;

public class MyESTest 
{
    final static int generations = 1000;
    final static int populationSize = 120;

    public static void main(String[] args)
    {
        ESnew es = new ESnew(populationSize, "best_huang_1_level0_input8_8_NN12_12_randomcrossoverwithnew_mutaterandom0_1_levelrand5.txt");
        es.outputFile= "best_huang_1_level0_input8_8_NN12_12_randomcrossoverwithnew_mutaterandom0_1_levelrand5.txt";
        es.cmdLineOptions.setPauseWorld(false);
        System.out.println("Evolving ");
        for (int gen = 0; gen < generations; gen++)
        {
            //if(gen%10 == 0)
        		//es.cmdLineOptions.setVisualization(true);
            es.nextGeneration();
            double bestResult = es.getBestFitnesses();
            System.out.println("Generation " + gen + " best " + bestResult);
            double secondResult = es.getNthFitness(1);
            System.out.println("Generation " + gen + " 2nd best " + secondResult);
        }
        es.cmdLineOptions.setVisualization(true);
        es.nextGeneration();
        double bestResult = es.getBestFitnesses();
        System.out.println("Generation " + generations + " best " + bestResult);
        double secondResult = es.getNthFitness(1);
        System.out.println("Generation " + generations + " 2nd best " + secondResult);
    }
}