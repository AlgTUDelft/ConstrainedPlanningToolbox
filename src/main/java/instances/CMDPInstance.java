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

import model.CMDP;

public class CMDPInstance {
	private CMDP[] cmdps;
	private double[] costLimitsBudget;
	private double[][] costLimitsInstantaneous;
	
	private ConstraintType constraintType;
	
	private int numDecisions = -1;
	private int numDomainResources = -1;
	
	private CMDPInstance(CMDP[] cmdps, double[][] costLimits, int numDecisions) {
		assert cmdps.length > 0;
		assert cmdps[0].getNumCostFunctions() == costLimits.length;
		assert cmdps[0].getNumDecisions() == numDecisions;
		
		this.cmdps = cmdps;
		this.costLimitsInstantaneous = costLimits;
		
		this.constraintType = ConstraintType.INSTANTANEOUS;
		
		this.numDecisions = numDecisions;
		this.numDomainResources = costLimits.length;
	}
	
	private CMDPInstance(CMDP[] cmdps, double[] costLimits, int numDecisions) {
		assert cmdps.length > 0;
		assert cmdps[0].getNumCostFunctions() == costLimits.length;
		assert cmdps[0].getNumDecisions() == numDecisions;
		
		this.cmdps = cmdps;
		this.costLimitsBudget = costLimits;
		
		this.constraintType = ConstraintType.BUDGET;
		
		this.numDecisions = numDecisions;
		this.numDomainResources = costLimits.length;
	}
	
	/**
	 * Get CMDP models of the agents
	 * @return array with CMDP for each agent
	 */
	public CMDP[] getCMDPs() {
		return cmdps;
	}
	
	/**
	 * Get constraint type used in this instance
	 * @return constraint type
	 */
	public ConstraintType getConstraintType() {
		return constraintType;
	}
	
	/**
	 * Get number of time steps, representing the planning horizon
	 * @return number of time steps
	 */
	public int getNumDecisions() {
		return numDecisions;
	}
	
	/**
	 * Get the number of resources in the domain
	 * @return number of resources
	 */
	public int getNumDomainResources() {
		return numDomainResources;
	}
	
	/**
	 * Get cost limit of resource k for budget constraints
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
	 * Get cost limit of resource k at time t, for instantaneous constraints
	 * @param k resource id
	 * @param t timestep
	 * @return limit
	 */
	public double getCostLimit(int k, int t) {
		assert constraintType == ConstraintType.INSTANTANEOUS;
		
		if(constraintType != ConstraintType.INSTANTANEOUS) {
			throw new RuntimeException("This problem instance does not have instantaneous constraints");
		}
		
		return costLimitsInstantaneous[k][t];
	}
	
	/**
	 * Get cost limits of the resources, for budget constraints
	 * @return array with cost limits
	 */
	public double[] getCostLimitsBudget() {
		return costLimitsBudget;
	}
	
	/**
	 * Get cost limits of the resources, for instantaneous constraints
	 * @return array with cost limits for each resource and time step
	 */
	public double[][] getCostLimitsInstantaneous() {
		return costLimitsInstantaneous;
	}
	
	/**
	 * Create a CMDPInstance with instantaneous constraints
	 * @param cmdps array with CMDP object for each agent
	 * @param costLimits cost limits for each resource and for each time step
	 * @param numDecisions number of decisions, representing the horizon
	 * @return instance
	 */
	public static CMDPInstance createInstantaneousInstance(CMDP[] cmdps, double[][] costLimits, int numDecisions) {
		assert cmdps.length > 0;
		assert cmdps[0].getNumCostFunctions() == costLimits.length;
		return new CMDPInstance(cmdps, costLimits, numDecisions);
	}
	
	/**
	 * Create a CMDPInstance with instantaneous constraints
	 * @param cmdps array with CMDP object for each agent
	 * @param costLimits cost limits for each resource
	 * @param numDecisions number of decisions, representing the horizon
	 * @return instance
	 */
	public static CMDPInstance createInstantaneousInstance(CMDP[] cmdps, double[] costLimits, int numDecisions) {
		assert cmdps.length > 0;
		assert cmdps[0].getNumCostFunctions() == costLimits.length;
		
		double[][] timeDependentCostLimits = new double[costLimits.length][numDecisions];
		for(int k=0; k<costLimits.length; k++) {
			for(int t=0; t<numDecisions; t++) {
				timeDependentCostLimits[k][t] = costLimits[k];
			}
		}
		
		return new CMDPInstance(cmdps, timeDependentCostLimits, numDecisions);
	}
	
	/**
	 * Create a CMDPInstance with budget constraints
	 * @param cmdps array with CMDP object for each agent
	 * @param costLimits cost limits for each resource
	 * @param numDecisions number of decisions, representing the horizon
	 * @return instance
	 */
	public static CMDPInstance createBudgetInstance(CMDP[] cmdps, double[] costLimits, int numDecisions) {
		assert cmdps.length > 0;
		assert cmdps[0].getNumCostFunctions() == costLimits.length;
		return new CMDPInstance(cmdps, costLimits, numDecisions);
	}
}
