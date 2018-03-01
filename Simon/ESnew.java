package gameai;

import gameai.mariocontroller.MarioESNNController;
import java.util.Random;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.mario.environments.MarioEnvironment;
import ch.idsia.tools.CmdLineOptions;
import gameai.mariocontroller.*;

public class ESnew {

	// manage an array of NN
	// evaluate parent
	//*use numbers divisible by 3
	// choose 1/3 elite
	// 1/3 for mutation & 1/3 for recombination
	public String outputFile = "2point_0.txt";
	private MarioESNNController[] population;
	private float[] fitness;
	private int elite;
	public CmdLineOptions cmdLineOptions;
	private Random r;
	protected final Environment environment = MarioEnvironment.getInstance();
	protected int level = 0;
	
	public ESnew(int populationSize) {
		// generate array of random NN 
		this.population = new MarioESNNController[populationSize];
		for(int i = 0; i < population.length; i++) {
			population[i] = new MarioESNNController();
		}
		this.fitness = new float[populationSize];
		this.elite = populationSize/4;
		r = new Random();
		String argsString = "-vis off -ld " + level + " -ag MarioESNNController";
		cmdLineOptions = new CmdLineOptions(argsString);
		cmdLineOptions.setFPS(GlobalOptions.MaxFPS);
		// run the first time
		for(int i = 0; i < population.length; i++) {
			evaluate(i);
		}
		System.out.println("start");
		for(int i = 0; i < population.length; i++) {
			if(population[i].NN == null)
				System.out.println("bad" + i);
		}
		sortPopulationByFitness();
	}
	
	public ESnew(int populationSize, String filename) {
		// generate array of random NN 
		this.population = new MarioESNNController[populationSize];
		this.fitness = new float[populationSize];
		this.elite = populationSize/4;
		for(int i = 0; i < elite; i++) {
			population[i] = new MarioESNNController(filename);
		}
		
		for(int i = elite; i < population.length; i++) {
			population[i] = new MarioESNNController();
		}
		
		r = new Random();
		String argsString = "-vis off -ld " + level + " -ag MarioESNNController";
		cmdLineOptions = new CmdLineOptions(argsString);
		cmdLineOptions.setFPS(GlobalOptions.MaxFPS);
		
		// run the first time
		for(int i = 0; i < population.length; i++) {
			evaluate(i);
		}
		System.out.println("start");
		for(int i = 0; i < population.length; i++) {
			if(population[i].NN == null)
				System.out.println("bad" + i);
		}
		sortPopulationByFitness();
		population[0].NN.writeWeightsToFile("starttest.txt");
	}
	
	public void nextGeneration() {
		// parents should be sorted
		// keep the best 1/3
		// mutate them as the second 1/3
		// recombine as the third 1/3
		// evaluate second and third 
		// sort
		for(int i = 0; i < elite; i++) {
			population[elite+i] = new MarioESNNController(population[i]);
			mutate(population[elite+i]);
		}
		OrderRecombine();
		AddNewGuys();
		sortPopulationByFitness();
		if(this.cmdLineOptions.isVisualization()) {
			System.out.println("Fitness " + evaluateSingleLevel(population[0]));
		}
		// set true in the main function
		cmdLineOptions.setVisualization(false);
		for(int i = elite; i < population.length; i++) {
			evaluate(i);
		}
	}
	
	// mutate the agent
	private void mutate(MarioESNNController agent) {
		double[] weights = agent.NN.getWeights();
		double[] mutationValue = new double[weights.length];
		//for(double d : mutationValue) {
		//	d = r.nextGaussian()*0.2;
		//}
		for(int i = elite; i < elite * 2; i++)
		{
			mutationValue[i] = 0.3 * r.nextGaussian();
			/*
			if(mutationValue[i] > 1)
			{
				mutationValue[i] = 1;
			}
			if(mutationValue[i] < -1)
			{
				mutationValue[i] = -1;
			}
			*/
		}
		for(int i = 0; i < weights.length; i++) {
			weights[i] += mutationValue[i];
		}
		agent.NN.loadWeights(weights);
	}
	
	// recombines the first 1/3 in order
	private void OrderRecombine() {
		/*
		for(int i = 0; i < elite; i++) {
			if(i == elite-1)
				population[elite*2+i] = RandomCrossover(population[i], population[0]);
			population[elite*2+i] = RandomCrossover(population[i], population[i+1]);
		}
		*/
		for(int i = 0; i < elite; i++)
		{
			if(i == elite - 1)
			{
				population[elite * 2 + i] = population[i];
			}
			else
			{
				population[elite * 2 + i] = population[i];
				population[elite * 2 + i + 1] = population[i+1];
				TwoPointsCrossOver(population[elite * 2 + i], population[elite * 2 + i + 1]);
			}
		}
	}
	
	private void AddNewGuys() {
		for (int i=0; i< elite; i++) {
			population[elite*3+i] = new MarioESNNController();
		}
	}
	
