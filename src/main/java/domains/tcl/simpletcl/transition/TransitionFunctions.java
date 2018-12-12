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
package domains.tcl.simpletcl.transition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import domains.tcl.Action;
import domains.tcl.Agent;
import domains.tcl.State;
import domains.tcl.simpletcl.TCLAgent;
import domains.tcl.simpletcl.action.TCLActionManager;
import domains.tcl.simpletcl.state.TCLState;
import domains.tcl.simpletcl.state.TCLStateManager;


public class TransitionFunctions
{
	// Functions over the simple elements.
	private List<TransitionFunction> transitionFunctions;

	private TCLActionManager actions;
	private TCLStateManager  states;

	private final double fOutsideTemperature;
	private final int    fPlanningStep;

	public TransitionFunctions(TCLActionManager actions, TCLStateManager states, double pOutside, int pPlanningStep)
	{
		transitionFunctions = new ArrayList<TransitionFunction>();

		this.actions = actions;
		this.states  = states;

		this.fOutsideTemperature  = pOutside;
		this.fPlanningStep		  = pPlanningStep;		
	}

	public void initializeTransitionFunction(TCLAgent pAgent)
	{
		transitionFunctions.add(new TransitionFunction(pAgent, actions, states, fOutsideTemperature, fPlanningStep));
	}

	public Map<TCLState, Double> getTransitionFunction(Agent p_i, State s_i, Action a_i)
	{
		return transitionFunctions.get(p_i.getID()).getTransitionFunction(s_i, a_i);
	}
}
