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
package instances;

import model.CPOMDP;

public class CPOMDPInstance {
	private CPOMDP[] cpomdps;
	private double[] costLimitsBudget;
	
	private ConstraintType constraintType;
	
	private int numDecisions = -1;
	private int numDomainResources = -1;
	
	private CPOMDPInstance(CPOMDP[] cpomdps, double[] costLimits, int numDecisions) {
		assert cpomdps.length > 0;
		assert cpomdps[0].getNumCostFunctions() == costLimits.length;
		
		this.cpomdps = cpomdps;
		this.costLimitsBudget = costLimits;
		
		this.constraintType = ConstraintType.BUDGET;
		
		this.numDecisions = numDecisions;
		this.numDomainResources = costLimits.length;
	}
	
	/**
	 * Get CPOMDP objects of the agents
	 * @return array with CPOMDP object for each agent
	 */
	public CPOMDP[] getCPOMDPs() {
		return cpomdps;
	}
	
	/**
	 * Get constraint type of this instance
	 * @return constraint type
	 */
	public ConstraintType getConstraintType() {
		return constraintType;
	}
	
	/**
	 * Get number of decisions, representing the horizon
	 * @return number of decisions
	 */
	public int getNumDecisions() {
		return numDecisions;
	}
	
	/**
	 * Get number of resources in the domain
	 * @return number of resources
	 */
	public int getNumDomainResources() {
		return numDomainResources;
	}
	
	/**
	 * Get cost limit of resource k, for budget constraints
	 * @param k resource id
	 * @return limit
	 */
	public double getCostLimit(int k) {
		assert constraintType == ConstraintType.BUDGET;
		
		if(constraintType != ConstraintType.BUDGET) {
			throw new RuntimeException("This problem instance does not have budget constraints");
		}
		
		return costLimitsBudget[k];
	}
	
	/**
	 * Get cost limits of the resources
	 * @return limits
	 */
	public double[] getCostLimits() {
		return costLimitsBudget;
	}
	
	/**
	 * Create a CPOMDPInstance with budget constraints
	 * @param cpomdps array with CPOMDP object for each agent
	 * @param costLimits cost limits for each resource
	 * @param numDecisions number of decisions, representing the horizon
	 * @return instance
	 */
	public static CPOMDPInstance createBudgetInstance(CPOMDP[] cpomdps, double[] costLimits, int numDecisions) {
		assert cpomdps.length > 0;
		assert cpomdps[0].getNumCostFunctions() == costLimits.length;
		return new CPOMDPInstance(cpomdps, costLimits, numDecisions);
	}
}
