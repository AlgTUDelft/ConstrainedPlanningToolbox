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

from util.ToolboxServer import ToolboxServer
from instances import XMLInstanceManager


def get_advertising_instance(num_agents, num_decisions):
    ToolboxServer.send_request("dumpDefaultDomain_advertising_{0}_{1}".format(num_agents, num_decisions))
    instance = XMLInstanceManager.read_cmdp_instance("javaInstance.xml")
    return instance


def get_maze_instance(num_agents, num_decisions):
    ToolboxServer.send_request("dumpDefaultDomain_maze_{0}_{1}".format(num_agents, num_decisions))
    instance = XMLInstanceManager.read_cmdp_instance("javaInstance.xml")
    return instance


def get_tcl_fixed_limit_instance(num_agents, num_decisions):
    ToolboxServer.send_request("dumpDefaultDomain_tclFixedLimit_{0}_{1}".format(num_agents, num_decisions))
    instance = XMLInstanceManager.read_cmdp_instance("javaInstance.xml")
    return instance


def get_tcl_multi_level_instance(num_agents, num_decisions):
    ToolboxServer.send_request("dumpDefaultDomain_tclMultiLevel_{0}_{1}".format(num_agents, num_decisions))
    instance = XMLInstanceManager.read_cmdp_instance("javaInstance.xml")
    return instance


def get_cbm_instance(num_agents, num_decisions):
    ToolboxServer.send_request("dumpDefaultDomain_cbm_{0}_{1}".format(num_agents, num_decisions))
    instance = XMLInstanceManager.read_cpomdp_instance("javaInstance.xml")
    return instance


def get_webad_instance(num_agents, num_decisions):
    ToolboxServer.send_request("dumpDefaultDomain_webad_{0}_{1}".format(num_agents, num_decisions))
    instance = XMLInstanceManager.read_cpomdp_instance("javaInstance.xml")
    return instance
