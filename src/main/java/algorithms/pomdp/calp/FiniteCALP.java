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
package algorithms.pomdp.calp;

import instances.CPOMDPInstance;
import instances.ConstraintType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import solutions.pomdp.POMDPAgentSolutionPolicyBased;
import solutions.pomdp.POMDPPolicyFSC;
import solutions.pomdp.CPOMDPSolution;
import solutions.pomdp.CPOMDPSolutionPolicyBased;
import util.ConfigFile;
import util.ConsoleOutput;

import lp.LPSolver;
import model.BeliefPoint;
import model.CPOMDP;

import algorithms.UnsupportedInstanceException;
import algorithms.pomdp.CPOMDPAlgorithm;


public class FiniteCALP implements CPOMDPAlgorithm {
	private CPOMDP[] cpomdps;
	private int nAgents;
	private double costLimit;
	private int nDecisions;
	
	private ApproximateLPFinite alp;
	
	private Random rnd;
	private LPSolver lpSolver;
	
	private double objectiveUpperbound;
	
	private int n;
	private int maxIter;
	private double timeLimit;
	private double beliefSearchTolerance;
	
	public FiniteCALP(LPSolver lpSolver, Random rnd) {
		this.rnd = rnd;
		this.lpSolver = lpSolver;
		
		this.n = ConfigFile.getIntProperty("calp_num_beliefs");
		this.maxIter = ConfigFile.getIntProperty("calp_max_iter");
		this.beliefSearchTolerance = ConfigFile.getDoubleProperty("calp_belief_search_tolerance");
		this.timeLimit = ConfigFile.getDoubleProperty("calp_time_limit");
	}
	
	public void setInstance(CPOMDPInstance instance) throws UnsupportedInstanceException {
		if(instance.getNumDomainResources() != 1 || instance.getCPOMDPs().length != 1) {
			throw new UnsupportedInstanceException();
		}
		
		if(instance.getConstraintType() != ConstraintType.BUDGET) {
			throw new UnsupportedInstanceException();
		}
		
		this.cpomdps = instance.getCPOMDPs();
		this.nAgents = cpomdps.length;
		this.costLimit = instance.getCostLimit(0);
		this.nDecisions = instance.getNumDecisions();
	}

	public CPOMDPSolution solve() {
		this.alp = new ApproximateLPFinite(cpomdps, nDecisions, costLimit, lpSolver);
		
		// create list with corner beliefs and initial beliefs (line 3)
		CPOMDP cpomdp = cpomdps[0];
		List<BeliefPoint> B = new ArrayList<BeliefPoint>();
		cpomdp.prepareBelief(cpomdp.getInitialBelief());
		B.add(cpomdp.getInitialBelief()); // initial belief must be added first!
		for(int s=0; s<cpomdp.getNumStates(); s++) {
			double[] newBelief = new double[cpomdp.getNumStates()];
			newBelief[s] = 1.0;
			BeliefPoint nbp = new BeliefPoint(newBelief);
			cpomdp.prepareBelief(nbp);
			B.add(nbp);
		}
		assert B.size() == cpomdp.getNumStates()+1;
		
		
		double exactExpectedReward = 0.0;
		double exactExpectedCost = 0.0;
		
		// start the loop (line 4)
		int iter = 0;
		long startTime = System.currentTimeMillis();
		while(true) {
			ConsoleOutput.println("Size of B: "+B.size());
			
			// solve approximate LP (line 5)
			alp.solve(B);
			double approximateExpectedReward = alp.getApproximateExpectedReward();
			double approximateExpectedCost = alp.getApproximateExpectedCost();
			exactExpectedReward = alp.getExactExpectedReward();
			exactExpectedCost = alp.getExactExpectedCost();
			
			double elapsedTime = (System.currentTimeMillis() - startTime) * 0.001;
			double gap = approximateExpectedReward-exactExpectedReward;
			double allowedGap = Math.pow(10.0, Math.ceil(Math.log10(Math.max(approximateExpectedReward, exactExpectedReward))) - 3.0);
			
			// print some statistics
			ConsoleOutput.println("R: "+exactExpectedReward+", R*: "+approximateExpectedReward+", gap: "+gap+", allowed: "+allowedGap+", elapsed: "+elapsedTime);
			ConsoleOutput.println("C: "+exactExpectedCost+", C*: "+approximateExpectedCost);
			ConsoleOutput.println();
			this.objectiveUpperbound = approximateExpectedReward;
			
			iter++;
			
			if(iter >= maxIter || elapsedTime > timeLimit || gap < allowedGap) {
				break;
			}
			
			List<BeliefPoint> newBeliefs = getNewBeliefs(cpomdps[0], B, alp.getPolicy());
			B.addAll(newBeliefs);
		}
		
		// perform binary search in case exact expected cost exceeds the limit
		if(exactExpectedCost > costLimit) {
			ConsoleOutput.println("Start binary search: cost is "+exactExpectedCost+" instead of "+costLimit+"!");
			alp.runBinarySearch();
			exactExpectedReward = alp.getExactExpectedReward();
			exactExpectedCost = alp.getExactExpectedCost();
			
			ConsoleOutput.println("R: "+exactExpectedReward+", R*: "+alp.getApproximateExpectedReward());
			ConsoleOutput.println("C: "+exactExpectedCost+", C*: "+alp.getApproximateExpectedCost());
			ConsoleOutput.println();
		}
		
		alp.dispose();
		
		// construct solution to return
		POMDPAgentSolutionPolicyBased[] retSolution = new POMDPAgentSolutionPolicyBased[nAgents];
		retSolution[0] = new POMDPPolicyFSC(B.size(), cpomdp.getNumActions(), cpomdp.getNumObservations(), alp.getPolicy(), alp.getWeights(), 0, alp.getExactExpectedReward(), alp.getExactExpectedReward(), alp.getExactExpectedCost(), rnd);
		
		return new CPOMDPSolutionPolicyBased(retSolution);
	}
	
