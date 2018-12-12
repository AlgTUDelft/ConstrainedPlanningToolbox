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
package domains.tcl.simpletcl.action;

import domains.tcl.Action;

public class TCLAction implements Action
{
	private final boolean fAction;

	public TCLAction(boolean pAction)
	{
		this.fAction = pAction;
	}

	public boolean isOn()
	{
		return this.fAction;
	}

	@Override
	public int getID()
	{
		return (this.isOn() ? 1 : 0);
	}

	@Override
	public int hashCode()
	{
		return this.getID();
	}

	@Override
	public boolean equals(Object other)
	{
		boolean equal = false;

		if ((other != null) && (other instanceof TCLAction))
		{
			equal = this.equals((TCLAction) other);
		}

		return equal;
	}

	private boolean equals(TCLAction that)
	{
		return (this.getID() == that.getID()) &&
			   (this.isOn()  == that.isOn());
	}

	@Override
	public String toString()
	{
		return "<" + Integer.toString(getID()) + ", " + (isOn() ? "ON" : "OFF") + ">";
	}
}
