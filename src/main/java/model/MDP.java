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
package model;


public class MDP {
	private int nStates;
	private int nActions;
	private int initialState;
	private int nDecisions;
	
	private int[][][] feasibleActions;
	
	private boolean rewardsDefined = false;
	private boolean hasTimeDependentReward = false;
	private double[][] rewardFunction;
	private double[][][] timeRewardFunction;
	private double minReward = Double.POSITIVE_INFINITY;
	private double maxReward = Double.NEGATIVE_INFINITY;
	
	private boolean transitionsDefined = false;
	private boolean hasTimeDependentTransitions = false;
	private int[][][] transitionDestinations;
	private double[][][] transitionProbabilities;
	private int[][][][] timeTransitionDestinations;
	private double[][][][] timeTransitionProbabilities;
	
	
	public MDP(int nStates, int nActions, int initialState, int nDecisions) {
		this.nStates = nStates;
		this.nActions = nActions;
		this.initialState = initialState;
		this.nDecisions = nDecisions;
		initDefaultFeasibleActions();
	}
	
	
	/**
	 * Get number of states
	 * @return number of states
	 */
	public int getNumStates() {
		return nStates;
	}
	
	/**
	 * Get number of actions
	 * @return number of actions
	 */
	public int getNumActions() {
		return nActions;
	}
	
	/**
	 * Get number of decisions
	 * @return number of decisions
	 */
	public int getNumDecisions() {
		return nDecisions;
	}
	
	/**
	 * Get initial state
	 * @return initial state
	 */
	public int getInitialState() {
		return initialState;
	}
	
	/**
	 * Set reward function
	 * @param rewardFunction reward function
	 */
	public void setRewardFunction(double[][] rewardFunction) {
		this.rewardFunction = rewardFunction;
		this.hasTimeDependentReward = false;
		this.rewardsDefined = true;
		
		minReward = Double.POSITIVE_INFINITY;
		maxReward = Double.NEGATIVE_INFINITY;
		
		for(int s=0; s<nStates; s++) {
			for(int a=0; a<nActions; a++) {
				minReward = Math.min(minReward, rewardFunction[s][a]);
				maxReward = Math.max(maxReward, rewardFunction[s][a]);
			}
		}
	}
	
	/**
	 * Set time-dependent reward function
	 * @param rewardFunction reward function
	 */
	public void setRewardFunction(double[][][] timeRewardFunction) {
		assert timeRewardFunction.length == nDecisions;
		this.timeRewardFunction = timeRewardFunction;
		this.hasTimeDependentReward = true;
		this.rewardsDefined = true;
		
		minReward = Double.POSITIVE_INFINITY;
		maxReward = Double.NEGATIVE_INFINITY;
		
		for(int t=0; t<nDecisions; t++) {
			for(int s=0; s<nStates; s++) {
				for(int a=0; a<nActions; a++) {
					minReward = Math.min(minReward, timeRewardFunction[t][s][a]);
					maxReward = Math.max(maxReward, timeRewardFunction[t][s][a]);
				}
			}
		}
	}
	
	/**
	 * Get reward function
	 * @return reward function
	 */
	public double[][] getRewardFunction() {
		assert rewardsDefined;
		return rewardFunction;
	}
	
	/** 
	 * Get time-dependent reward function
	 * @return time-dependent reward function
	 */
	public double[][][] getTimeRewardFunction() {
		assert rewardsDefined;
		return timeRewardFunction;
	}
	
	/**
	 * Get reward R(s,a)
	 * @param s state s
	 * @param a action a
	 * @return reward R(s,a)
	 */
	public double getReward(int s, int a) {
		assert s<nStates && a<nActions && !hasTimeDependentReward && rewardsDefined;
		return rewardFunction[s][a];
	}
	
	/**
	 * Get reward R(s,a) at time t
	 * @param t time t
	 * @param s state s
	 * @param a action a
	 * @return reward R(s,a) at time t
	 */
	public double getReward(int t, int s, int a) {
		assert rewardsDefined;
		
		if(hasTimeDependentReward) {
			assert s<nStates && a<nActions && t<nDecisions;
			return timeRewardFunction[t][s][a];
		}
		else {
			assert s<nStates && a<nActions;
			return rewardFunction[s][a];
		}
	}
	
	/**
	 * Get minimum instantaneous reward
	 * @return min reward
	 */
	public double getMinReward() {
		assert rewardsDefined;
		return minReward;
	}
	
	/**
	 * Get maximum instantaneous reward
	 * @return max reward
	 */
	public double getMaxReward() {
		assert rewardsDefined;
		return maxReward;
	}
	
	/**
	 * Returns true if MDP has time dependent rewards
	 * @return true if MDP has time dependent rewards, false otherwise
	 */
	public boolean hasTimeDependentReward() {
		return hasTimeDependentReward;
	}
	
	/**
	 * Returns true if MDP has time dependent transitions
	 * @return true if MDP has time dependent transitions, false otherwise
	 */
	public boolean hasTimeDependentTransitions() {
		return hasTimeDependentTransitions;
	}
	
