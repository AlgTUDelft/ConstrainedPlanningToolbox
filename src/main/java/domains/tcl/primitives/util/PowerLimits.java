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

import java.util.HashMap;

public class PowerLimits extends HashMap<Integer, Integer>
{
	private static final long serialVersionUID = 8158289770671792637L;

	private final int fNumTCLs;

	public PowerLimits(int pNumTCLs)
	{
		this.fNumTCLs = pNumTCLs;
	}

	public int getLimit(int pTime)
	{
		int lLimit = this.fNumTCLs;

		if (this.get(pTime) != null)
		{
			lLimit = this.get(pTime);
		}

		return lLimit;
	}
}
