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
package domains.tcl.primitives.generator.limit;

import java.util.List;

public class MultiActionLimitGen extends AlternatingRatioGen
{
	private int numActions;
	private int max;

	public MultiActionLimitGen(int numActions)
	{
		setNumActions(numActions);
	}

	public void setNumActions(int numActions)
	{
		this.numActions = numActions;
	}

	@Override
	public List<Integer> generateLimits(int numAgents, int horizon)
	{
		max = numAgents * (numActions-1);

		return super.generateLimits(numAgents, horizon);
	}

	@Override
	public int getMaximumConsumption() {
		return max;
	}

	@Override
	public String getName()
	{
		return "MultiActionAlternatingLimit";
	}
}
