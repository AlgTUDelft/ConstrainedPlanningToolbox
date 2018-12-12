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
package domains.tcl.simpletcl.state;

import java.util.ArrayList;
import java.util.List;

public class TCLStateManager
{
	private List<TCLState> states;

	// State ID counter.
	private int fNextStateID;
	private final double fMinTemperature;
	private final double fStepSize;

	public TCLStateManager(double pMinTemperature, double pMaxTemperature, int pNumStates)
	{
		this.states			 = new ArrayList<TCLState>();
		this.fNextStateID	 = 0;
		this.fMinTemperature = pMinTemperature;
		this.fStepSize		 = (pMaxTemperature - pMinTemperature) / pNumStates;

		this.initializeStates(pNumStates);
	}

	private void initializeStates(int pNumStates)
	{
		TCLState lPrevious = new TCLState(this.fNextStateID++, String.format("Under %.3f", this.fMinTemperature), Double.NEGATIVE_INFINITY, this.fMinTemperature);
		states.add(lPrevious);

		double lCurrentValue = this.fMinTemperature;
		for (int i = 0; i < pNumStates; i++)
		{
			double lMinRange = lCurrentValue;
			lCurrentValue    = lCurrentValue + this.fStepSize;
			double lMaxRange = lCurrentValue;

			TCLState lCurrent = new TCLState(this.fNextStateID++, String.format("%.3f to %.3f", lMinRange, lMaxRange), lMinRange, lMaxRange);
			lPrevious.setNext(lCurrent);
			lCurrent.setPrevious(lPrevious);
			states.add(lCurrent);

			lPrevious = lCurrent;
		}

		TCLState lFinal = new TCLState(this.fNextStateID++, String.format("Over %.3f", lCurrentValue), lCurrentValue, Double.POSITIVE_INFINITY);
		lPrevious.setNext(lFinal);
		lFinal.setPrevious(lPrevious);
		states.add(lFinal);
	}

	public TCLState findTemperatureState(double pTemperature)
	{
		int lLow  = 0;
		int lHigh = getNumStates();
		int lPos  = (lHigh + lLow) / 2;

		while (!getState(lPos).isInRange(pTemperature))
		{
			if (getState(lPos).getMaxTemp() < pTemperature)
			{
				lLow  = lPos;
				lPos  = (lHigh + lLow) / 2;
			}
			else
			{
				lHigh = lPos;
				lPos  = (lHigh + lLow) / 2;
			}
		}

		return getState(lPos);
	}

	public int getNumStates()
	{
		return states.size();
	}

	public TCLState getState(int pID)
	{
		return states.get(pID);
	}

	public List<TCLState> getStates()
	{
		return states;
	}
}
