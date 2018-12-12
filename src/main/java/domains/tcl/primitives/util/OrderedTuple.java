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
package domains.tcl.primitives.util;


public class OrderedTuple implements Comparable<OrderedTuple>
{
	private final int fTime;

	private final int fLimit;

	public OrderedTuple(int pTime, int pLimit)
	{
		this.fTime	= pTime;
		this.fLimit = pLimit;
	}

	public int getTime()
	{
		return this.fTime;
	}

	public int getLimit()
	{
		return this.fLimit;
	}

	@Override
	public int compareTo(OrderedTuple pOther)
	{
		int lDiff = this.getTime() - pOther.getTime();

		if (lDiff == 0)
		{
			lDiff = pOther.getLimit() - this.getLimit();
		}

		return lDiff;
	}

	@Override
	public boolean equals(Object pOther)
	{
		if (pOther instanceof OrderedTuple)
		{
			OrderedTuple pThat = (OrderedTuple) pOther;

			return (this.compareTo(pThat) == 0);
		}

		return false;
	}

	@Override
	public String toString()
	{
		return String.format("OrderedTuple(%d, %d)", this.getTime(), this.getLimit());
	}

}
