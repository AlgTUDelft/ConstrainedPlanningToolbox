#################################################################################
# ConstrainedPlanningToolbox
# Copyright (C) 2019 Algorithmics group, Delft University of Technology
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#################################################################################


from model.CMDP import CMDP
from model.CPOMDP import CPOMDP
from model.BeliefPoint import BeliefPoint
from instances.CMDPInstance import CMDPInstance
from instances.CPOMDPInstance import CPOMDPInstance
from util.ToolboxServer import ToolboxServer
from algorithms.mdp.constrainedmdp import ConstrainedMDPFiniteHorizon
from algorithms.pomdp.cgcp import CGCP

num_states = 2
num_actions = 2
num_decisions = 10
initial_state = 0

# create reward function
reward_function = [[0.0 for a in range(num_actions)] for s in range(num_states)]
reward_function[1][1] = 10.0

# create transition function
transition_destinations = [[[] for a in range(num_actions)] for s in range(num_states)]
transition_probabilities = [[[] for a in range(num_actions)] for s in range(num_states)]

transition_destinations[0][0].append(0)
transition_probabilities[0][0].append(1.0)

transition_destinations[0][1].append(0)
transition_destinations[0][1].append(1)
transition_probabilities[0][1].append(0.1)
transition_probabilities[0][1].append(0.9)

# define one cost function
num_cost_functions = 1
cost_function = [[[0.0 for a in range(num_actions)] for s in range(num_states)] for k in range(num_cost_functions)]
cost_function[0][1][1] = 2.0

# define cost limits
limits = [0.5]

# create CMDP
cmdp = CMDP(num_states, num_actions, initial_state, num_decisions)
cmdp.set_reward_function(reward_function)
cmdp.set_transitions(transition_destinations, transition_probabilities)
cmdp.set_cost_functions(cost_function)

# create instance
cmdps = []
cmdps.append(cmdp)
cmdp_instance = CMDPInstance(cmdps, num_decisions)
cmdp_instance.set_cost_limits_budget(limits)

# solve the problem
ToolboxServer.connect()
expected_reward = ConstrainedMDPFiniteHorizon.solve(cmdp_instance)
print("Expected reward:", expected_reward)

# extend to POMDP
num_observations = 2
observation_function = [[[0.0 for o in range(num_observations)] for s in range(num_states)] for a in range(num_actions)]
for a in range(num_actions):
    for s_next in range(num_states):
        o = s_next
        observation_function[a][s_next][o] = 1.0

b0 = BeliefPoint([1.0, 0.0])

cpomdp = CPOMDP(num_states, num_actions, num_observations, b0, num_decisions)
cpomdp.set_reward_function(reward_function)
cpomdp.set_transitions(transition_destinations, transition_probabilities)
cpomdp.set_cost_functions(cost_function)
cpomdp.set_observation_function(observation_function)

cpomdps = []
cpomdps.append(cpomdp)
cpomdp_instance = CPOMDPInstance(cpomdps, num_decisions)
cpomdp_instance.set_cost_limits(limits)


expected_reward = CGCP.solve(cpomdp_instance)
print("Expected reward:", expected_reward)

ToolboxServer.disconnect()
