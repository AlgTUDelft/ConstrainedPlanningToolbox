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

public class CMDPSolutionPolicyBased implements CMDPSolution, Serializable {
	
	private static final long serialVersionUID = 1L;

	private MDPAgentSolutionPolicyBased[] solutions;
	
	private MDPPolicy[] currentPolicies;
	
	public CMDPSolutionPolicyBased(MDPAgentSolutionPolicyBased[] solutions) {
		this.solutions = solutions;
	}
	
	@Override
	public int[] getActions(int t, int[] jointState) {
		// new run starts if t=0, so we select new policy for this run
		if(t == 0) {
			currentPolicies = new MDPPolicy[solutions.length];
			for(int i=0; i<solutions.length; i++) {
				currentPolicies[i] = solutions[i].getPolicy();
			}
		}
		
		int[] actions = new int[solutions.length];
		for(int i=0; i<solutions.length; i++) {
			actions[i] = currentPolicies[i].getAction(t, jointState[i]);
		}
		
		return actions;
	}

	@Override
	public double getExpectedReward() {
		double expectedReward = 0.0;
		
		for(int i=0; i<solutions.length; i++) {
			expectedReward += solutions[i].getExpectedReward();
		}
		
		return expectedReward;
	}

	@Override
	public double getExpectedTotalCost(int k) {
		double expectedCost = 0.0;
		
		for(int i=0; i<solutions.length; i++) {
			expectedCost += solutions[i].getExpectedTotalCost(k);
		}
		
		return expectedCost;
	}

	@Override
	public double getExpectedInstantaneousCost(int k, int t) {
		double expectedCost = 0.0;
		
		for(int i=0; i<solutions.length; i++) {
			expectedCost += solutions[i].getExpectedInstantaneousCost(k, t);
		}
		
		return expectedCost;
	}
	
	
}
