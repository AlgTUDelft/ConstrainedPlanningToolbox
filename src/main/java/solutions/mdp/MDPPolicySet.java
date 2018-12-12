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
import java.util.List;
import java.util.Random;

import util.ProbabilitySample;

public class MDPPolicySet implements MDPAgentSolutionPolicyBased, Serializable {
	private static final long serialVersionUID = 1L;
	private List<MDPPolicy> policies;
	private List<Double> probabilities;
	private double expectedReward;
	private double[][] expectedInstantaneousCost;
	private double[] expectedTotalCost;
	private Random rnd;
	
	public MDPPolicySet(List<MDPPolicy> policies, List<Double> probabilities, double expectedReward, double[][] expectedInstantaneousCost, double[] expectedTotalCost, Random rnd) {
		this.policies = policies;
		this.probabilities = probabilities;
		this.rnd = rnd;
		this.expectedReward = expectedReward;
		this.expectedInstantaneousCost = expectedInstantaneousCost;
		this.expectedTotalCost = expectedTotalCost;
	}
	
	@Override
	public MDPPolicy getPolicy() {
		ProbabilitySample ps = new ProbabilitySample(rnd);
		for(int i=0; i<policies.size(); i++) {
			double prob = probabilities.get(i);
			if(prob > 1.0) prob = 1.0;
			if(prob < 0.0) prob = 0.0;
			
			ps.addItem(i, prob);
		}
		return policies.get(ps.sampleItem());
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
