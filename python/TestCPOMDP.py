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

from evaluation.CPOMDPSimulator import CPOMDPSimulator
from util.ToolboxServer import ToolboxServer
from instances import InstanceGenerator
from algorithms.pomdp.cgcp import CGCP
from algorithms.SolveException import SolveException

ToolboxServer.connect()


# get instance
num_agents = 2
num_decisions = 7
instance = InstanceGenerator.get_cbm_instance(num_agents, num_decisions)


try:
    # solve instance
    expected_reward = CGCP.solve(instance)
    print("Expected reward:", expected_reward)
    print("Cost limit: ", instance.get_cost_limit(0))

    # evaluate instance
    sim = CPOMDPSimulator(instance)
    mean_reward = sim.run(1000)
    print("Mean reward:", mean_reward)
    print("Mean cost:", sim.get_mean_total_cost(0))

except SolveException as e:
    print("Error occurred while solving the instance")


ToolboxServer.disconnect()

