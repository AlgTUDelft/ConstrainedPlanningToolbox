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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.TreeSet;

import domains.tcl.primitives.util.OrderedTuple;


public class Neighborhood {
	private final List<TCL> fLoads;

	private final Map<TCL, Double> fInitialTemperature;

	private final TreeSet<OrderedTuple> fLimits;

	private PriorityQueue<OrderedTuple> fFutureLimits;

	private Map<TCL, Double> fCurrentTemperature;

	private int fBlockLimit;

	private int fCurrentLimit;

	private int fTime;

	private double fPenalty;

	private double fPenaltySum;

	private double fAverageTemperature;

	public Neighborhood(List<TCL> pLoads, Map<TCL, Double> pInit, TreeSet<OrderedTuple> pLimits)
	{
		this.fLoads				 = pLoads;
		this.fInitialTemperature = pInit;
		this.fLimits			 = pLimits;

		this.reset();
	}

	public List<TCL> getTCLs()
	{
		return this.fLoads;
	}

	public double getTemperature(TCL pTCL)
	{
		return this.fCurrentTemperature.get(pTCL);
	}

	public void reset()
	{
		this.fTime				 = 0;
		this.fCurrentTemperature = new HashMap<TCL, Double>();
		this.fFutureLimits		 = new PriorityQueue<OrderedTuple>(this.fLimits);
		this.fCurrentLimit		 = this.fLoads.size();
		this.fPenalty			 = 0;
		this.fPenaltySum		 = 0;

		this.fAverageTemperature = 0;
		for (TCL lTCL : this.fLoads)
		{
			double lInitialTemperature = this.fInitialTemperature.get(lTCL);
			this.fCurrentTemperature.put(lTCL, lInitialTemperature);
			this.fAverageTemperature = this.fAverageTemperature + lInitialTemperature;
		}
		this.fAverageTemperature = this.fAverageTemperature / this.fLoads.size();
	}

	public Map<TCL, Double> getCurrentState()
	{
		Map<TCL, Double> lCurrentState = new HashMap<TCL, Double>();

		for (TCL lTCL : this.fLoads)
		{
			lCurrentState.put(lTCL, this.getTemperature(lTCL));
		}

		return lCurrentState;
	}

	public int getLimit()
	{
		return this.fCurrentLimit;
	}

	public int getBlockLimit()
	{
		return this.fBlockLimit;
	}

	public void advanceTime(int pStep, Map<TCL, Boolean> pSwitchDecisions)
	{
		this.advanceTime(pStep, pSwitchDecisions, null, 0);
	}

	public void advanceTime(int pStep, Map<TCL, Boolean> pSwitchDecisions, Random pRandom, double pVariance)
	{
		/*
		 * Create generator for random
		 */
		/*
		 * Reset the current time-step penalty.
		 */
		this.fPenalty = 0;

		/*
		 * Determine the limit coming into this block, and the smallest limit encountered inside this block.
		 */
		int lMinimumLimit = this.fCurrentLimit;
		int lFutureLimit  = this.fCurrentLimit;

		while (!this.fFutureLimits.isEmpty() && this.fFutureLimits.peek().getTime() < this.fTime + pStep)
		{
			OrderedTuple lLimit = this.fFutureLimits.poll();

			if (lLimit.getTime() == this.fTime)
			{
				lMinimumLimit = lLimit.getLimit();
			}

			lMinimumLimit = Math.min(lMinimumLimit, lLimit.getLimit());
			lFutureLimit  = lLimit.getLimit();
		}

		this.fBlockLimit = lMinimumLimit;

		/*
		 * Advance the temperatures.
		 */
		int lSwitchedOn = 0;
		for (TCL lTCL : this.fLoads)
		{
			boolean lDecision	= pSwitchDecisions.get(lTCL); 
			double lTemperature	= this.fCurrentTemperature.get(lTCL);

			if (lDecision) lSwitchedOn++;

			this.fPenalty = this.fPenalty + lTCL.computePenalty(lTemperature);
			lTCL.setStepSize(pStep);
			lTemperature  = lTCL.computeNextTemperature(lTemperature, lDecision);

			double lRand = 0;
			if (pRandom != null)
			{
				lRand = pVariance * pRandom.nextGaussian();
			}

			lTemperature = lTemperature + lRand;

			this.fCurrentTemperature.put(lTCL, lTemperature);
		}

		/*
		 * Compute the new average temperature.
		 */
		this.fAverageTemperature = 0;
		for (TCL lTCL : this.fLoads)
		{

			this.fAverageTemperature = this.fAverageTemperature + this.fCurrentTemperature.get(lTCL);
		}
		this.fAverageTemperature = this.fAverageTemperature / this.fLoads.size();

		/*
		 * Check the limit for violations.
		 */
		if (lSwitchedOn > lMinimumLimit)
		{
			System.err.println("WARNING: Overconsumption at step " + (this.fTime / pStep) + ", " + lSwitchedOn + " > " + lMinimumLimit);
		}

		/*
		 * Update the summed penalty.
		 */
		this.fPenaltySum   = this.fPenaltySum + this.fPenalty;
		this.fCurrentLimit = lFutureLimit;
		this.fTime		   = this.fTime + pStep;
	}

	public double getPenalty()
	{
		return this.fPenalty;
	}

	public double getPenaltySum()
	{
		return this.fPenaltySum;
	}

	public double getAverageTemperature()
	{
		return this.fAverageTemperature;
	}
}
