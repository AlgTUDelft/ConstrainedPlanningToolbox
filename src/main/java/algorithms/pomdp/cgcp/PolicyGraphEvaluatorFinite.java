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

import java.util.ArrayList;

import solutions.pomdp.POMDPPolicyGraph;

import model.CPOMDP;
import model.POMDP;


public class PolicyGraphEvaluatorFinite {
	private POMDP pomdp;
	private POMDPPolicyGraph pg;
	
	private double expectedValue;
	private double expectedReward;
	private double expectedCost;
	
	public PolicyGraphEvaluatorFinite(POMDP pomdp, POMDPPolicyGraph pg) {
		this.pomdp = pomdp;
		this.pg = pg;
	}
	
	public void evaluate(double lambda) {
		int[] layerNodeCount = pg.getLayerNodeCount();
		ArrayList<int[]> layerNodeActions = pg.getLayerNodeActions();
		ArrayList<int[][]> layerTransitions = pg.getLayerTransitions();
		int startNode = pg.getStartNode();
		
		int t = pg.getNumLayers()-1;
		
		if(pomdp instanceof CPOMDP) {
			CPOMDP cpomdp = (CPOMDP) pomdp;
			
			double[][] Vvalue = new double[layerNodeCount[t]][pomdp.getNumStates()];
			double[][] Vreward = new double[layerNodeCount[t]][pomdp.getNumStates()];
			double[][] Vcost = new double[layerNodeCount[t]][pomdp.getNumStates()];
			
			while(t > 0) {
				t--;
				
				double[][] VvalueNext = new double[layerNodeCount[t]][pomdp.getNumStates()];
				double[][] VrewardNext = new double[layerNodeCount[t]][pomdp.getNumStates()];
				double[][] VcostNext = new double[layerNodeCount[t]][pomdp.getNumStates()];
				
				for(int q=0; q<layerNodeCount[t]; q++) {
					int a = layerNodeActions.get(t)[q];
					for(int s=0; s<pomdp.getNumStates(); s++){
						VvalueNext[q][s] = pomdp.getReward(s, a) - lambda * cpomdp.getCost(0, s, a);
						VrewardNext[q][s] = pomdp.getReward(s, a);
						VcostNext[q][s] = cpomdp.getCost(0, s, a);
						
						for(int o=0; o<pomdp.getNumObservations(); o++) {
							int qNext = layerTransitions.get(t)[q][o];
							
							int[] transitionDestinations = pomdp.getTransitionDestinations(s, a);
							double[] transitionProbabilities = pomdp.getTransitionProbabilities(s, a);
							
							for(int j=0; j<transitionDestinations.length; j++) {
								int sNext = transitionDestinations[j];
								double prob = transitionProbabilities[j];
								
								VvalueNext[q][s] += prob * pomdp.getObservationProbability(a, sNext, o) * Vvalue[qNext][sNext];
								VrewardNext[q][s] += prob * pomdp.getObservationProbability(a, sNext, o) * Vreward[qNext][sNext];
								VcostNext[q][s] += prob * pomdp.getObservationProbability(a, sNext, o) * Vcost[qNext][sNext];
							}
							
						}
					}
				}
				
				Vvalue = VvalueNext;
				Vreward = VrewardNext;
				Vcost = VcostNext;
			}
			
			expectedValue = 0.0;
			expectedReward = 0.0;
			expectedCost = 0.0;
			for(int s=0; s<pomdp.getNumStates(); s++) {
				expectedValue += Vvalue[startNode][s] * pomdp.getInitialBelief().getBelief(s);
				expectedReward += Vreward[startNode][s] * pomdp.getInitialBelief().getBelief(s);
				expectedCost += Vcost[startNode][s] * pomdp.getInitialBelief().getBelief(s);
			}
		}
		else {
			double[][] V = new double[layerNodeCount[t]][pomdp.getNumStates()];
			
			while(t > 0) {
				t--;
				
				double[][] Vnext = new double[layerNodeCount[t]][pomdp.getNumStates()];
				for(int q=0; q<layerNodeCount[t]; q++) {
					int a = layerNodeActions.get(t)[q];
					for(int s=0; s<pomdp.getNumStates(); s++){
						Vnext[q][s] = pomdp.getReward(s, a);
						
						for(int o=0; o<pomdp.getNumObservations(); o++) {
							int qNext = layerTransitions.get(t)[q][o];
							
							int[] transitionDestinations = pomdp.getTransitionDestinations(s, a);
							double[] transitionProbabilities = pomdp.getTransitionProbabilities(s, a);
							
							for(int j=0; j<transitionDestinations.length; j++) {
								int sNext = transitionDestinations[j];
								double prob = transitionProbabilities[j];
							
								Vnext[q][s] += prob * pomdp.getObservationProbability(a, sNext, o) * V[qNext][sNext];
							}
						}
					}
				}
				
				V = Vnext;
			}
			
			expectedReward = 0.0;
			for(int s=0; s<pomdp.getNumStates(); s++) {
				expectedReward += V[startNode][s] * pomdp.getInitialBelief().getBelief(s);
			}
			expectedValue = expectedReward;
		}
	}
	
	public double getExpectedReward() {
		return expectedReward;
	}
	
	public double getExpectedCost() {
		return expectedCost;
	}
	
	public double getExpectedValue() {
		return expectedValue;
	}
}
