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


class CPOMDPInstance:

    def __init__(self, cpomdps, num_decisions):
        self.cpomdps = cpomdps
        self.cost_limits = None
        self.num_decisions = num_decisions
        self.num_domain_resources = -1

    def set_cost_limits(self, limits):
        self.cost_limits = limits
        self.num_domain_resources = len(limits)

    def get_cost_limit(self, k):
        return self.cost_limits[k]

    def get_num_agents(self):
        return len(self.cpomdps)
