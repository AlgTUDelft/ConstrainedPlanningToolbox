/*******************************************************************************
 * ConstrainedPlanningToolbox
 * Copyright (C) 2019 Algorithmics group, Delft University of Technology
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *******************************************************************************/
package algorithms.pomdp.cgcp;

import instances.CPOMDPInstance;
import instances.ConstraintType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import lp.LPSolver;
import model.CPOMDP;

import solutions.pomdp.POMDPAgentSolutionPolicyBased;
import solutions.pomdp.POMDPPolicy;
import solutions.pomdp.POMDPPolicySet;
import solutions.pomdp.CPOMDPSolution;
import solutions.pomdp.CPOMDPSolutionPolicyBased;
import util.ConfigFile;
import util.ConsoleOutput;

import algorithms.UnsupportedInstanceException;
import algorithms.pomdp.CPOMDPAlgorithm;


public class CGCP implements CPOMDPAlgorithm {
	private LPSolver lpSolver;
	
	private Random rnd;
	private FiniteVI cpomdpSolver;
	
	private CPOMDP[] cpomdps;
	private int nAgents;
	private double costLimit;
	private int numDecisions;
	
	private double objectiveUpperbound = Double.POSITIVE_INFINITY;
	private int numIterations = 0;
	private int noConsumptionAction = -1; // can be set to action without resource consumption, to initialize first policy
	
	private double lambdaTolerance;
	private double terminateTime = 10.0;
	private int minimumIncreaseRounds;
	private boolean useRuntimeIncrease;
	private double runtimeIncrease;
	
	// data structures to store policies
	private List<ArrayList<POMDPAgentSolutionPolicyBased>> solutions = null;
	
	public CGCP(LPSolver lpSolver, Random rnd) {
		this.rnd = rnd;
		this.lpSolver = lpSolver;
		
		this.lambdaTolerance = ConfigFile.getDoubleProperty("cgcp_convergence_tolerance");		
		this.minimumIncreaseRounds = ConfigFile.getIntProperty("cgcp_minimum_increase_rounds");
		this.useRuntimeIncrease = ConfigFile.getBooleanProperty("cgcp_use_runtime_increase");
		this.runtimeIncrease = ConfigFile.getDoubleProperty("cgcp_runtime_increase");
		this.terminateTime = ConfigFile.getDoubleProperty("cgcp_time_limit");
		double time_limit_subproblem_solver = ConfigFile.getDoubleProperty("cgcp_time_limit_subproblem_solver");
		
		FiniteVI fvi = new FiniteVI(rnd);
		fvi.setTerminateTime(time_limit_subproblem_solver);
		fvi.enableDumpPolicyGraph();
		this.cpomdpSolver = fvi;
	}
	
	public void setInstance(CPOMDPInstance instance) throws UnsupportedInstanceException {
		if(instance.getNumDomainResources() != 1) {
			throw new UnsupportedInstanceException();
		}
		
		if(instance.getConstraintType() != ConstraintType.BUDGET) {
			throw new UnsupportedInstanceException();
		}
		
		this.cpomdps = instance.getCPOMDPs();
		this.nAgents = cpomdps.length;
		this.costLimit = instance.getCostLimit(0);
		this.numDecisions = instance.getNumDecisions();
	}
	
