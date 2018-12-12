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

import java.util.HashSet;
import java.util.Set;

public class MultiTCLActionManager
{
	private MultiTCLAction[] actions;

	private Set<MultiTCLAction> all;
	private Set<MultiTCLAction> constrained;
	private Set<MultiTCLAction> unconstrained;

	public MultiTCLActionManager(int numActions)
	{
		actions = new MultiTCLAction[numActions];

		all           = new HashSet<MultiTCLAction>();
		constrained   = new HashSet<MultiTCLAction>();
		unconstrained = new HashSet<MultiTCLAction>();

		initializeActions(numActions);
	}

	private void initializeActions(int numActions)
	{
		for (int id = 0; id < numActions; id++)
		{
			actions[id] = new MultiTCLAction(id, numActions-1);

			all.add(actions[id]);

			if (id == 0) unconstrained.add(actions[id]);
			else         constrained.add(actions[id]);
		}
	}

	public int getNumActions()
	{
		return actions.length;
	}

	public MultiTCLAction getAction(int id)
	{
		return actions[id];
	}

	public Set<MultiTCLAction> getActionSet()
	{
		return all;
	}

	public Set<MultiTCLAction> getUnconstrainedSet()
	{
		return unconstrained;
	}

	public Set<MultiTCLAction> getConstrainedSet()
	{
		return constrained;
	}
}
