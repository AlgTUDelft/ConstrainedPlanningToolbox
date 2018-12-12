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
package domains.tcl.simpletcl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import domains.tcl.Action;
import domains.tcl.Agent;
import domains.tcl.MDP;
import domains.tcl.State;
import domains.tcl.primitives.NeighborhoodFactory;
import domains.tcl.primitives.TCL;
import domains.tcl.simpletcl.action.TCLAction;
import domains.tcl.simpletcl.action.TCLActionManager;
import domains.tcl.simpletcl.reward.TCLRewardFunction;
import domains.tcl.simpletcl.state.TCLState;
import domains.tcl.simpletcl.state.TCLStateManager;
import domains.tcl.simpletcl.transition.TransitionFunctions;


public class SimpleTCLDomain implements MDP
{
	// Simple elements of the system.
	private Set<TCLAgent>  fAgents;
	private TCLActionManager actions;
	private TCLStateManager  states;

	// Functions over the simple elements.
	private TransitionFunctions fTransitionFunction;
	private TCLRewardFunction rewards;

	// Action limitations.
	private Map<Integer, Integer> fActionLimits;

	// Parameters of the system.
	private final int fHorizon;

	public SimpleTCLDomain(double pMinTemperature, double pMaxTemperature, int pNumStates, 
						   double pOutside, int pSetpoint, int pPlanningStep, int pNumSteps)
	{
		this.fAgents = new HashSet<TCLAgent>();
		this.actions = new TCLActionManager();
		this.states  = new TCLStateManager(pMinTemperature, pMaxTemperature, pNumStates);

		this.fTransitionFunction	= new TransitionFunctions(actions, states, pOutside, pPlanningStep);
		this.rewards = new TCLRewardFunction(pSetpoint);
		this.fActionLimits			= new HashMap<Integer, Integer>();

		this.fHorizon				= pNumSteps / pPlanningStep;
	}

	public SimpleTCLDomain(NeighborhoodFactory pFactory,
						   double pMinTemperature, double pMaxTemperature, int pNumStates, 
						   int pPlanningStep, int pNumSteps)
	{
		this.fAgents = new HashSet<TCLAgent>();
		this.actions = new TCLActionManager();
		this.states  = new TCLStateManager(pMinTemperature, pMaxTemperature, pNumStates);

		this.fTransitionFunction	= new TransitionFunctions(actions, states, pFactory.getOutsideTemperature(), pPlanningStep);
		this.rewards = new TCLRewardFunction(pFactory.getSetPoint());
		this.fActionLimits			= new HashMap<Integer, Integer>();

		this.fHorizon				= pNumSteps / pPlanningStep;

		for (TCL lTCL : pFactory.getTCLs())
		{
			this.addAgent(lTCL);
		}

		Map<Integer, Integer> lLimits = pFactory.getLimits(pNumSteps, pPlanningStep);
		for (Integer lTime : lLimits.keySet())
		{
			this.setOnLimits(lTime, lLimits.get(lTime));
		}
	}

	protected void addAgent(TCL lTCL)
	{
		TCLAgent lNewAgent = new TCLAgent(lTCL);
		addAgent(lNewAgent);
	}

	private void addAgent(TCLAgent newAgent)
	{
		fAgents.add(newAgent);
		fTransitionFunction.initializeTransitionFunction(newAgent);
	}

	public void setOnLimits(int pTimeStep, int pLimit)
	{
		this.fActionLimits.put(pTimeStep, pLimit);
	}

	public void setLimits(Map<Integer, Integer> pLimits)
	{
		this.fActionLimits = pLimits;
	}

	public Map<Integer, Integer> getLimits()
	{
		return this.fActionLimits;
	}

	public TCLAction getOnAction()
	{
		return actions.getOn();
	}

	public TCLAction getOffAction()
	{
		return actions.getOff();
	}

	public TCLState findTemperatureState(double pTemperature)
	{
		return states.findTemperatureState(pTemperature);
	}

	@Override
	public int getHorizon()
	{
		return this.fHorizon;
	}

	@Override
	public int getNumAgents()
	{
		return this.fAgents.size();
	}

	@Override
	public int getNumStates()
	{
		return states.getNumStates();
	}

	@Override
	public int getNumActions()
	{
		return actions.getNumActions();
	}

	@Override
	public State getState(int pID)
	{
		return states.getState(pID);
	}

	@Override
	public Set<? extends Action> getConstrainedActions()
	{
		Set<TCLAction> constrained = new HashSet<TCLAction>();

		constrained.add(getOnAction());

		return constrained;
	}

	@Override
	public Set<? extends Action> getUnconstrainedActions()
	{
		Set<TCLAction> unconstrained = new HashSet<TCLAction>();

		unconstrained.add(getOffAction());

		return unconstrained;
	}

	@Override
	public Set<? extends Action> getActions()
	{
		return actions.getActions();
	}

	@Override
	public Set<? extends Action> getActions(int pTime)
	{
		return this.getActions();
	}

	@Override
	public Set<? extends Agent> getAgents()
	{
		return this.fAgents;
	}

	@Override
	public Map<TCLState, Double> getTransitionFunction(Agent p_i, State s_i, Action a_i)
	{
		return fTransitionFunction.getTransitionFunction(p_i, s_i, a_i);
	}

	@Override
	public Double getRewardFunction(Agent p_i, State s_i, Action a_i)
	{
		return rewards.getRewardFunction((TCLState) s_i);
	}

	@Override
	public Integer getActionLimit(Action a_i, int t)
	{
		int lDefaultLimit = this.fAgents.size();

		if (this.getOnAction().equals(a_i))
		{
			Integer lLimit = this.fActionLimits.get(t);

			if (lLimit != null)
				lDefaultLimit = lLimit;
		}

		return lDefaultLimit;
	}

	@Override
	public int getResourceLimit(int time)
	{
		Integer limit = fActionLimits.get(time);

		if (limit == null)
			limit = fAgents.size();

		return limit;
	}

	@Override
	public int getResourceConsumption(Action action)
	{
		return (getOnAction().equals(action)) ? 1 : 0;
	}
}
