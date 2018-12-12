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
import java.util.ArrayList;


import model.BeliefPoint;


public class POMDPPolicyGraph implements POMDPAgentSolutionPolicyBased, POMDPPolicy, Serializable {
	private static final long serialVersionUID = 1L;
	private int numLayers;
	private int[] layerNodeCount;
	private int numActions;
	private int numObservations;
	private ArrayList<BeliefPoint[]> layerNodeBeliefs;
	private ArrayList<int[]> layerNodeActions;
	private ArrayList<int[][]> layerTransitions;
	private int startNode;
	
	private double expectedReward = 0.0;
	private double expectedCost = 0.0;
	private double expectedValue = 0.0;
	private double expectedValueUpperbound = Double.POSITIVE_INFINITY;
	
	private int currentLayer;
	private int currentNode;
	
	public POMDPPolicyGraph(int numLayers, int[] layerNodeCount, int numActions, int numObservations, ArrayList<int[]> layerNodeActions, ArrayList<BeliefPoint[]> layerNodeBeliefs, ArrayList<int[][]> layerTransitions, int startNode) {
		this.numLayers = numLayers;
		this.layerNodeCount = layerNodeCount;
		this.numActions = numActions;
		this.numObservations = numObservations;
		this.layerNodeActions = layerNodeActions;
		this.layerNodeBeliefs = layerNodeBeliefs;
		this.layerTransitions = layerTransitions;
		this.startNode = startNode;
		this.currentLayer = 0;
		this.currentNode = startNode;
	}

	public int getAction(BeliefPoint b, int t) {
		assert currentLayer == t;
		return layerNodeActions.get(currentLayer)[currentNode];
	}

	public void update(int a, int o) {
		assert a >= 0 && a < numActions && o >= 0 && o < numObservations;
		
		if(currentLayer < numLayers-1) {
			int[][] transitions = layerTransitions.get(currentLayer);
			currentNode = transitions[currentNode][o];
			if(currentLayer < numLayers-1) currentLayer++;
			assert currentLayer >= 0 && currentLayer < numLayers;
			assert currentNode >= 0 && currentNode < layerNodeCount[currentLayer] : "Current node: "+currentNode+", num nodes in layer: "+layerNodeCount[currentLayer];
		}
	}

	public void reset() {
		currentLayer = 0;
		currentNode = startNode;
	}

	public POMDPPolicy getPolicy() {
		return this;
	}
	
	public int getNumLayers() {
		return numLayers;
	}
	
	public int[] getLayerNodeCount() {
		return layerNodeCount;
	}
	
	public ArrayList<int[]> getLayerNodeActions() {
		return layerNodeActions;
	}
	
	public ArrayList<BeliefPoint[]> getLayerNodeBeliefs() {
		return layerNodeBeliefs;
	}
	
	public ArrayList<int[][]> getLayerTransitions() {
		return layerTransitions;
	}
	
	public int getStartNode() {
		return startNode;
	}
	
	@Override
	public double getExpectedValue() {
		return expectedValue;
	}
	
	public void setExpectedValue(double value) {
		expectedValue = value;
	}
	
	public double getExpectedValueUpperbound() {
		return expectedValueUpperbound;
	}
	
	public void setExpectedValueUpperbound(double expectedValueUpperbound) {
		this.expectedValueUpperbound = expectedValueUpperbound;
	}

	@Override
	public double getExpectedReward() {
		return expectedReward;
	}

	public void setExpectedReward(double reward) {
		expectedReward = reward;
	}
	
	@Override
	public double getExpectedCost() {
		return expectedCost;
	}
	
	public void setExpectedCost(double cost) {
		expectedCost = cost;
	}
}