	// take in two NN and randomly cross over
	private MarioESNNController RandomCrossover(MarioESNNController a1, MarioESNNController a2) {
		double[] weights1 = a1.NN.getWeights().clone();
		double[] weights2 = a2.NN.getWeights().clone();
		for(int i = 0; i < weights1.length; i++) {
			int randint = r.nextInt(2);
			if (randint == 0)
				weights1[i] = weights2[i];
		}
		MarioESNNController child = new MarioESNNController(a1);
		child.NN.loadWeights(weights1);
		return child;
	}
	
	//modify passed in clones into children
	private void TwoPointsCrossOver(MarioESNNController a1, MarioESNNController a2)
	{
		double[] weights1 = a1.NN.getWeights();
		double[] weights2 = a2.NN.getWeights();
		
		int r1 = r.nextInt(weights1.length);
		int r2 = r.nextInt(weights2.length);
		
		if(r1 > r2)
		{
			int tmp = r2;
			r2 = r1;
			r1 = tmp;
		}
		
		for(int i = r1; i < r2; i++)
		{
			double tmp = weights1[i];
			weights1[i] = weights2[i];
			weights2[i] = tmp;
		}
		
		a1.NN.loadWeights(weights1);
		a2.NN.loadWeights(weights2);
		
	}
	
	private void evaluate(int which) {
		// run one map? many map? difficulty?
		// collect score and progress
		this.cmdLineOptions.setAgent(population[which]);
		fitness[which] = evaluateSingleLevel();
	}
	
	private float evaluateSingleLevel()
	{
		Agent agent = cmdLineOptions.getAgent();
		float fitness = 0;
	    runOneEpisode(agent);
	    fitness += 1.5 * this.environment.getEvaluationInfo().computeDistancePassed();
	    //fitness -= (10.0 / this.environment.getEvaluationInfo().computeDistancePassed());
	    fitness += this.environment.getKillsTotal();
	    //fitness += this.environment.getKillsByFire();
	    fitness -= (2-this.environment.getMarioMode())*10;
	    fitness += this.environment.getEvaluationInfo().computeWeightedFitness();
	    fitness -= this.environment.getEvaluationInfo().timeSpent * 5;
	    
	    /*
	    if(cmdLineOptions.isVisualization())
	    	System.out.println("Total Fitness: " + fitness + 
	    		"=\nDistance Passed: " + this.environment.getEvaluationInfo().computeDistancePassed() + 
	    		"+\nKills: " + this.environment.getKillsTotal() + 
	    		"-\nHealth: " + (2-this.environment.getMarioMode())*10 + 
	    		"+\nWeightedTotal: " + this.environment.getEvaluationInfo().computeWeightedFitness());
	    */
	    
	    return fitness;
	}
	
	private float evaluateSingleLevel(Agent agent)
	{
		//agent = cmdLineOptions.getAgent();
		float fitness = 0;
	    runOneEpisode(agent);
	    fitness += 1.5 * this.environment.getEvaluationInfo().computeDistancePassed();
	    //fitness -= (10.0 / this.environment.getEvaluationInfo().computeDistancePassed());
	    fitness += this.environment.getKillsTotal();
	    //fitness += this.environment.getKillsByFire();
	    fitness -= (2-this.environment.getMarioMode())*10;
	    fitness += this.environment.getEvaluationInfo().computeWeightedFitness();
	    fitness -= this.environment.getEvaluationInfo().timeSpent * 5;
	    
	    
	    /*
	    if(cmdLineOptions.isVisualization())
	    	System.out.println("Total Fitness: " + fitness + 
	    		"=\nDistance Passed: " + this.environment.getEvaluationInfo().computeDistancePassed() + 
	    		"+\nKills: " + this.environment.getKillsTotal() + 
	    		"-\nHealth: " + (2-this.environment.getMarioMode())*10 + 
	    		"+\nWeightedTotal: " + this.environment.getEvaluationInfo().computeWeightedFitness());
	    */
	    
	    return fitness;
	}
	
	public boolean runOneEpisode(Agent agent)
	{
	    this.environment.reset(cmdLineOptions);
	    while (!environment.isLevelFinished())
	    {
	        environment.tick();
	        if (!GlobalOptions.isGameplayStopped)
	        {
	            agent.integrateObservation(environment);
	            agent.giveIntermediateReward(environment.getIntermediateReward());

	            boolean[] action = agent.getAction();
	            environment.performAction(action);
	        }
	    }
	    environment.closeRecorder();
	    environment.getEvaluationInfo().setTaskName("MyTask");
	    return true;
	}
	
	public void setCmdOption(String newLine) {
		cmdLineOptions = new CmdLineOptions(newLine);
	}
	
	// higher >> lower
	private void sortPopulationByFitness()
    {
        for (int i = 0; i < population.length; i++)
        {
            for (int j = i + 1; j < population.length; j++)
            {
                if (fitness[i] < fitness[j])
                {
                    swap(i, j);
                }
            }
        }
    }
	
	// swap both fitness and population
	private void swap(int i, int j)
    {
        float cache = fitness[i];
        fitness[i] = fitness[j];
        fitness[j] = cache;
        MarioESNNController gcache = population[i];
        population[i] = population[j];
        population[j] = gcache;
    }
	
	public double getBestFitnesses(int n) {
		population[0].NN.writeWeightsToFile(outputFile);
		return fitness[n];
	}
}