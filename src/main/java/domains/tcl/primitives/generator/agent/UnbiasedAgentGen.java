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
import domains.tcl.primitives.TCL;


public class UnbiasedAgentGen implements AgentGenerator {
	private Random randGen;

	private int setpoint;

	private int minTransitionTime;
	private int maxTransitionTime;

	private double minC;
	private double maxC;

	public UnbiasedAgentGen(int setpoint)
	{
		setRandomGen(new Random());

		setSetpoint(setpoint);

		setTransitionTimes(150, 270);
	}

	public void setTransitionTimes(int minTime, int maxTime)
	{
		minTransitionTime = minTime;
		maxTransitionTime = maxTime;
	}

	@Override
	public void setRandomGen(Random randGen)
	{
		this.randGen = randGen;
	}

	private void setSetpoint(int setpoint)
	{
		this.setpoint = setpoint;
	}

	@Override
	public List<Blueprint> generateAgents(int numAgents)
	{
		List<Blueprint> agents = new ArrayList<Blueprint>();

		recomputeCached();

		for (int i = 0; i < numAgents; i++)
		{
			agents.add(randomizeAgent(i));
		}

		return agents;
	}

	private void recomputeCached()
	{
		// Compute capacitances.
		minC = computeBestC(minTransitionTime, setpoint);
		maxC = computeBestC(maxTransitionTime, setpoint);
	}

	public Blueprint randomizeAgent(int id)
	{
		int secondsToHeat = minTransitionTime + (int) Math.round((maxTransitionTime - minTransitionTime) * randGen.nextDouble());

		double R = 1;
		double C = minC + randGen.nextDouble() * (maxC-minC);
		double P = computeBestP(C, secondsToHeat, setpoint);

		return new Blueprint(id, R, C, P);
	}

	private double computeBestC(int pDecreaseTimeSeconds, double pSetTemp)
	{
		double bestC = 1;
		int stepsize = 1;

		int bestDiff = Integer.MAX_VALUE;
		for (double C = 100; C > 0; C = C - 0.01)
		{
			TCL lTrial = new TCL(0, 1, C, 0, pSetTemp);
			lTrial.setOutsideTemperature(0);
			lTrial.setStepSize(stepsize);

			int    lSteps = 0;
			double lTemp = pSetTemp + 0.5;
			while (lTemp > pSetTemp - 0.5)
			{
				lTemp = lTrial.computeNextTemperature(lTemp, false);
				lSteps = lSteps + stepsize;
			}

			if (Math.abs(pDecreaseTimeSeconds - lSteps) < bestDiff)
			{
				bestC    = C;
				bestDiff = Math.abs(pDecreaseTimeSeconds - lSteps);
			}
		}

		return bestC;
	}

	private double computeBestP(double C, int pIncreaseTimeSeconds, double pSetTemp)
	{
		double bestP = 1;
		int stepsize = 1;

		int bestDiff = Integer.MAX_VALUE;
		for (double P = 200; P > pSetTemp+1; P = P - 0.01)
		{
			TCL lTrial = new TCL(0, 1, C, P, pSetTemp);
			lTrial.setOutsideTemperature(0);
			lTrial.setStepSize(stepsize);

			int    lSteps = 0;
			double lTemp = pSetTemp - 0.5;
			while (lTemp < pSetTemp + 0.5)
			{
				lTemp = lTrial.computeNextTemperature(lTemp, true);
				lSteps = lSteps + stepsize;
			}

			if (Math.abs(pIncreaseTimeSeconds - lSteps) < bestDiff)
			{
				bestP    = P;
				bestDiff = Math.abs(pIncreaseTimeSeconds - lSteps);
			}
		}

		return bestP;
	}

	@Override
	public String getName()
	{
		return "Unbiased";
	}
}