	public CPOMDPSolution solve() {	
		double[][] costLimits = new double[1][1];
		costLimits[0][0] = costLimit;
		numIterations = 0;
		int policyID = 0;
		
		// compute initial column and initialize master LP and data structure for solutions
		MasterLP mlp = null;
		
		// initialize solutions datastructure
		solutions = new ArrayList<ArrayList<POMDPAgentSolutionPolicyBased>>();
		for(int i=0; i<nAgents; i++) {
			ArrayList<POMDPAgentSolutionPolicyBased> agentSolutions = new ArrayList<POMDPAgentSolutionPolicyBased>();
			solutions.add(agentSolutions);
		}
		assert solutions.size() == nAgents;
		
		if(noConsumptionAction != -1) {
			double[] initialExpectedReward = new double[nAgents];
			List<double[][]> initialExpectedCost = new ArrayList<double[][]>();
			
			for(int i=0; i<nAgents; i++) {
				POMDPAgentSolutionPolicyBased solution = cpomdpSolver.getNoConsumptionSolution(cpomdps[i], numDecisions, noConsumptionAction);
				
				initialExpectedReward[i] = solution.getExpectedReward();
				double[][] currCost = new double[1][1];
				currCost[0][0] = solution.getExpectedCost();
				initialExpectedCost.add(currCost);
				
				storeAgentSolution(solution, i, policyID);
			}
			assert initialExpectedCost.size() == nAgents;
			
			mlp = new MasterLP(lpSolver, nAgents, costLimits, initialExpectedReward, initialExpectedCost);
		}
		else {
			mlp = new MasterLP(lpSolver, nAgents, costLimits);
			
			for(int i=0; i<nAgents; i++) {
				storeAgentSolution(null, i, policyID);
			}
		}
		assert solutions.size() == nAgents;
		assert mlp != null;
		assert solutions != null;
		
		List<Double> lambdas = new ArrayList<Double>();
		
		double oldLambda = Double.POSITIVE_INFINITY;
		double currentLambda = Double.POSITIVE_INFINITY;
		double currentObjective = Double.NEGATIVE_INFINITY;
		int numObjectiveIncrease = 0;
		
		
		long startTime = System.currentTimeMillis();
		while(true) {			
			mlp.solve();
			ConsoleOutput.println("Objective: "+mlp.getExpectedReward());
			ConsoleOutput.println("Upper bound: "+objectiveUpperbound);
			ConsoleOutput.println("Expected cost: "+mlp.getExpectedCost(0,0));
			
			oldLambda = currentLambda;
			currentLambda = mlp.getLambda(0, 0);
			ConsoleOutput.println("Current lambda: "+currentLambda);
			lambdas.add(currentLambda);
			
			if(mlp.getExpectedReward() > currentObjective) numObjectiveIncrease++;
			currentObjective = mlp.getExpectedReward();
			
			double elapsedTime = (System.currentTimeMillis() - startTime) * 0.001;
			double gap = objectiveUpperbound-mlp.getExpectedReward();
			double allowedGap = Math.pow(10.0, Math.ceil(Math.log10(Math.max(objectiveUpperbound, mlp.getExpectedReward()))) - 3.0);
			
			ConsoleOutput.println("Gap: "+gap);
			ConsoleOutput.println("Gap allowed: "+allowedGap);
			
			assert gap >= -0.001 : "Gap must be positive";
			
			if(elapsedTime > terminateTime) {
				ConsoleOutput.println("Timelimit expired (elapsed: "+elapsedTime+", limit: "+terminateTime+")");
				// time limit expired
				break;
			}
			else if(objectiveUpperbound - mlp.getExpectedReward() < allowedGap) {
				// gap sufficiently small
				ConsoleOutput.println("Gap sufficiently small");
				
				/*
				 * Same as condition in GapMin:
				 * terminate if upper - lower < 10^(ceil(log10(max(abs(ubInitVal(i)),abs(lbInitVal(i)))))-3)
				 */
				
				break;
			}
			else if(numIterations > 1 && Math.abs(oldLambda - currentLambda) < lambdaTolerance) {
				// column generation converged
				ConsoleOutput.println("Lambda converged");
				
				if(useRuntimeIncrease && numObjectiveIncrease > minimumIncreaseRounds) {
					// lambda has converged, but bounds did not, so we increase subproblem time and continue
					
					ConsoleOutput.println("Old lambda: "+oldLambda+", current: "+currentLambda);
					ConsoleOutput.println("Lambda converged: increase runtime subproblem solver");
					cpomdpSolver.increaseRuntime(runtimeIncrease);
					oldLambda = Double.POSITIVE_INFINITY;
					currentLambda = Double.POSITIVE_INFINITY;
					numObjectiveIncrease = 0;
				}
				else {
					// lambda has converged, bounds did not, but we do not want to continue
					break;
				}
			}
			else {
				policyID++;
				numIterations++;
				
				double[] colExpectedReward = new double[nAgents];
				List<double[][]> colExpectedCost = new ArrayList<double[][]>();
				
				objectiveUpperbound = currentLambda * costLimit;
				
				for(int i=0; i<nAgents; i++) {
					POMDPAgentSolutionPolicyBased solution = cpomdpSolver.solve(cpomdps[i], numDecisions, currentLambda);
					colExpectedReward[i] = solution.getExpectedReward();
					objectiveUpperbound += solution.getExpectedValueUpperbound();
					
					double[][] currCost = new double[1][1];
					currCost[0][0] = solution.getExpectedCost();
					
					colExpectedCost.add(currCost);
					storeAgentSolution(solution, i, policyID);
					ConsoleOutput.println("Add policy "+policyID+" for agent "+i+": "+solution.getExpectedReward()+" "+solution.getExpectedCost());
					ConsoleOutput.println();
				}
				
				mlp.addColumns(colExpectedReward, colExpectedCost);	
			}
		}
		
		ConsoleOutput.println("Lambdas: "+lambdas);
		
		POMDPAgentSolutionPolicyBased[] retSolution = new POMDPAgentSolutionPolicyBased[nAgents];
		for(int i=0; i<nAgents; i++) {
			double[] agentDistribution = mlp.getPolicyDistribution(i);			
			POMDPAgentSolutionPolicyBased solution = getSolution(i, agentDistribution);
			retSolution[i] = solution;
		}

		return new CPOMDPSolutionPolicyBased(retSolution);
	}
	
