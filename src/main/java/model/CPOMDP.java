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

import java.util.List;

public class CPOMDP extends POMDP {	
	private List<double[][]> costFunctions;
	private double[] minCost;
	private double[] maxCost;
	
	public CPOMDP(int nStates, int nActions, int nObservations, List<double[][]> costFunctions, double[][][] observationFunction, BeliefPoint b0, int nDecisions) {
		super(nStates, nActions, nObservations, observationFunction, b0, nDecisions);
		
		this.costFunctions = costFunctions;
		
		minCost = new double[costFunctions.size()];
		maxCost = new double[costFunctions.size()];
		
		for(int k=0; k<costFunctions.size(); k++) {
			double[][] costFunction = costFunctions.get(k);
			minCost[k] = Double.POSITIVE_INFINITY;
			maxCost[k] = Double.NEGATIVE_INFINITY;
			
			// compute min and max cost
			for(int s=0; s<nStates; s++) {
				for(int a=0; a<nActions; a++) {
					minCost[k] = Math.min(minCost[k], costFunction[s][a]);
					maxCost[k] = Math.max(maxCost[k], costFunction[s][a]);
				}
			}
		}
	}
	
	/**
	 * Get cost / resource consumption for resource k when executing action a in state s
	 * @param k resource id
	 * @param s state
	 * @param a action
	 * @return cost / resource consumption
	 */
	public double getCost(int k, int s, int a) {
		assert s<super.getNumStates() && a<super.getNumActions();
		double[][] costFunction = costFunctions.get(k);
		return costFunction[s][a];
	}
	
	/**
	 * Get cost function for resource k
	 * @param k resource id
	 * @return cost function
	 */
	public double[][] getCostFunction(int k) {
		return costFunctions.get(k);
	}
	
	/**
	 * Get minimum cost incurred for resource k when executing an action
	 * @param k resource id
	 * @return min cost
	 */
	public double getMinCost(int k) {
		return minCost[k];
	}
	
	/**
	 * Get maximum cost incurred for resource k when executing an action
	 * @param k resource id
	 * @return max cost
	 */
	public double getMaxCost(int k) {
		return maxCost[k];
	}
	
	/**
	 * Get number of cost functions
	 * @return number of cost functions
	 */
	public int getNumCostFunctions() {
		return costFunctions.size();
	}
	
	/**
	 * Get cost functions
	 * @return list of cost functions
	 */
	public List<double[][]> getCostFunctions() {
		return costFunctions;
	}
}
