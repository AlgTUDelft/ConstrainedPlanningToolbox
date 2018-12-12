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
import java.util.List;
import java.util.Random;

import util.ProbabilitySample;


public class POMDPPolicySet implements POMDPAgentSolutionPolicyBased, Serializable {
	private static final long serialVersionUID = 1L;
	private List<POMDPPolicy> policies;
	private double[] probabilities;
	private double expectedValue;
	private double expectedReward;
	private double expectedCost;
	private double expectedValueUpperbound;
	private Random rnd;
	
	public POMDPPolicySet(List<POMDPPolicy> policies, double[] probabilities, double expectedValue, double expectedReward, double expectedCost, Random rnd) {
		this.policies = policies;
		this.probabilities = probabilities;
		this.rnd = rnd;
		this.expectedValue = expectedValue;
		this.expectedReward = expectedReward;
		this.expectedCost = expectedCost;
		this.expectedValueUpperbound = Double.POSITIVE_INFINITY;
	}
	
	@Override
	public POMDPPolicy getPolicy() {
		assert policies.size() > 0;
		
		ProbabilitySample ps = new ProbabilitySample(rnd);
		for(int i=0; i<policies.size(); i++) {
			ps.addItem(i, probabilities[i]);
		}
		return policies.get(ps.sampleItem());
	}
	
	@Override
	public double getExpectedValue() {
		return expectedValue;
	}

	@Override
	public double getExpectedReward() {
		return expectedReward;
	}

	@Override
	public double getExpectedCost() {
		return expectedCost;
	}
	
	@Override
	public double getExpectedValueUpperbound() {
		return expectedValueUpperbound;
	}
}
