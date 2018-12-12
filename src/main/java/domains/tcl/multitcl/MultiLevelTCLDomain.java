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
package domains.tcl.multitcl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import domains.tcl.Action;
import domains.tcl.Agent;
import domains.tcl.MDP;
import domains.tcl.State;
import domains.tcl.multitcl.action.MultiTCLAction;
import domains.tcl.multitcl.action.MultiTCLActionManager;
import domains.tcl.multitcl.transition.MultiTransition;
import domains.tcl.primitives.NeighborhoodFactory;
import domains.tcl.primitives.TCL;
import domains.tcl.simpletcl.TCLAgent;
import domains.tcl.simpletcl.reward.TCLRewardFunction;
import domains.tcl.simpletcl.state.TCLState;
import domains.tcl.simpletcl.state.TCLStateManager;


public class MultiLevelTCLDomain implements MDP
{
	// Simple elements of the system.
	private Set<TCLAgent>  fAgents;
	private MultiTCLActionManager actions;
	private TCLStateManager  states;

	// Functions over the simple elements.
	private List<MultiTransition> transitionFunctions;
	private TCLRewardFunction rewards;

	// Action limitations.
	private Map<Integer, Integer> fActionLimits;

	// Parameters of the system.
	private final int fHorizon;

	// Parameters of the environment.
	private final double outTemp;
	private final int    transitionDelta;

	public MultiLevelTCLDomain(NeighborhoodFactory pFactory,
						   double pMinTemperature, double pMaxTemperature, int pNumStates, 
						   int pPlanningStep, int pNumSteps, int numActions)
	{
		this.fAgents = new HashSet<TCLAgent>();
		this.actions = new MultiTCLActionManager(numActions);
		this.states  = new TCLStateManager(pMinTemperature, pMaxTemperature, pNumStates);

		this.transitionFunctions = new ArrayList<MultiTransition>();
		this.rewards             = new TCLRewardFunction(pFactory.getSetPoint());
		this.fActionLimits		 = new HashMap<Integer, Integer>();

		this.fHorizon		 = pNumSteps / pPlanningStep;
		this.outTemp         = pFactory.getOutsideTemperature();
		this.transitionDelta = pPlanningStep;

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
		transitionFunctions.add(new MultiTransition(newAgent, actions, states, outTemp, transitionDelta));
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

	public TCLState findTemperatureState(double pTemperature)
	{
		return states.findTemperatureState(pTemperature);
	}

	public Map<Agent, State> getInitialState(int setpoint)
	{
		Map<Agent, State> initial = new HashMap<Agent, State>();

		for (Agent agent : fAgents)
		{
			initial.put(agent, findTemperatureState(setpoint));
		}

		return initial;
	}

	@Override
	public int getHorizon()
	{
		return this.fHorizon;
	}

	@Override
	public int getNumAgents()
	{
		return fAgents.size();
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
		return actions.getConstrainedSet();
	}

	@Override
	public Set<? extends Action> getUnconstrainedActions()
	{
		return actions.getUnconstrainedSet();
	}

	@Override
	public Set<? extends Action> getActions()
	{
		return actions.getActionSet();
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
		return transitionFunctions.get(p_i.getID()).getTransitionFunction(s_i, a_i);
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

		if (((MultiTCLAction) a_i).getOnFraction() > 0)
		{
			Integer lLimit = this.fActionLimits.get(t);

			if (lLimit != null)
				lDefaultLimit = lLimit;
		}

		return lDefaultLimit;
	}

	@Override
	public int getResourceLimit(int t)
	{
		Integer limit = this.fActionLimits.get(t);

		if (limit == null)
		{
			limit = fAgents.size();
		}

		return limit;
	}

	@Override
	public int getResourceConsumption(Action a_i)
	{
		return ((MultiTCLAction) a_i).getOnFraction();
	}
}