	/**
	 * Set transition function
	 * @param transitionDestinations array containing destination states
	 * @param transitionProbabilities array containing probabilities for destinations
	 */
	public void setTransitionFunction(int[][][] transitionDestinations, double[][][] transitionProbabilities) {
		this.hasTimeDependentTransitions = false;
		this.transitionsDefined = true;
		this.transitionDestinations = transitionDestinations;
		this.transitionProbabilities = transitionProbabilities;
		this.timeTransitionDestinations = null;
		this.timeTransitionProbabilities = null;
	}
	
	/**
	 * Set time-dependent transition function
	 * @param transitionDestinations array containing destination states
	 * @param transitionProbabilities array containing probabilities for destinations
	 */
	public void setTransitionFunction(int[][][][] transitionDestinations, double[][][][] transitionProbabilities) {
		this.hasTimeDependentTransitions = true;
		this.transitionsDefined = true;
		this.transitionDestinations = null;
		this.transitionProbabilities = null;
		this.timeTransitionDestinations = transitionDestinations;
		this.timeTransitionProbabilities = transitionProbabilities;
	}
	
	/**
	 * Get reachable states when executing action a in state s
	 * @param s state
	 * @param a action
	 * @return reachable states
	 */
	public int[] getTransitionDestinations(int s, int a) {
		assert s<nStates && a<nActions && transitionsDefined;
		assert !hasTimeDependentTransitions;
		return transitionDestinations[s][a];
	}
	
	/**
	 * Get transition probabilities for states reachable when executing action a in state s
	 * @param s state
	 * @param a action
	 * @return transition probabilities
	 */
	public double[] getTransitionProbabilities(int s, int a) {
		assert s<nStates && a<nActions && transitionsDefined;
		assert !hasTimeDependentTransitions;
		return transitionProbabilities[s][a];
	} 

	/**
	 * Get reachable states when executing action a in state s at time t
	 * @param t time
	 * @param s state
	 * @param a action
	 * @return reachable states
	 */
	public int[] getTransitionDestinations(int t, int s, int a) {
		assert transitionsDefined;
		
		if(hasTimeDependentTransitions) {
			assert s<nStates && a<nActions && t<nDecisions;
			return timeTransitionDestinations[t][s][a];
		}
		else {
			assert s<nStates && a<nActions;
			return transitionDestinations[s][a];
		}		
	}
	
	/**
	 * Get transition probabilities for states reachable when executing action a in state s at time t
	 * @param t time
	 * @param s state
	 * @param a action
	 * @return transition probabilities
	 */
	public double[] getTransitionProbabilities(int t, int s, int a) {
		assert transitionsDefined;
		
		if(hasTimeDependentTransitions) {
			assert s<nStates && a<nActions && t<nDecisions;
			return timeTransitionProbabilities[t][s][a];
		}
		else {
			assert s<nStates && a<nActions;
			return transitionProbabilities[s][a];
		}		
	}
	
	/**
	 * Get transition destinations array
	 * @return transition destinations array
	 */
	public int[][][] getTransitionDestinations() {
		assert !hasTimeDependentTransitions && transitionsDefined;
		return transitionDestinations;
	}
	
	/**
	 * Get transition probabilities array
	 * @return transition probabilities array
	 */
	public double[][][] getTransitionProbabilities() {
		assert !hasTimeDependentTransitions && transitionsDefined;
		return transitionProbabilities;
	}
	
	/**
	 * Get time transition destinations array
	 * @return time transition destinations
	 */
	public int[][][][] getTimeTransitionDestinations() {
		assert hasTimeDependentTransitions && transitionsDefined;
		return timeTransitionDestinations;
	}
	
	/**
	 * Get time transition probabilities array
	 * @return time transition probabilities array
	 */
	public double[][][][] getTimeTransitionProbabilities() {
		assert hasTimeDependentTransitions && transitionsDefined;
		return timeTransitionProbabilities;
	}

	/**
	 * Initialize feasible actions such that all actions are feasible (default)
	 */
	private void initDefaultFeasibleActions() {
		this.feasibleActions = new int[nDecisions][nStates][nActions];
		for(int t=0; t<nDecisions; t++) {
			for(int s=0; s<nStates; s++) {
				for(int a=0; a<nActions; a++) {
					feasibleActions[t][s][a] = a;
				}
			}
		}
	}
	
	/**
	 * Set feasible actions
	 * @param feasibleActions array with feasible actions
	 */
	public void setFeasibleActions(int[][][] feasibleActions) {
		this.feasibleActions = feasibleActions;
	}
	
	/**
	 * Get feasible actions to execute in state s at time t
	 * @param t time
	 * @param s state
	 * @return array with feasible actions
	 */
	public int[] getFeasibleActions(int t, int s) {
		return feasibleActions[t][s];
	}
	
	/**
	 * Get feasible action array
	 * @return feasible action array
	 */
	public int[][][] getFeasibleActions() {
		return feasibleActions;
	}
}
