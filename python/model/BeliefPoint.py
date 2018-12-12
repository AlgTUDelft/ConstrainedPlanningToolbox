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


class BeliefPoint:

    def __init__(self, belief):
        self.belief = belief
        self.action_observation_probs_initialized = False
        self.action_observation_probs = []

    def has_action_observation_probs(self):
        return self.action_observation_probs_initialized

    def get_action_observation_probability(self, a, o):
        assert self.action_observation_probs_initialized
        return self.action_observation_probs[a][o]

    def set_action_observation_probabilities(self, probs):
        self.action_observation_probs = probs
        self.action_observation_probs_initialized = True
