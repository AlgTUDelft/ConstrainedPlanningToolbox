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
import java.util.Random;

import util.ProbabilitySample;

public class MDPPolicyStochastic implements MDPPolicy, MDPAgentSolutionPolicyBased, Serializable {
	private static final long serialVersionUID = 1L;
	private double[][][] x; // x[t][s][a] is the probability that a is selected in state s at time t (note that x[t][s][.]'s dont always sum to 1)
	private double expectedReward;
	private Random rnd;
	
	public MDPPolicyStochastic(double[][][] x, double expectedReward, Random rnd) {
		this.rnd = rnd;
		this.x = x;
		this.expectedReward = expectedReward;
	}
	
	public int getAction(int t, int s) {
		ProbabilitySample ps = new ProbabilitySample(rnd);
		int nActions = x[t][s].length;
		
		double probSum = 0.0;
		for(int a=0; a<nActions; a++) {
			probSum += x[t][s][a];
		}
		
		assert probSum > 0.0 : "Prob sum: "+probSum;
		
		for(int a=0; a<nActions; a++) {
			if(x[t][s][a] > 0.0) {
				double prob = x[t][s][a] / probSum;
				if(prob > 1.0) prob = 1.0;
				if(prob < 0.0) prob = 0.0;
				ps.addItem(a, prob);
			}
		}
		
		return ps.sampleItem();
	}

	public MDPPolicy getPolicy() {
		return this;
	}

	public double getExpectedReward() {
		return expectedReward;
	}

	@Override
	public double getExpectedInstantaneousCost(int k, int t) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public double getExpectedTotalCost(int k) {
		throw new RuntimeException("not implemented");
	}
	
}
