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
package domains.tcl.primitives.generator.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import domains.tcl.primitives.Blueprint;


public class AnalyticAgentGen implements AgentGenerator {

	private Random randGen;

	private int setpoint;

	private int minTransitionTime;
	private int maxTransitionTime;

	public AnalyticAgentGen(int setpoint) {
		setSetpoint(setpoint);
		setTransitionTimes(150, 270);
		setRandomGen(new Random());
	}

	public void setSetpoint(int setpoint) {
		this.setpoint = setpoint;
	}

	public void setTransitionTimes(int minTime, int maxTime) {
		this.minTransitionTime = minTime;
		this.maxTransitionTime = maxTime;
	}

	@Override
	public void setRandomGen(Random randGen) {
		this.randGen = randGen;
	}

	@Override
	public String getName() {
		return "Analytic";
	}

	@Override
	public List<Blueprint> generateAgents(int numAgents) {
		List<Blueprint> agents = new ArrayList<>();

		for (int id = 0; id < numAgents; id++) {
			int secondsToHeat = minTransitionTime + (int) Math.round((maxTransitionTime - minTransitionTime) * randGen.nextDouble());
			int secondsToCool = minTransitionTime + (int) Math.round((maxTransitionTime - minTransitionTime) * randGen.nextDouble());

			agents.add(constructAgent(id, secondsToCool, secondsToHeat));
		}

		return agents;
	}

	private Blueprint constructAgent(int agentID, int secondsToCool, int secondsToHeat) {
		final double tOut  = 0;
		final double tMin  = setpoint - 0.5;
		final double tMax  = setpoint + 0.5;
		final int    delta = 60;

		double alpha = computeAlpha(tMax, tMin, tOut, secondsToCool);
		double R = 1;
		double C = computeCapacitance(alpha, delta);
		double P = computePower(tMin, tMax, alpha, secondsToHeat) - tOut;

		return new Blueprint(agentID, R, C, P);
	}

	private double computeAlpha(double tZero, double tGoal, double p, double t) {
		return Math.pow(((p-tGoal)/(p-tZero)), (1D/t));
	}

	private double computeCapacitance(double alpha, double delta) {
		return (-1D / 3600D) / Math.log(alpha);
	}

	private double computePower(double tZero, double tGoal, double alpha, double t) {
		return (tZero * Math.pow(alpha, t) - tGoal) / (Math.pow(alpha, t) - 1D);
	}

}
