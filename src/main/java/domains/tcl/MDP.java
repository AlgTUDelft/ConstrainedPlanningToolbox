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

import java.util.Map;
import java.util.Set;

public interface MDP
{
	public int getHorizon();

	public int getNumAgents();

	public int getNumStates();

	public int getNumActions();

	public State getState(int pID);

	public Set<? extends Action> getConstrainedActions();

	public Set<? extends Action> getUnconstrainedActions();

	public Set<? extends Action> getActions();

	public Set<? extends Action> getActions(int pTime);

	public Set<? extends Agent> getAgents();

	public Map<? extends State, Double> getTransitionFunction(Agent p_i, State s_i, Action a_i);

	public Double getRewardFunction(Agent p_i, State s_i, Action a_i);

	public Integer getActionLimit(Action a_i, int t);

	public int getResourceLimit(int time);

	public int getResourceConsumption(Action action);
}
