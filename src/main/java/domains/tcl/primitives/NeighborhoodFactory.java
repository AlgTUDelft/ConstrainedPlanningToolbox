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
package domains.tcl.primitives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.TreeSet;

import util.ConsoleOutput;

import domains.tcl.primitives.util.OrderedTuple;
import domains.tcl.primitives.util.PowerLimits;


public class NeighborhoodFactory
{
	private final List<TCL> fLoads;

	private TreeSet<OrderedTuple> fLimits;

	private int fSetPoint;

	private double fOutTemperature;

	public NeighborhoodFactory()
	{
		this.fLoads			 = new ArrayList<TCL>();
		this.fLimits		 = new TreeSet<OrderedTuple>();
		this.fSetPoint		 = 0;
		this.fOutTemperature = 0;
	}

	public void addTCL(double pR, double pC, double pP)
	{
		TCL lTCL = new TCL(this.fLoads.size(), pR, pC, pP, this.fSetPoint);
		this.fLoads.add(lTCL);
	}

	public void setSetpoint(int pSetPoint)
	{
		this.fSetPoint = pSetPoint;
	}

	public void setOutsideTemperature(double pOutTemperature)
	{
		this.fOutTemperature = pOutTemperature;
	}

	/**
	 * From second pStartTime the limit pLimit is enforced, restricting the number of TCLs that can be ON
	 * 
	 * @param pStartTime
	 * @param pLimit
	 */
	public void addLimit(int pStartTime, int pLimit)
	{
		this.fLimits.add(new OrderedTuple(pStartTime, pLimit));
	}

	public List<TCL> getTCLs()
	{
		return this.fLoads;
	}


	public TCL getTCL(int pID)
	{
		return this.fLoads.get(pID);
	}

	public double getOutsideTemperature()
	{
		return this.fOutTemperature;
	}

	public int getSetPoint()
	{
		return this.fSetPoint;
	}

	/**
	 * This procedure transforms the list of times when limits are imposed into a mapping of <time, limit> pairs.
	 * 
	 * @param pHorizon
	 * @param pStepSize
	 * @return
	 */
	public PowerLimits getLimits(int pHorizon, int pStepSize)
	{
		PriorityQueue<OrderedTuple> lStack = new PriorityQueue<OrderedTuple>(this.fLimits);
		PowerLimits lLimits = new PowerLimits(this.fLoads.size());

		int lLimit;
		for (int i = 0; i < pHorizon / pStepSize; i++)
		{
			Integer lNewLimit = null;
			while (!lStack.isEmpty() && lStack.peek().getTime() < (i+1) * pStepSize)
			{
				OrderedTuple lTuple = lStack.poll();

				if (lNewLimit == null || lNewLimit > lTuple.getLimit())
				{
					lNewLimit = lTuple.getLimit();
				}
			}

			lLimit = this.fLoads.size();
			if (lNewLimit != null)
			{
				lLimit = lNewLimit;
			}

			lLimits.put(i, lLimit);
		}

		return lLimits;
	}

	public Neighborhood generateExpectedInstance()
	{
		return this.generateInstance(0, null);
	}

	public Neighborhood generateInstance(double pVariance, long pSeed)
	{
		return this.generateInstance(pVariance, new Random(pSeed));
	}

	private Neighborhood generateInstance(double pVariance, Random pRandGen)
	{
		Map<TCL, Double> lInitialTemperatures = new HashMap<TCL, Double>();

		for (TCL lTCL : this.fLoads)
		{
			double lVariance = 0;

			if (pRandGen != null)
			{
				lVariance = (pRandGen.nextDouble() - 0.5) * pVariance;
			}

			lTCL.setOutsideTemperature(this.fOutTemperature);
			lTCL.setStepSize(1);
			lInitialTemperatures.put(lTCL, this.fSetPoint + lVariance);
		}
		Neighborhood lNeighborhood = new Neighborhood(this.fLoads, lInitialTemperatures, this.fLimits);

		return lNeighborhood;
	}

	public void printInstance(int pHorizon, int pTimeStep)
	{
		ConsoleOutput.println("[Temperatures]");
		ConsoleOutput.println("Out\t"+this.getOutsideTemperature());
		ConsoleOutput.println("Set\t"+this.getSetPoint());
		ConsoleOutput.println();
		ConsoleOutput.println("[ID\ta\tTheta_pwr]");
		for (int i = 0; i < this.getTCLs().size(); i++)
		{
			TCL lTCL = this.getTCL(i);
			ConsoleOutput.println(i + "\t" + lTCL.getLeakageA() + "\t" + lTCL.getHeatingP() * lTCL.getResistanceR());
		}
		ConsoleOutput.println();
		ConsoleOutput.println("[Limits]");
		PowerLimits lLimits = this.getLimits(pHorizon*pTimeStep, pTimeStep);
		for (int t = 0; t < pHorizon; t++)
		{
			ConsoleOutput.println(t + "\t" + lLimits.getLimit(t));
		}
	}
}
