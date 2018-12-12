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

from util.ProbabilitySample import ProbabilitySample
from util.ToolboxServer import ToolboxServer

class CMDPSimulator:

    def __init__(self, instance):
        self.instance = instance
        self.mean_total_cost = []
        self.mean_instantaneous_cost = []
        self.violation_prob_estimate_total = []
        self.violation_prob_estimate_instantaneous = []

    def run(self, num_runs):
        ToolboxServer.send_request("startSimulation")

        cmdps = self.instance.cmdps
        num_agents = self.instance.get_num_agents()
        num_decisions = self.instance.num_decisions
        num_domain_resources = self.instance.num_domain_resources

        mean_reward = 0.0
        self.mean_total_cost = [0.0 for k in range(num_domain_resources)]
        self.mean_instantaneous_cost = [[0.0 for t in range(num_decisions)] for k in range(num_domain_resources)]
        num_total_violations = [0 for k in range(num_domain_resources)]
        num_instantaneous_violations = [[0 for t in range(num_decisions)] for k in range(num_domain_resources)]

        for run in range(num_runs):
            run_reward = 0.0
            run_total_cost = [0.0 for k in range(num_domain_resources)]
            run_instantaneous_cost = [[0.0 for t in range(num_decisions)] for k in range(num_domain_resources)]

            # get initial states
            state = [0 for i in range(num_agents)]
            for i in range(num_agents):
                state[i] = cmdps[i].initial_state

            for t in range(num_decisions):
                # ask server for actions and convert to list with ints
                actions = ToolboxServer.send_request("getActionsCMDP_{0}_{1}".format(t, state))
                actions = actions.split()
                for i in range(len(actions)):
                    actions[i] = int(actions[i])

                # execute the actions and sample next states
                for i in range(num_agents):
                    cmdp = cmdps[i]
                    s = state[i]
                    a = actions[i]

                    run_reward += cmdp.get_time_reward(t, s, a)
                    for k in range(num_domain_resources):
                        run_total_cost[k] += cmdp.get_cost(k, s, a)
                        run_instantaneous_cost[k][t] += cmdp.get_cost(k, s, a)

                    destinations = cmdp.get_time_transition_destinations(t, s, a)
                    probabilities = cmdp.get_time_transition_probabilities(t, s, a)

                    ps = ProbabilitySample(destinations, probabilities)
                    state[i] = ps.sample_item()


            mean_reward += (run_reward / num_runs)
            for k in range(num_domain_resources):
                self.mean_total_cost[k] += run_total_cost[k] / num_runs

                if self.instance.use_budget_constraints and run_total_cost[k] > self.instance.get_cost_limit_budget(k):
                    num_total_violations[k] += 1

                for t in range(num_decisions):
                    self.mean_instantaneous_cost[k][t] += run_instantaneous_cost[k][t] / num_runs
                    if (not self.instance.use_budget_constraints) and run_instantaneous_cost[k][t] > self.instance.get_cost_limit_instantaneous(k, t):
                        num_instantaneous_violations[k][t] += 1

        # compute violation probability estimates
        self.violation_prob_estimate_total = [0.0 for k in range(num_domain_resources)]
        self.violation_prob_estimate_instantaneous = [[0.0 for t in range(num_decisions)] for k in range(num_domain_resources)]
        for k in range(num_domain_resources):
            self.violation_prob_estimate_total[k] = num_total_violations[k] / num_runs

            for t in range(num_decisions):
                self.violation_prob_estimate_instantaneous[k][t] = num_instantaneous_violations[k][t] / num_runs

        return mean_reward


    def get_mean_instantaneous_cost(self, k, t):
        return self.mean_instantaneous_cost[k][t]

    def get_mean_total_cost(self, k):
        return self.mean_total_cost[k]

    def get_violation_probability_estimate_total(self, k):
        return self.violation_prob_estimate_total[k]

    def get_violation_probability_estimate_instantaneous(self, k, t):
        return self.violation_prob_estimate_instantaneous[k][t]
