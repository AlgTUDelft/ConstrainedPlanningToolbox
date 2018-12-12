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

import instances.CPOMDPInstance;
import instances.ConstraintType;

import java.util.Random;

import model.BeliefPoint;
import model.CPOMDP;
import model.POMDP;

import solutions.pomdp.CPOMDPSolution;
import util.ProbabilitySample;


public class CPOMDPSimulator {
	private POMDP[] pomdps;
	private CPOMDPSolution solution;
	private int nAgents;
	private int nDecisions;
	private int nDomainResources;
	
	private CPOMDPInstance instance;
	
	private Random rnd;
	
	private boolean simulationDone = false;
	private double meanReward;
	private double[] meanTotalCost;
	private double[][] meanInstantaneousCost;
	private double[] violationProbEstimateTotal;
	
	public CPOMDPSimulator(CPOMDPInstance instance, CPOMDPSolution solutions, Random rnd) {
		this.pomdps = instance.getCPOMDPs();
		this.solution = solutions;
		this.nAgents = this.pomdps.length;
		this.rnd = rnd;
		this.nDecisions = instance.getNumDecisions();
		this.nDomainResources = instance.getNumDomainResources();
		this.instance = instance;
		
		assert pomdps != null;
		assert pomdps.length > 0;
		assert solutions != null;
	}
	
	/**
	 * Run simulation
	 * @param runs number of runs
	 */
	public void run(int runs) {
		meanReward = 0.0;
		meanTotalCost = new double[nDomainResources];
		meanInstantaneousCost = new double[nDomainResources][nDecisions];
		
		int[] numTotalViolations = new int[nDomainResources];
		
		for(int run=0; run<runs; run++) {
			double runReward = 0.0;
			double[] runTotalCost = new double[nDomainResources];
			double[][] runInstantaneousCost = new double[nDomainResources][nDecisions];
			
			
			int[] state = new int[nAgents];
			BeliefPoint[] beliefs = new BeliefPoint[nAgents];
			
			// sample initial states
			for(int i=0; i<nAgents; i++) {
				POMDP pomdp = pomdps[i];
				BeliefPoint b = pomdp.getInitialBelief();
				beliefs[i] = b;
				
				// sample an initial state
				ProbabilitySample ps = new ProbabilitySample(rnd);
				for(int s=0; s<pomdp.getNumStates(); s++) {
					ps.addItem(s, b.getBelief(s));
				}
				state[i] = ps.sampleItem();
			}
			
			for(int t=0; t<nDecisions; t++) {
				// select actions
				int[] actions = new int[nAgents];
				actions = solution.getActions(t, beliefs);
				
				int[] observations = new int[nAgents];
				
				// get reward and cost, sample observation and next state
				for(int i=0; i<nAgents; i++) {
					POMDP pomdp = pomdps[i];
					
					if(pomdp instanceof CPOMDP) {
						runReward += pomdp.getReward(state[i], actions[i]);
						
						for(int k=0; k<nDomainResources; k++) {
							runTotalCost[k] += ((CPOMDP)pomdp).getCost(k, state[i], actions[i]);
							runInstantaneousCost[k][t] += ((CPOMDP)pomdp).getCost(k, state[i], actions[i]);
						}
					}
					else {
						runReward += pomdp.getReward(state[i], actions[i]);
					}
					
					// sample next state
					ProbabilitySample ps = new ProbabilitySample(rnd);
					int[] transitionDestinations = pomdp.getTransitionDestinations(t, state[i], actions[i]);
					double[] transitionProbabilities = pomdp.getTransitionProbabilities(t, state[i], actions[i]);
					
					for(int j=0; j<transitionDestinations.length; j++) {
						int s = transitionDestinations[j];
						double prob = transitionProbabilities[j];
						ps.addItem(s, prob);
					}
					int stateNext = ps.sampleItem();
					assert stateNext >= 0 && stateNext < pomdp.getNumStates();
					
					// sample an observation
					ps = new ProbabilitySample(rnd);
					for(int o=0; o<pomdp.getNumObservations(); o++) {
						double probability = pomdp.getObservationProbability(actions[i], stateNext, o);
						ps.addItem(o, probability);
						
					}
					int observation = ps.sampleItem();
					assert observation >= 0 && observation < pomdp.getNumObservations();
					observations[i] = observation;
					
					// update the belief state
					BeliefPoint bao = pomdp.updateBelief(beliefs[i], actions[i], observation);
					
					// prepare for next step
					state[i] = stateNext;
					beliefs[i] = bao;
				}
				
				solution.update(actions, observations);
			}
			
			
			meanReward += runReward / ((double) runs);
			for(int k=0; k<nDomainResources; k++) {
				meanTotalCost[k] += runTotalCost[k] / ((double) runs);
				
				if(instance.getConstraintType() == ConstraintType.BUDGET && runTotalCost[k] > instance.getCostLimit(k)) {
					numTotalViolations[k]++;
				}
				
				for(int t=0; t<nDecisions; t++) {
					meanInstantaneousCost[k][t] += runInstantaneousCost[k][t] / ((double) runs);
				}
			}
		}
		
		
		simulationDone = true;
		
		// compute violation probability estimates
		violationProbEstimateTotal = new double[nDomainResources];
		for(int k=0; k<nDomainResources; k++) {
			violationProbEstimateTotal[k] = ((double) numTotalViolations[k]) / ((double) runs);
		}
	}
	
	/**
	 * Get total mean value, based on simulations starting from the initial belief
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
		assert k>=0 && k<nDomainResources && t>=0 && t<nDecisions;
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
	
}
