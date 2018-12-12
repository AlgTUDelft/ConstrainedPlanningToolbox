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

from instances import XMLInstanceManager
from util.ToolboxServer import ToolboxServer
from algorithms.SolveException import SolveException

def solve(instance):
    XMLInstanceManager.write_cmdp_instance(instance, "pythonInstance.xml")
    expected_reward = ToolboxServer.send_request("solveXMLDomainMDP_colgen")

    if expected_reward == "EXCEPTION":
        raise SolveException("Problem could not be solved by server")

    return expected_reward
