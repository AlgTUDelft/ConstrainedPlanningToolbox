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

import domains.tcl.State;



public class TCLState extends State
{
	private static final long serialVersionUID = -3518024258027640373L;

	private final int fID;

	private final double fMinTempRange;
	private final double fMaxTempRange;

	private TCLState fNextLower;
	private TCLState fNextHigher;

	public TCLState(int pID, String pLabel, double pMinRange, double pMaxRange)
	{
		super(pLabel);

		this.fID = pID;

		this.fMinTempRange = pMinRange;
		this.fMaxTempRange = pMaxRange;

		this.fNextLower  = null;
		this.fNextHigher = null;
	}

	@Override
	public int getID()
	{
		return this.fID;
	}

	public double getReward(int pSetPoint)
	{
		double lMinDiff =  Math.min(30, Math.max(0, Math.abs(this.getMinTemp() - pSetPoint) - 0.5));
		double lMaxDiff =  Math.min(30, Math.max(0, Math.abs(this.getMaxTemp() - pSetPoint) - 0.5));
		double lReward  = -Math.max(lMinDiff * lMinDiff, lMaxDiff * lMaxDiff);

		lReward = (double) Math.round(lReward * 10000) / 10000;

		return lReward;
	}

	public void setPrevious(TCLState pPrevious)
	{
		this.fNextLower  = pPrevious;
	}

	public void setNext(TCLState pNext)
	{
		this.fNextHigher = pNext;
	}

	public TCLState getPrevious()
	{
		return this.fNextLower;
	}

	public TCLState getNext()
	{
		return this.fNextHigher;
	}

	public double getMinTemp()
	{
		return this.fMinTempRange;
	}

	public double getMaxTemp()
	{
		return this.fMaxTempRange;
	}

	public boolean isInRange(double pTempValue)
	{
		return (pTempValue >= this.getMinTemp() && pTempValue < this.getMaxTemp());
	}

	@Override
	public int hashCode()
	{
		return this.getID();
	}

	@Override
	public boolean equals(Object that)
	{
		boolean equal = false;

		if (that != null && that instanceof TCLState)
		{
			equal = this.equals((TCLState) that);
		}

		return equal;
	}

	private boolean equals(TCLState that)
	{
		return ((this.getID()      == that.getID())      &&
				(this.getMinTemp() == that.getMinTemp()) &&
				(this.getMaxTemp() == that.getMaxTemp()));
	}
}
