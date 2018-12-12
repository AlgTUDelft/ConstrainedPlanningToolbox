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

from model.BeliefPoint import BeliefPoint

class CPOMDP:
    def __init__(self, num_states, num_actions, num_observations, initial_belief, num_decisions):
        self.num_states = num_states
        self.num_actions = num_actions
        self.num_observations = num_observations
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

        self.observation_function = None
        self.initial_belief = initial_belief
        self.initial_state = 0

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

    def set_observation_function(self, observation_function):
        self.observation_function = observation_function

    def get_observation_probability(self, a, s_next, o):
        return self.observation_function[a][s_next][o]

    def prepare_belief(self, belief_point):
        if belief_point.has_action_observation_probs():
            return

        ao_probs = [[0.0 for o in range(self.num_observations)] for a in range(self.num_actions)]

        for a in range(self.num_actions):
            for o in range(self.num_observations):
                prob = 0.0
                for s in range(self.num_states):
                    destinations = self.get_transition_destinations(s, a)
                    probabilities = self.get_transition_probabilities(s, a)
                    for i in range(len(destinations)):
                        s_next = destinations[i]
                        s_next_prob = probabilities[i]
                        prob += self.get_observation_probability(a, s_next, o) * s_next_prob * belief_point.belief[s]
                ao_probs[a][o] = prob

        belief_point.set_action_observation_probabilities(ao_probs)

    def update_belief(self, belief_point, a, o):
        new_belief = [0.0 for s in range(self.num_states)]

        if not belief_point.has_action_observation_probs():
            self.prepare_belief(belief_point)

        nc = belief_point.get_action_observation_probability(a, o)

        for s in range(self.num_states):
            destinations = self.get_transition_destinations(s, a)
            probabilities = self.get_transition_probabilities(s, a)
            for i in range(len(destinations)):
                s_next = destinations[i]
                s_next_prob = probabilities[i]
                new_belief[s_next] += self.get_observation_probability(a, s_next, o) * s_next_prob * (1.0 / nc) * belief_point.belief[s]

        return BeliefPoint(new_belief)
