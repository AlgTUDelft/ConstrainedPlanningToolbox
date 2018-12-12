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
package algorithms.mdp.colgen;


import solutions.mdp.MDPAgentSolutionPolicyBased;
import solutions.mdp.MDPPolicyDeterministic;

import model.CMDP;

/**
 * Finite-horizon value iteration for MDPs, which keeps track of time-dependent value, reward and cost.
 */

public class ValueIterationFiniteHorizon {	
	
	public ValueIterationFiniteHorizon() {
		
	}
	
	/**
	 * Solve for budget constraints
	 * @param cmdp cmdp model
	 * @param T horizon
	 * @param lambda array with lambda for each resource
	 * @return solution
	 */
	public MDPAgentSolutionPolicyBased solve(CMDP cmdp, int T, double[] lambda) {
		int K = lambda.length;
		
		// compute the time-dependent value function and time-dependent policy
		double[][] Vvalue = new double[T][cmdp.getNumStates()];
		double[][] Vreward = new double[T][cmdp.getNumStates()];
		int[][] pi = new int[T][cmdp.getNumStates()];
		
		for(int t=T-1; t>=0; t--) {
			for(int s=0; s<cmdp.getNumStates(); s++) {
				double maxVal = Double.NEGATIVE_INFINITY;
				double maxReward = 0.0; // this is the total reward corresponding to the max value
				int maxAction = -1;
				
				for(int a : cmdp.getFeasibleActions(t, s)) {
					double currentVal = 0.0;
					double currentReward = 0.0;
					
					int[] transitionDestinations = cmdp.getTransitionDestinations(t, s, a);
					double[] transitionProbabilities = cmdp.getTransitionProbabilities(t, s, a);
					
					for(int j=0; j<transitionDestinations.length; j++) {
						int sNext = transitionDestinations[j];
						double prob = transitionProbabilities[j];
						
						if(t == T-1) {
							double val = cmdp.getReward(t, s, a);
							for(int k=0; k<K; k++) {
								val += -1.0 * lambda[k] * cmdp.getCost(k, s, a);
							}
							
							currentVal += prob * val;
							currentReward += prob * cmdp.getReward(t, s, a);
						}
						else {
							double val = cmdp.getReward(t, s, a);
							for(int k=0; k<K; k++) {
								val += -1.0 * lambda[k] * cmdp.getCost(k, s, a);
							}
							
							currentVal += prob * (val + Vvalue[t+1][sNext]);
							currentReward += prob * (cmdp.getReward(t, s, a) + Vreward[t+1][sNext]);
						}
					}
					
					if(currentVal > maxVal) {
						maxVal = currentVal;
						maxReward = currentReward;
						maxAction = a;
					}
				}
				
				Vvalue[t][s] = maxVal;
				Vreward[t][s] = maxReward;
				pi[t][s] = maxAction;
			}
		}
		
		// compute time-dependent expected instantaneous reward and cost using a forward pass in the tree		
		double[][] stateProbabilities = new double[T][cmdp.getNumStates()];
		double[] expectedInstantaneousReward = new double[T];
		double[][] expectedInstantaneousCost = new double[cmdp.getNumCostFunctions()][T];
		double[] expectedTotalCost = new double[cmdp.getNumCostFunctions()];
		stateProbabilities[0][cmdp.getInitialState()] = 1.0;
		for(int t=0; t<T; t++) {
			for(int s=0; s<cmdp.getNumStates(); s++) {
				int a = pi[t][s];
				
				// compute instantaneous reward and cost
				expectedInstantaneousReward[t] += stateProbabilities[t][s] * cmdp.getReward(t, s, a);
				for(int k=0; k<cmdp.getNumCostFunctions(); k++) {
					expectedInstantaneousCost[k][t] += stateProbabilities[t][s] * cmdp.getCost(k, s, a);
					expectedTotalCost[k] += stateProbabilities[t][s] * cmdp.getCost(k, s, a);
				}
				
				// compute probabilities for the next iteration
				if(t<T-1) {					
					int[] transitionDestinations = cmdp.getTransitionDestinations(t, s, a);
					double[] transitionProbabilities = cmdp.getTransitionProbabilities(t, s, a);
					
					for(int j=0; j<transitionDestinations.length; j++) {
						int sNext = transitionDestinations[j];
						double prob = transitionProbabilities[j];
						stateProbabilities[t+1][sNext] += stateProbabilities[t][s] * prob;
					}
					
					for(int sNext=0; sNext<cmdp.getNumStates(); sNext++) {
						if(stateProbabilities[t+1][sNext] < 0.0) stateProbabilities[t+1][sNext] = 0.0;
						if(stateProbabilities[t+1][sNext] > 1.0) stateProbabilities[t+1][sNext] = 1.0;
						assert stateProbabilities[t+1][sNext] >= 0.0 && stateProbabilities[t+1][sNext] <= 1.0 : stateProbabilities[t+1][sNext]+"";
					}
				}
			}
		}
		
		return new MDPPolicyDeterministic(pi, Vreward[0][cmdp.getInitialState()], expectedInstantaneousCost, expectedTotalCost);
	}
	
