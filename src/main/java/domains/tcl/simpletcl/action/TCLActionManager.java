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

import java.util.HashSet;
import java.util.Set;

public class TCLActionManager
{
	private Set<TCLAction> actions;

	private TCLAction off;
	private TCLAction on;

	public TCLActionManager()
	{
		initializeActions();
		initializeSet();
	}

	private void initializeActions()
	{
		off = new TCLAction(false);
		on  = new TCLAction(true);
	}

	private void initializeSet()
	{
		actions = new HashSet<TCLAction>();
		actions.add(off);
		actions.add(on);
	}

	public TCLAction getOff()
	{
		return off;
	}

	public TCLAction getOn()
	{
		return on;
	}

	public int getNumActions()
	{
		return actions.size();
	}

	public Set<TCLAction> getActions()
	{
		return actions;
	}
}
