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
package domains.tcl;

import java.io.Serializable;

public abstract class State implements Serializable, Identifiable
{
	private static final long serialVersionUID = 2571920331097241305L;

	private final String fLabel;

	public State(String pLabel)
	{
		this.fLabel = pLabel;
	}

	@Override
	public int hashCode()
	{
		return this.getID();
	}

	@Override
	public String toString()
	{
		return String.format("State(%3d - %s)", this.getID(), this.fLabel);
	}
}
