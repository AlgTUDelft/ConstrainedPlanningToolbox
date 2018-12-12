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
package solutions.pomdp;

import java.io.Serializable;
import java.util.Random;

import util.ProbabilitySample;

import model.BeliefPoint;


public class POMDPPolicyFSC implements POMDPAgentSolutionPolicyBased, POMDPPolicy, Serializable {
	private static final long serialVersionUID = 1L;
	private int numNodes;
	private int numActions;
	private int numObservations;
	private double[][][] policy; // policy[b][a] = probability to select a in node b
	private double[][][][] weights; // w[bPrime][b][a][o] = w(b', b^{a,o})
	private double expectedReward;
	private double expectedCost;
	private double expectedValue;
	private double expectedValueUpperbound;
	private Random rnd;
	private int startNode;
	private int currentNode;
	private int currentTime;
	
	public POMDPPolicyFSC(int numNodes, int numActions, int numObservations, double[][][] policy, double[][][][] weights, int startNode, double expectedValue, double expectedReward, double expectedCost, Random rnd) {
		this.numNodes = numNodes;
		this.numActions = numActions;
		this.numObservations = numObservations;
		this.policy = policy;
		this.weights = weights;
		this.expectedValue = expectedValue;
		this.expectedReward = expectedReward;
		this.expectedCost = expectedCost;
		this.expectedValueUpperbound = Double.POSITIVE_INFINITY;
		this.rnd = rnd;
		this.startNode = startNode;
		this.currentNode = startNode;
		this.currentTime = 0;
		
		// check whether the weights represent a valid transition model
		for(int bIndex=0; bIndex<numNodes; bIndex++) {
			for(int a=0; a<numActions; a++) {
				for(int o=0; o<numObservations; o++) {					

					double sum = 0.0;
					
					for(int nextB=0; nextB<numNodes; nextB++) {
						sum += weights[nextB][bIndex][a][o];
					}
					
					assert Math.abs(sum-1.0) < 0.0001 : "Weights do not sum to 1: "+sum;
				}
			}
		}
		
		// check whether policy is valid
		for(int t=0; t<policy.length; t++) {
			for(int bIndex=0; bIndex<numNodes; bIndex++) {
				double sum = 0.0;
				for(int a=0; a<numActions; a++) {
					assert policy[t][bIndex][a] >= -0.02 && policy[t][bIndex][a] <= 1.02 : "prob: "+policy[t][bIndex][a];
					sum += policy[t][bIndex][a];
				}
				assert Math.abs(sum-1.0) < 0.0001 : "Probabilities do not sum to 1: "+sum;
			}
		}
	}
	
	@Override
	public int getAction(BeliefPoint b, int t) {
		// belief point parameter is ignored
		assert currentTime == t : currentTime+" "+t;
		
		ProbabilitySample ps = new ProbabilitySample(rnd);
		for(int a=0; a<numActions; a++) {
			double prob = policy[t][currentNode][a];
			assert prob >= -0.01 && prob <= 1.01 : "prob: "+prob;
			
			if(prob < 0.0) prob = 0.0;
			if(prob > 1.0) prob = 1.0;
			
			ps.addItem(a, prob);
		}
		
		return ps.sampleItem();
	}
	
	@Override
	public void update(int a, int o) {
		assert a >= 0 && a < numActions && o >= 0 && o < numObservations;
		
		ProbabilitySample ps = new ProbabilitySample(rnd);
		for(int node=0; node<numNodes; node++) {
			double prob = weights[node][currentNode][a][o];
			assert prob >= -0.01 && prob <= 1.01;
			
			if(prob < 0.0) prob = 0.0;
			if(prob > 1.0) prob = 1.0;
			
			ps.addItem(node, prob);
		}
		
		currentNode = ps.sampleItem();
		currentTime++;
	}

	@Override
	public POMDPPolicy getPolicy() {
		return this;
	}
	
	@Override
	public double getExpectedValue() {
		return expectedValue;
	}

	@Override
	public double getExpectedReward() {
		return expectedReward;
	}

	@Override
	public double getExpectedCost() {
		return expectedCost;
	}

	@Override
	public void reset() {
		this.currentNode = startNode;
		this.currentTime = 0;
	}
	
	public double getExpectedValueUpperbound() {
		return expectedValueUpperbound;
	}
	
	public void setExpectedValueUpperbound(double expectedValueUpperbound) {
		this.expectedValueUpperbound = expectedValueUpperbound;
	}
}
