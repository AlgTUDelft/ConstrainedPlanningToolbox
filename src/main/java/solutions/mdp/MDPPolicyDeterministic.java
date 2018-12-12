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
package solutions.mdp;

import java.io.Serializable;

public class MDPPolicyDeterministic implements MDPPolicy, MDPAgentSolutionPolicyBased, Serializable {
	private static final long serialVersionUID = 1L;
	private int[][] pi;
	private double expectedReward;
	private double[][] expectedInstantaneousCost;
	private double[] expectedTotalCost;
	
	public MDPPolicyDeterministic(int[][] pi, double expectedReward, double[][] expectedInstantaneousCost, double[] expectedTotalCost) {
		this.pi = pi;
		this.expectedReward = expectedReward;
		this.expectedInstantaneousCost = expectedInstantaneousCost;
		this.expectedTotalCost = expectedTotalCost;
	}
	
	public int getAction(int t, int s) {
		return pi[t][s];
	}

	public MDPPolicy getPolicy() {
		return this;
	}

	@Override
	public double getExpectedReward() {
		return expectedReward;
	}

	@Override
	public double getExpectedInstantaneousCost(int k, int t) {
		return expectedInstantaneousCost[k][t];
	}

	@Override
	public double getExpectedTotalCost(int k) {
		return expectedTotalCost[k];
	}
}
