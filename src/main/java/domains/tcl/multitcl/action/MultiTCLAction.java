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
package domains.tcl.multitcl.action;

import domains.tcl.Action;


public class MultiTCLAction implements Action
{
	private final int onFraction;
	private final int onMaximum;

	public MultiTCLAction(int on, int max)
	{
		onFraction = on;
		onMaximum  = max;
	}

	@Override
	public int getID()
	{
		return getOnFraction();
	}

	public int getOnFraction()
	{
		return onFraction;
	}

	public int getOnMaximum()
	{
		return onMaximum;
	}

	public boolean isOn()
	{
		return (getOnFraction() > 0);
	}

	@Override
	public int hashCode()
	{
		return getID();
	}

	@Override
	public boolean equals(Object that)
	{
		boolean equal = false;

		if (that instanceof MultiTCLAction)
		{
			equal = isEqual((MultiTCLAction) that);
		}

		return equal;
	}

	private boolean isEqual(MultiTCLAction that)
	{
		return ((this.getOnFraction() == that.getOnFraction()) 
			 && (this.getOnMaximum()  == that.getOnMaximum()));
	}
}
