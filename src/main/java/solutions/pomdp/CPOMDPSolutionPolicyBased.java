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

import model.BeliefPoint;

public class CPOMDPSolutionPolicyBased implements CPOMDPSolution, Serializable {
	private static final long serialVersionUID = 1L;

	private POMDPAgentSolutionPolicyBased[] solutions;
	
	private POMDPPolicy[] currentPolicies;
	
	public CPOMDPSolutionPolicyBased(POMDPAgentSolutionPolicyBased[] solutions) {
		this.solutions = solutions;
	}
	
	@Override
	public int[] getActions(int t, BeliefPoint[] jointBelief) {
		// new run starts if t=0, so we select new policy for this run
		if(t == 0) {
			currentPolicies = new POMDPPolicy[solutions.length];
			for(int i=0; i<solutions.length; i++) {
				currentPolicies[i] = solutions[i].getPolicy();
				currentPolicies[i].reset();
			}
		}
		
		int[] actions = new int[solutions.length];
		for(int i=0; i<solutions.length; i++) {
			actions[i] = currentPolicies[i].getAction(jointBelief[i], t);
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
	public double getExpectedCost() {
		double expectedCost = 0.0;
		
		for(int i=0; i<solutions.length; i++) {
			expectedCost += solutions[i].getExpectedCost();
		}
		
		return expectedCost;
	}
	
	@Override
	public void update(int[] actions, int[] observations) {
		for(int i=0; i<solutions.length; i++) {
			currentPolicies[i].update(actions[i], observations[i]);
		}
	}
}
