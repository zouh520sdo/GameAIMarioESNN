package gameai;

import gameai.mariocontroller.MarioESNNController;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

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
	protected int level = 0;
	
	// from huang
	public int levelRand = 5;
	public float ageCost = 5;
	
	private int MarioAirTime = 0;
	private int WallNotJumptime = 0;
	private int JumpTooLongTime = 0;
	
	// settings
	boolean newGuys = false;
	double mutationRate = 0.1;
	int recombineMethod = 0;
	
	public ESnew(int populationSize) {
		// generate array of random NN 
		this.population = new MarioESNNController[populationSize];
		for(int i = 0; i < population.length; i++) {
			population[i] = new MarioESNNController();
		}
		this.fitness = new float[populationSize];
		this.elite = populationSize/3;
		
		if(newGuys)
			this.elite = populationSize/4;
		
		r = new Random();
		
		
		// cmd line settings
		String argsString = "-vis off -ld " + level + " -ag MarioESNNController";
		cmdLineOptions = new CmdLineOptions(argsString);
		cmdLineOptions.setLevelDifficulty(25);
		cmdLineOptions.setFPS(GlobalOptions.MaxFPS/2);
		
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
		
		this.elite = populationSize/3;
		
		if(newGuys)
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
		//cmdLineOptions.setFPS(GlobalOptions.MaxFPS);
		
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
		//population[0].NN.writeWeightsToFile("starttest.txt");
	}
	
	public void nextGeneration() {
		// parents should be sorted
		// keep the best 1/3
		// mutate them as the second 1/3
		// recombine as the third 1/3
		// evaluate second and third 
		// sort
		
		// mutate
		for(int i = 0; i < elite; i++) {
			population[elite+i] = new MarioESNNController(population[i]);
			mutate(population[elite+i]);
		}
		
		// recombine
		if(recombineMethod == 0)
			OrderRecombine();
		else if (recombineMethod == 1)
			RandomRecombine();
		
		// new blood
		if(newGuys)
			AddNewGuys();
		
		if(recombineMethod == 2)
			RandomRecombineWithNew();
		
		if(this.cmdLineOptions.isVisualization()) {
			evaluate(0);
			cmdLineOptions.setVisualization(false);
		}
		
		// set true in the main function
		for(int i = elite; i < population.length; i++) {
			evaluate(i);
		}
		sortPopulationByFitness();
	}
	
	// mutate the agent
	private void mutate(MarioESNNController agent) {
		double[] weights = agent.NN.getWeights().clone();
		double[] mutationValue = new double[weights.length];
		
		for(int i = 0; i < weights.length; i++)
			mutationValue[i] = r.nextGaussian()*mutationRate;
		for(int i = 0; i < weights.length; i++) {
			weights[i] += mutationValue[i];
		}
		agent.NN.loadWeights(weights);
	}
	
	// recombines the first 1/3 in order
	private void OrderRecombine() {
		for(int i = 0; i < elite; i++) {
			if(i == elite-1)
				population[elite*2+i] = RandomCrossover(population[i], population[0]);
			population[elite*2+i] = RandomCrossover(population[i], population[i+1]);
		}
		/** two point crossover... or one
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
				//OnePointsCrossOver(population[elite * 2 + i], population[elite * 2 + i + 1]);
			}
		}
		**/
	}
	
	// random recombine
	private void RandomRecombine() {
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
			//population[elite*2+currentI].age = 0;
			currentI++;
			population[elite*2+currentI] = RandomCrossover(population[p2], population[p1]);
			//population[elite*2+currentI].age = 0;
			currentI++;
		}
	}
	
	private void RandomRecombineWithNew() {
		List<Integer> indices = new ArrayList<Integer>(elite);
		for (int i=0; i<elite;i++) {
			indices.add(i);
		}
		int currentI = 0;
		while (!indices.isEmpty()) {
			int p1 = indices.remove((r.nextInt(indices.size())));
			population[elite*2+currentI] = RandomCrossover(population[p1], population[elite* 3 + p1]);
			//population[elite*2+currentI].age = 0;
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
		float numOfMaps = 1;
		
		this.cmdLineOptions.setAgent(population[which]);
		float fitnessTotal = 0;
		for(int i = 0; i < numOfMaps; i++) {
			cmdLineOptions.setLevelRandSeed(i);
			float thisFitness = evaluateSingleLevel();
			if(this.cmdLineOptions.isVisualization())
				System.out.println("fitness" + " i: " + thisFitness);
			fitnessTotal += thisFitness;
		}
		fitness[which] = fitnessTotal / numOfMaps;
		if(this.cmdLineOptions.isVisualization())
			System.out.println("total fitness" + fitnessTotal);
	}
	
	private float evaluateSingleLevel()
	{
		Agent agent = cmdLineOptions.getAgent();
		float fitness = 0;
	    runOneEpisode(agent);
	    fitness += this.environment.getEvaluationInfo().computeDistancePassed()*100;
	    fitness += this.environment.getKillsTotal()*500;
	    fitness -= (2-this.environment.getMarioMode())*10;
	    fitness += MarioAirTime;
	    fitness -= WallNotJumptime;
	    fitness -= JumpTooLongTime;
	    //fitness += this.environment.getEvaluationInfo().computeWeightedFitness();
	    if(cmdLineOptions.isVisualization())
	    	System.out.println("Total Fitness: " + fitness + 
	    		" =\nDistance Passed: " + this.environment.getEvaluationInfo().computeDistancePassed() + 
	    		" +\nKills: " + this.environment.getKillsTotal()*500 + 
	    		" -\nHealth: " + (2-this.environment.getMarioMode())*10 + 
	    		" +\nWeightedTotal: " + this.environment.getEvaluationInfo().computeWeightedFitness() + 
	    		" +\nair time: " + this.MarioAirTime*2 + 
	    		" -\nwall not jump: " + this.WallNotJumptime + 
	    		" -\nJump Too Long: " + this.JumpTooLongTime);
	    MarioAirTime = 0;
	    WallNotJumptime = 0;
	    JumpTooLongTime = 0;
	    return fitness;
	}
	
	private float evaluateSingleLevel(Agent agent)
	{
		//agent = cmdLineOptions.getAgent();
		float fitness = 0;
	    runOneEpisode(agent);
	    fitness += this.environment.getEvaluationInfo().computeDistancePassed()*100;
	    fitness += this.environment.getKillsTotal()*500;
	    fitness -= (2-this.environment.getMarioMode())*10;
	    fitness += MarioAirTime;
	    fitness -= WallNotJumptime;
	    fitness -= JumpTooLongTime;
	    //fitness += this.environment.getEvaluationInfo().computeWeightedFitness();
	    if(cmdLineOptions.isVisualization())
	    	System.out.println("Total Fitness: " + fitness + 
	    		" =\nDistance Passed: " + this.environment.getEvaluationInfo().computeDistancePassed() + 
	    		" +\nKills: " + this.environment.getKillsTotal()*500 + 
	    		" -\nHealth: " + (2-this.environment.getMarioMode())*10 + 
	    		" +\nWeightedTotal: " + this.environment.getEvaluationInfo().computeWeightedFitness() + 
	    		" +\nair time: " + this.MarioAirTime*2 + 
	    		" -\nwall not jump: " + this.WallNotJumptime + 
	    		" -\nJump Too Long: " + this.JumpTooLongTime);
	    MarioAirTime = 0;
	    WallNotJumptime = 0;
	    JumpTooLongTime = 0;
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
	            
	            if(!((MarioESNNController) agent).getOnGround()) {
	            	
	            	int counter = ((MarioESNNController) agent).getJumpCounter();
	            	this.MarioAirTime += counter;
	            }
	            
	            if(((MarioESNNController) agent).FacingWallNotJump()) {
	            	this.WallNotJumptime++;
	            }
	            
	            if(((MarioESNNController) agent).jumpTooLong()==1) {
	            	this.JumpTooLongTime++;
	            }
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
		//System.out.println("Best age " + population[0].age);
		population[0].NN.writeWeightsToFile(outputFile);
		return fitness[0];
	}
	
	public double getBestFitnesses(int n) {
		if(n==0)
			population[0].NN.writeWeightsToFile(outputFile);
		return fitness[n];
	}
}