	private List<BeliefPoint> getNewBeliefs(CPOMDP cpomdp, List<BeliefPoint> B, double[][][] policy) {
		List<BeliefPoint> newBeliefs = new ArrayList<BeliefPoint>();
		
		long t0 = System.currentTimeMillis();
		
		int nBeliefs = B.size();
		int nActions = cpomdp.getNumActions();
		int nObservations = cpomdp.getNumObservations();
		
		// add new beliefs
		for(int bIndex=0; bIndex<nBeliefs; bIndex++) {
			BeliefPoint b = B.get(bIndex);
			
			// enumerate reachable beliefs in the trivial way
			for(int a=0; a<nActions; a++) {
				if(!alp.isActionPolicyFeasible(bIndex, a)) {
					continue;
				}
				
				// action a may be executed in belief b
				
				for(int o=0; o<nObservations; o++) {
					if(b.getActionObservationProbability(a, o) > beliefSearchTolerance) {
						// observation o can be observed after executing action a in belief b
						
						BeliefPoint newB = cpomdp.updateBelief(b, a, o);
						
						ArrayList<BeliefPoint> tmpList = new ArrayList<BeliefPoint>();
						tmpList.add(newB);
						double dist = alp.getDistance(B, tmpList, 0);
						
						if(dist > 0.0) {
							newBeliefs.add(newB);
						}
						
						// compress the belief set
						if(newBeliefs.size() > n) {
							newBeliefs = compressBeliefSet(B, newBeliefs);
							assert newBeliefs.size() <= n : n+" "+newBeliefs.size();
						}
					}
				}
			}
			
			// done
		}
		
		// initialize action observation probabilities of the new belief points
		for(BeliefPoint b : newBeliefs) {
			cpomdp.prepareBelief(b);
		}
		
		long t1 = System.currentTimeMillis();
		ConsoleOutput.println("  Runtime belief search: "+((t1-t0)*0.001)+"s");
		
		return newBeliefs;
	}
	
	private List<BeliefPoint> compressBeliefSet(List<BeliefPoint> B, List<BeliefPoint> newBeliefs) {
		double minDistance = Double.POSITIVE_INFINITY;
		double candidate = -1;
		
		// find the belief with the smallest distance
		for(int i=0; i<newBeliefs.size(); i++) {
			double distance = alp.getDistance(B, newBeliefs, i);
			
			if(distance < minDistance) {
				minDistance = distance;
				candidate = i;
			}
		}
		assert candidate != -1;
		
		// create new belief set without the candidate we found
		List<BeliefPoint> compressedBeliefs = new ArrayList<BeliefPoint>();
		for(int i=0; i<newBeliefs.size(); i++) {
			if(i != candidate) {
				compressedBeliefs.add(newBeliefs.get(i));
			}
		}
		
		return compressedBeliefs;
	}

	public double getValueUpperBound() {
		return objectiveUpperbound;
	}

	public String getName() {
		return "FiniteCALP";
	}
	
}
