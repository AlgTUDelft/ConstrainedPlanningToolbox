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
package evaluation;

import instances.CMDPInstance;
import instances.ConstraintType;

import java.util.Random;

import solutions.mdp.CMDPSolution;
import util.ProbabilitySample;

import model.MDP;
import model.CMDP;

public class CMDPSimulator {
	private MDP[] mdps;
	private CMDPSolution solution;
	private int nAgents;
	private int numDecisions;
	private int numDomainResources;
	
	private CMDPInstance instance;
	
	private Random rnd;
	
	private boolean simulationDone = false;
	private double meanReward;
	private double[] meanTotalCost;
	private double[][] meanInstantaneousCost;
	private double[] violationProbEstimateTotal;
	private double[][] violationProbEstimateInstantaneous;
	
	public CMDPSimulator(CMDPInstance instance, CMDPSolution solution, Random rnd) {
		this.mdps = instance.getCMDPs();
		this.solution = solution;
		this.nAgents = instance.getCMDPs().length;
		this.rnd = rnd;
		this.numDomainResources = instance.getNumDomainResources();
		this.numDecisions = instance.getNumDecisions();
		this.instance = instance;
	}
	
	/**
	 * Run simulation
	 * @param runs number of runs
	 */
	public void run(int runs) {		
		meanReward = 0.0;
		meanTotalCost = new double[numDomainResources];
		meanInstantaneousCost = new double[numDomainResources][numDecisions];
		
		int[] numTotalViolations = new int[numDomainResources];
		int[][] numInstantaneousViolations = new int[numDomainResources][numDecisions];
		
		ProbabilitySample ps = new ProbabilitySample(rnd);
		
		for(int run=0; run<runs; run++) {
			double runReward = 0.0;
			double[] runTotalCost = new double[numDomainResources];
			double[][] runInstantaneousCost = new double[numDomainResources][numDecisions];
			
			// determine initial states
			int[] state = new int[nAgents];
			for(int i=0; i<nAgents; i++) {
				MDP mdp = mdps[i];
				state[i] = mdp.getInitialState();
			}
			
			for(int t=0; t<numDecisions; t++) {
				// select actions
				int[] actions = solution.getActions(t, state);
				
				// get reward and cost, sample next state
				for(int i=0; i<nAgents; i++) {
					MDP mdp = mdps[i];
					
					if(mdp instanceof CMDP) {
						runReward += mdp.getReward(t, state[i], actions[i]);
						
						for(int k=0; k<numDomainResources; k++) {
							runTotalCost[k] += ((CMDP)mdp).getCost(k, state[i], actions[i]);
							runInstantaneousCost[k][t] += ((CMDP)mdp).getCost(k, state[i], actions[i]);
						}
					}
					else {
						runReward += mdp.getReward(t, state[i], actions[i]);
					}
					
					// sample next state
					ps = new ProbabilitySample(rnd);
					int[] transitionDestinations = mdp.getTransitionDestinations(t, state[i], actions[i]);
					double[] transitionProbabilities = mdp.getTransitionProbabilities(t, state[i], actions[i]);
					
					for(int j=0; j<transitionDestinations.length; j++) {
						int s = transitionDestinations[j];
						double prob = transitionProbabilities[j];
						ps.addItem(s, prob);
					}
					int stateNext = ps.sampleItem();
					assert stateNext >= 0 && stateNext < mdp.getNumStates();
					state[i] = stateNext;
				}
			}
			
			meanReward += runReward / ((double) runs);
			for(int k=0; k<numDomainResources; k++) {
				meanTotalCost[k] += runTotalCost[k] / ((double) runs);
				
				if(instance.getConstraintType() == ConstraintType.BUDGET && runTotalCost[k] > instance.getCostLimit(k)) {
					numTotalViolations[k]++;
				}
				
				for(int t=0; t<numDecisions; t++) {
					meanInstantaneousCost[k][t] += runInstantaneousCost[k][t] / ((double) runs);
					
					if(instance.getConstraintType() == ConstraintType.INSTANTANEOUS && runInstantaneousCost[k][t] > instance.getCostLimit(k, t)) {
						numInstantaneousViolations[k][t]++;
					}
				}
			}
		}
		
		simulationDone = true;
		
		// compute violation probability estimates
		violationProbEstimateTotal = new double[numDomainResources];
		violationProbEstimateInstantaneous = new double[numDomainResources][numDecisions];
		for(int k=0; k<numDomainResources; k++) {
			violationProbEstimateTotal[k] = ((double) numTotalViolations[k]) / ((double) runs);
			
			for(int t=0; t<numDecisions; t++) {
				violationProbEstimateInstantaneous[k][t] = ((double) numInstantaneousViolations[k][t]) / ((double) runs);
			}
		}
	}
	
	/**
	 * Get total mean value, based on simulations starting from the initial state
	 * @return value
	 */
	public double getMeanReward() {
		assert simulationDone;
		return meanReward;
	}
	
	/**
	 * Get the mean cost obtained at time t
	 * @param t time t
	 * @return mean cost
	 */
	public double getMeanInstantaneousCost(int k, int t) {
		assert k>=0 && k<numDomainResources && t>=0 && t<numDecisions;
		assert simulationDone;
		return meanInstantaneousCost[k][t];
	}
	
	/**
	 * Get mean total cost for resource k
	 * @param k resource id
	 * @return mean total cost
	 */
	public double getMeanTotalCost(int k) {
		return meanTotalCost[k];
	}
	
	/**
	 * Get empirical estimate of violation probability of resource k
	 * @param k rsource id
	 * @return violation probability estimate
	 */
	public double getViolationProbabilityEstimateTotal(int k) {
		return violationProbEstimateTotal[k];
	}
	
	/**
	 * Get empirical estimate of violation probability of resource k at time t
	 * @param k resource id
	 * @param t time step
	 * @return violation probability estimate
	 */
	public double getViolationProbabilityEstimateInstantaneous(int k, int t) {
		return violationProbEstimateInstantaneous[k][t];
	}
	
}
