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
import java.util.ArrayList;


import model.AlphaVector;
import model.BeliefPoint;


public class POMDPPolicyVector implements POMDPPolicy, POMDPAgentSolutionPolicyBased, Serializable {
	private static final long serialVersionUID = 1L;
	private ArrayList<ArrayList<AlphaVector>> vectorList;
	private int maxT;
	private double expectedValue;
	private double expectedReward;
	private double expectedCost;
	private double expectedValueUpperbound;
	
	public POMDPPolicyVector(ArrayList<ArrayList<AlphaVector>> vectors, double expectedValue) {
		this.vectorList = vectors;
		this.maxT = vectors.size()-1;
		this.expectedValue = expectedValue;
		this.expectedValueUpperbound = Double.POSITIVE_INFINITY;
	}
	
	public POMDPPolicyVector(ArrayList<ArrayList<AlphaVector>> vectors) {
		this.vectorList = vectors;
		this.maxT = vectors.size()-1;
		this.expectedValue = 0.0;
		this.expectedValueUpperbound = Double.POSITIVE_INFINITY;
	}
	
	@Override
	public int getAction(BeliefPoint b, int t) {
		assert t>=0 && t<=maxT;
		ArrayList<AlphaVector> vectors = vectorList.get(t);
		int vectorIndex = AlphaVector.getBestVectorIndex(b.getBelief(), vectors);
		assert vectorIndex >= 0 && vectorIndex < vectors.size();
		return vectors.get(vectorIndex).getAction();
	}

	@Override
	public void update(int a, int o) {
		// dummy
	}

	@Override
	public void reset() {
		// dummy
	}

	@Override
	public POMDPPolicy getPolicy() {
		return this;
	}

	@Override
	public double getExpectedValue() {
		return expectedValue;
	}
	
	public double getExpectedValueUpperbound() {
		return expectedValueUpperbound;
	}
	
	public void setExpectedValueUpperbound(double expectedValueUpperbound) {
		this.expectedValueUpperbound = expectedValueUpperbound;
	}

	@Override
	public double getExpectedReward() {
		return expectedReward;
	}

	@Override
	public double getExpectedCost() {
		return expectedCost;
	}
}