	/**
	 * Solve for instantaneous constraints
	 * @param cmdp cmdp model
	 * @param T horizon
	 * @param lambda lambda for each resource-time combination
	 * @return solution
	 */
	public MDPAgentSolutionPolicyBased solve(CMDP cmdp, int T, double[][] lambda) {
		assert lambda[0].length == T;
		int K = lambda.length;
		
		// compute the time-dependent value function and time-dependent policy
		double[][] Vvalue = new double[T][cmdp.getNumStates()];
		double[][] Vreward = new double[T][cmdp.getNumStates()];
		int[][] pi = new int[T][cmdp.getNumStates()];
		
		for(int t=T-1; t>=0; t--) {
			for(int s=0; s<cmdp.getNumStates(); s++) {
				double maxVal = Double.NEGATIVE_INFINITY;
				double maxReward = 0.0; // this is the total reward corresponding to the max value
				int maxAction = -1;
				
				for(int a : cmdp.getFeasibleActions(t, s)) {
					double currentVal = 0.0;
					double currentReward = 0.0;
					
					int[] transitionDestinations = cmdp.getTransitionDestinations(t, s, a);
					double[] transitionProbabilities = cmdp.getTransitionProbabilities(t, s, a);
					
					for(int j=0; j<transitionDestinations.length; j++) {
						int sNext = transitionDestinations[j];
						double prob = transitionProbabilities[j];
						
						if(t == T-1) {
							double val = cmdp.getReward(t, s, a);
							for(int k=0; k<K; k++) {
								val += -1.0 * lambda[k][t] * cmdp.getCost(k, s, a);
							}
							
							currentVal += prob * val;
							currentReward += prob * cmdp.getReward(t, s, a);
						}
						else {
							double val = cmdp.getReward(t, s, a);
							for(int k=0; k<K; k++) {
								val += -1.0 * lambda[k][t] * cmdp.getCost(k, s, a);
							}
							
							currentVal += prob * (val + Vvalue[t+1][sNext]);
							currentReward += prob * (cmdp.getReward(t, s, a) + Vreward[t+1][sNext]);
						}
					}
					
					if(currentVal > maxVal) {
						maxVal = currentVal;
						maxReward = currentReward;
						maxAction = a;
					}
				}
				
				Vvalue[t][s] = maxVal;
				Vreward[t][s] = maxReward;
				pi[t][s] = maxAction;
			}
		}
		
		// compute time-dependent expected instantaneous reward and cost using a forward pass in the tree		
		double[][] stateProbabilities = new double[T][cmdp.getNumStates()];
		double[] expectedInstantaneousReward = new double[T];
		double[][] expectedInstantaneousCost = new double[cmdp.getNumCostFunctions()][T];
		double[] expectedTotalCost = new double[cmdp.getNumCostFunctions()];
		stateProbabilities[0][cmdp.getInitialState()] = 1.0;
		for(int t=0; t<T; t++) {
			for(int s=0; s<cmdp.getNumStates(); s++) {
				int a = pi[t][s];
				
				// compute instantaneous reward and cost
				expectedInstantaneousReward[t] += stateProbabilities[t][s] * cmdp.getReward(t, s, a);
				for(int k=0; k<cmdp.getNumCostFunctions(); k++) {
					expectedInstantaneousCost[k][t] += stateProbabilities[t][s] * cmdp.getCost(k, s, a);
					expectedTotalCost[k] += stateProbabilities[t][s] * cmdp.getCost(k, s, a);
				}
				
				// compute probabilities for the next iteration
				if(t<T-1) {					
					int[] transitionDestinations = cmdp.getTransitionDestinations(t, s, a);
					double[] transitionProbabilities = cmdp.getTransitionProbabilities(t, s, a);
					
					for(int j=0; j<transitionDestinations.length; j++) {
						int sNext = transitionDestinations[j];
						double prob = transitionProbabilities[j];
						stateProbabilities[t+1][sNext] += stateProbabilities[t][s] * prob;
					}
					
					for(int sNext=0; sNext<cmdp.getNumStates(); sNext++) {
						if(stateProbabilities[t+1][sNext] < 0.0) stateProbabilities[t+1][sNext] = 0.0;
						if(stateProbabilities[t+1][sNext] > 1.0) stateProbabilities[t+1][sNext] = 1.0;
						assert stateProbabilities[t+1][sNext] >= 0.0 && stateProbabilities[t+1][sNext] <= 1.0 : stateProbabilities[t+1][sNext]+"";
					}
				}
			}
		}
		
		return new MDPPolicyDeterministic(pi, Vreward[0][cmdp.getInitialState()], expectedInstantaneousCost, expectedTotalCost);
	}
}
