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


class CMDP:
    def __init__(self, num_states, num_actions, initial_state, num_decisions):
        self.num_states = num_states
        self.num_actions = num_actions
        self.initial_state = initial_state
        self.num_decisions = num_decisions

        self.rewards_defined = False
        self.has_time_dependent_reward = False
        self.reward_function = None
        self.time_reward_function = None

        self.transitions_defined = False
        self.has_time_dependent_transitions = False
        self.transition_destinations = None
        self.transition_probabilities = None
        self.time_transition_destinations = None
        self.time_transition_probabilities = None

        self.cost_function_defined = False
        self.cost_function = None

        self.feasible_actions = None
        self.init_default_feasible_actions()

    def set_reward_function(self, reward_function):
        self.rewards_defined = True
        self.has_time_dependent_reward = False
        self.reward_function = reward_function
        self.time_reward_function = None

    def set_time_reward_function(self, time_reward_function):
        self.rewards_defined = True
        self.has_time_dependent_reward = True
        self.reward_function = None
        self.time_reward_function = time_reward_function

    def get_reward(self, s, a):
        assert not self.has_time_dependent_reward
        return self.reward_function[s][a]

    def get_time_reward(self, t, s, a):
        if self.has_time_dependent_reward:
            return self.time_reward_function[t][s][a]
        else:
            return self.reward_function[s][a]

    def set_transitions(self, destinations, probabilities):
        self.transitions_defined = True
        self.has_time_dependent_transitions = False
        self.transition_destinations = destinations
        self.transition_probabilities = probabilities

    def set_time_transitions(self, destinations, probabilities):
        self.transitions_defined = True
        self.has_time_dependent_transitions = True
        self.time_transition_destinations = destinations
        self.time_transition_probabilities = probabilities

    def get_transition_destinations(self, s, a):
        assert not self.has_time_dependent_transitions
        return self.transition_destinations[s][a]

    def get_transition_probabilities(self, s, a):
        assert not self.has_time_dependent_transitions
        return self.transition_probabilities[s][a]

    def get_time_transition_destinations(self, t, s, a):
        if self.has_time_dependent_transitions:
            return self.time_transition_destinations[t][s][a]
        else:
            return self.transition_destinations[s][a]

    def get_time_transition_probabilities(self, t, s, a):
        if self.has_time_dependent_transitions:
            return self.time_transition_probabilities[t][s][a]
        else:
            return self.transition_probabilities[s][a]

    def init_default_feasible_actions(self):
        self.feasible_actions = [[[] for s in range(self.num_states)] for t in range(self.num_decisions)]
        for t in range(self.num_decisions):
            for s in range(self.num_states):
                for a in range(self.num_actions):
                    self.feasible_actions[t][s].append(a)

    def get_feasible_actions(self, t, s):
        return self.feasible_actions[t][s]

    def set_feasible_actions(self, feasible_actions):
        self.feasible_actions = feasible_actions

    def get_cost(self, k, s, a):
        assert self.cost_function_defined
        return self.cost_function[k][s][a]

    def get_num_domain_resources(self):
        assert self.cost_function_defined
        return len(self.cost_function)

    def set_cost_functions(self, cost_function):
        self.cost_function_defined = True
        self.cost_function = cost_function

