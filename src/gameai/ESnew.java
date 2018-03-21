package gameai;

import gameai.mariocontroller.MarioESNNController;
import gameai.neuralnetwork.Layer;

import java.util.ArrayList;
import java.util.List;
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
	public String outputFile = "best.txt";
	private MarioESNNController[] population;
	private float[] fitness;
	private int elite;
	public CmdLineOptions cmdLineOptions;
	private Random r;
	protected final Environment environment = MarioEnvironment.getInstance();
	protected int level = 5;
	public int levelRand = 5;
	public float ageCost = 1.5f;
	
	protected double mutationRate = 0.02;
	protected double preElitesFitness;
	
	public ESnew(int populationSize) {
		preElitesFitness = 0;
		// generate array of random NN
		this.population = new MarioESNNController[populationSize];
		for(int i = 0; i < population.length; i++) {
			population[i] = new MarioESNNController();
		}
		this.fitness = new float[populationSize];
		this.elite = populationSize/4;
		r = new Random(System.currentTimeMillis());
		String argsString = "-vis off -ld " + level + " -ag MarioESNNController";
		cmdLineOptions = new CmdLineOptions(argsString);
		//cmdLineOptions.setFPS(GlobalOptions.MaxFPS);
		// run the first time
		for(int i = 0; i < population.length; i++) {
			evaluateRandLevels(i, levelRand);
			//evaluate(i);
		}
		System.out.println("start");
		for(int i = 0; i < population.length; i++) {
			if(population[i].NN == null)
				System.out.println("bad" + i);
		}
		sortPopulationByFitness();
		updateMutationRateBasedOnElitesFit();
	}
	
	public ESnew(int populationSize, String filename) {
		preElitesFitness = 0;
		// generate array of random NN 
		this.population = new MarioESNNController[populationSize];
		this.fitness = new float[populationSize];
		this.elite = populationSize/4;
		
		population[0] = new MarioESNNController(filename);
		
		for(int i = 1; i < population.length; i++) {
			population[i] = new MarioESNNController();
		}
		
		r = new Random(System.currentTimeMillis());
		String argsString = "-vis off -ld " + level + " -ag MarioESNNController";
		cmdLineOptions = new CmdLineOptions(argsString);
		//cmdLineOptions.setFPS(GlobalOptions.MaxFPS);
		
		// run the first time
		for(int i = 0; i < population.length; i++) {
			evaluateRandLevels(i, levelRand);
			//evaluate(i);
		}
		System.out.println("start");
		for(int i = 0; i < population.length; i++) {
			if(population[i].NN == null)
				System.out.println("bad" + i);
		}
		sortPopulationByFitness();
		updateMutationRateBasedOnElitesFit();
	}
	
	public void nextGeneration() {
		// parents should be sorted
		// keep the best 1/3
		// mutate them as the second 1/3
		// recombine as the third 1/3
		// evaluate second and third 
		// sort
		for (int i=0; i<elite; i++) {
			population[i].age+=1;
			fitness[i] -= ageCost;
		}
		
		for(int i = 0; i < elite; i++) {
			population[elite+i] = new MarioESNNController(population[i]);
			population[elite+i].age = 0;
			mutate(population[elite+i]);
		}
		
		AddNewGuys();
		OrderRecombineWithNew();
		
		//this.cmdLineOptions.setLevelRandSeed(r.nextInt());
		
		// set true in the main function
		//cmdLineOptions.setLevelRandSeed(r.nextInt());
		boolean display = this.cmdLineOptions.isVisualization();
		cmdLineOptions.setVisualization(false);
		for(int i = elite; i < population.length; i++) {
			evaluateRandLevels(i, levelRand);
			//evaluate(i);
		}
		sortPopulationByFitness();
		updateMutationRateBasedOnElitesFit();
		if(display) {
			cmdLineOptions.setVisualization(true);
			cmdLineOptions.setLevelRandSeed(r.nextInt(levelRand));
			evaluateSingleLevel(population[0]);
			System.out.println("Fitness " + fitness[0]);
			evaluateSingleLevel(population[1]);
			System.out.println("Fitness " + fitness[1]);
			cmdLineOptions.setVisualization(false);
		}
	}
	
	// mutate the agent
	private void mutate(MarioESNNController agent) {
		double[] weights = agent.NN.getWeights().clone();
		double[] mutationValue = new double[weights.length];
		//for(double d : mutationValue) {
		//	d = r.nextGaussian()*0.2;
		//}
		for(int i = 0; i < weights.length; i++)
			mutationValue[i] = r.nextGaussian() * r.nextDouble() * mutationRate;
		for(int i = 0; i < weights.length; i++) {
			weights[i] += mutationValue[i];
		}
		agent.NN.loadWeights(weights);
	}
	
	// recombines the first 1/3 in order
	private void OrderRecombine() {
		List<Integer> indices = new ArrayList<Integer>(elite);
		for (int i=0; i<elite;i++) {
			indices.add(i);
		}
		int currentI = 0;
		while (!indices.isEmpty()) {
			int p1 = indices.remove((r.nextInt(indices.size())));
			if (indices.isEmpty()) {
				population[elite*2+currentI] = new MarioESNNController(population[p1]);
				break;
			}
			int p2 = indices.remove((r.nextInt(indices.size())));
			population[elite*2+currentI] = RandomCrossover(population[p1], population[p2]);
			population[elite*2+currentI].age = 0;
			currentI++;
			population[elite*2+currentI] = RandomCrossover(population[p2], population[p1]);
			population[elite*2+currentI].age = 0;
			currentI++;
		}
	}
	
	private void OrderRecombineWithNew() {
		List<Integer> indices = new ArrayList<Integer>(elite);
		for (int i=0; i<elite;i++) {
			indices.add(i);
		}
		int currentI = 0;
		while (!indices.isEmpty()) {
			int p1 = indices.remove((r.nextInt(indices.size())));
			population[elite*2+currentI] = RandomCrossover(population[p1], population[elite* 3 + p1]);
			population[elite*2+currentI].age = 0;
			currentI++;
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
	
	private MarioESNNController OnePointCrossover(MarioESNNController a1, MarioESNNController a2) {
		double[] weights1 = a1.NN.getWeights().clone();
		double[] weights2 = a2.NN.getWeights().clone();
		int cutPoint = r.nextInt(weights1.length);
		for (int i=0; i <= cutPoint; i++) {
			weights1[i] = weights2[i];
		}
		MarioESNNController child = new MarioESNNController(a1);
		child.NN.loadWeights(weights1);
		return child;
	}
	
	protected void updateMutationRateBasedOnElitesFit() {
		double temp = 0;
		for (int i=0; i<elite; i++) {
			temp += (fitness[i] + population[i].age * ageCost);
		}
		temp /= elite;
		
		
		double diff = Math.abs(temp - preElitesFitness);
		double s = Math.exp(-diff/100.0); // (0 .. 1]
		mutationRate = 0.5 * s;// (0 .. 2]
		System.out.println("mutation rate diff " + diff);
		System.out.println("mutation rate s " + s);
		System.out.println("mutation rate " + mutationRate);
		preElitesFitness = temp;
	}
	
	private void evaluate(int which) {
		// run one map? many map? difficulty?
		// collect score and progress
		this.cmdLineOptions.setAgent(population[which]);
		fitness[which] = evaluateSingleLevel();
	}
	
	private void evaluateRandLevels(int which, int rand) {
		// run one map? many map? difficulty?
		// collect score and progress
		this.cmdLineOptions.setAgent(population[which]);
		float f = 0;
		float fs[] = new float[rand];
		for (int i=0; i<rand; i++) {
			this.cmdLineOptions.setLevelRandSeed(i);
			 fs[i] = evaluateSingleLevel();
		}
		// Sort fs
		for (int i=0; i<fs.length; i++) {
			for (int j=i+1; j<fs.length; j++) {
				if (fs[i] > fs[j]) {
					float temp = fs[i];
					fs[i] = fs[j];
					fs[j] = temp;
				}
			}
		}
		
		/*
		f = fs[0];
		for (int i=1; i<fs.length; i++) {
			f =(f + fs[i]) * 0.5f;
		}
		*/
		for (int i=0; i<fs.length; i++) {
			f += fs[i];
		}
		fitness[which] = (f / fs.length);
	}
	
	private float evaluateSingleLevel()
	{
		Agent agent = cmdLineOptions.getAgent();
		float fitness = 0;
	    runOneEpisode(agent);
	    fitness += this.environment.getEvaluationInfo().computeDistancePassed();
	    //fitness -= (10.0 / this.environment.getEvaluationInfo().computeDistancePassed());
	    fitness += this.environment.getKillsTotal() * 100;
	    fitness -= (2-this.environment.getMarioMode())*10;
	    fitness += this.environment.getEvaluationInfo().computeWeightedFitness();
	    fitness -= this.environment.getEvaluationInfo().timeSpent * 8;
	    fitness -= (((MarioESNNController)agent).age * ageCost);
	    //System.out.println("age " + ((MarioESNNController)agent).age * 5);
	    if(cmdLineOptions.isVisualization())
	    	System.out.println("Total Fitness: " + fitness + 
	    		"=\nDistance Passed: " + this.environment.getEvaluationInfo().computeDistancePassed() + 
	    		"+\nKills: " + this.environment.getKillsTotal() * 100 + 
	    		"-\nHealth: " + (2-this.environment.getMarioMode())*10 + 
	    		"+\nWeightedTotal: " + this.environment.getEvaluationInfo().computeWeightedFitness() + 
	    	"-\nTimeSpend: " + this.environment.getEvaluationInfo().timeSpent * 8 + 
	    	"-\nAge: " + ((MarioESNNController)agent).age * ageCost);
	    return fitness;
	}
	
	private float evaluateSingleLevel(Agent agent)
	{
		//agent = cmdLineOptions.getAgent();
		float fitness = 0;
	    runOneEpisode(agent);
	    fitness += this.environment.getEvaluationInfo().computeDistancePassed();
	    //fitness -= (10.0 / this.environment.getEvaluationInfo().computeDistancePassed());
	    fitness += this.environment.getKillsTotal() * 100;
	    fitness -= (2-this.environment.getMarioMode())*10;
	    fitness += this.environment.getEvaluationInfo().computeWeightedFitness();
	    fitness -= this.environment.getEvaluationInfo().timeSpent * 8;
	    fitness -= ((MarioESNNController)agent).age * ageCost;
	    if(cmdLineOptions.isVisualization())
	    	System.out.println("Total Fitness: " + fitness + 
	    		"=\nDistance Passed: " + this.environment.getEvaluationInfo().computeDistancePassed() + 
	    		"+\nKills: " + this.environment.getKillsTotal() * 100 + 
	    		"-\nHealth: " + (2-this.environment.getMarioMode())*10 + 
	    		"+\nWeightedTotal: " + this.environment.getEvaluationInfo().computeWeightedFitness() + 
	    	"-\nTimeSpend: " + this.environment.getEvaluationInfo().timeSpent * 8 + 
	    	"-\nAge: " + ((MarioESNNController)agent).age * ageCost);
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
	
	public double getBestFitnesses() {
		System.out.println("Best age " + population[0].age);
		population[0].NN.writeWeightsToFile(outputFile);
		return fitness[0];
	}
	
	public double getNthFitness(int n) {
		System.out.println("Best " + n + " age " + population[n].age);
		population[n].NN.writeWeightsToFile(Integer.toString(n) + outputFile);
		return fitness[n];
	}
}