	private POMDPAgentSolutionPolicyBased getSolution(int agent, double[] distribution) {		
		if(noConsumptionAction == -1) {
			assert distribution[0] < 0.00001 : "Dummy policy cannot be selected";
		}
		
		List<POMDPPolicy> policies = new ArrayList<POMDPPolicy>();
		List<Double> probabilities = new ArrayList<Double>();
		double expectedReward = 0.0;
		double expectedCost = 0.0;
		
		for(int p=0; p<distribution.length; p++) {			
			if(distribution[p] > 0.0) {
				POMDPAgentSolutionPolicyBased solution = getAgentSolution(agent, p);
				
				POMDPPolicy pv = solution.getPolicy();
				policies.add(pv);
				probabilities.add(distribution[p]);
				
				expectedReward += distribution[p] * solution.getExpectedReward();
				expectedCost += distribution[p] * solution.getExpectedCost();
			}
		}
		
		double[] probs = new double[probabilities.size()];
		for(int p=0; p<probabilities.size(); p++) {
			probs[p] = probabilities.get(p);
		}
		
		return new POMDPPolicySet(policies, probs, expectedReward, expectedReward, expectedCost, rnd);
	}
	
	public double getValueUpperBound() {
		return objectiveUpperbound;
	}
	
	public int getNumIterations() {
		return numIterations;
	}

	@Override
	public String getName() {
		return "ColGen ("+cpomdpSolver.getName()+")";
	}
	
	public void enableRuntimeIncrease(double t) {
		this.useRuntimeIncrease = true;
		this.runtimeIncrease = t;
	}
	
	public void setTerminateTime(double t) {
		this.terminateTime = t;
	}
	
	public void setNoConsumptionAction(int a) {
		this.noConsumptionAction = a;
	}
	
	private void storeAgentSolution(POMDPAgentSolutionPolicyBased solution, int agent, int policyID) {
		solutions.get(agent).add(solution);
	}
	
	private POMDPAgentSolutionPolicyBased getAgentSolution(int agent, int id) {
		return solutions.get(agent).get(id);
	}
	
}
