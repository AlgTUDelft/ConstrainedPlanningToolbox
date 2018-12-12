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

class CPOMDPSimulator:

    def __init__(self, instance):
        self.instance = instance
        self.mean_total_cost = []
        self.mean_instantaneous_cost = []
        self.violation_prob_estimate_total = []

    def run(self, num_runs):
        ToolboxServer.send_request("startSimulation")

        cpomdps = self.instance.cpomdps
        num_agents = self.instance.get_num_agents()
        num_decisions = self.instance.num_decisions
        num_domain_resources = self.instance.num_domain_resources

        mean_reward = 0.0
        self.mean_total_cost = [0.0 for k in range(num_domain_resources)]
        self.mean_instantaneous_cost = [[0.0 for t in range(num_decisions)] for k in range(num_domain_resources)]
        num_total_violations = [0 for k in range(num_domain_resources)]

        for run in range(num_runs):
            run_reward = 0.0
            run_total_cost = [0.0 for k in range(num_domain_resources)]
            run_instantaneous_cost = [[0.0 for t in range(num_decisions)] for k in range(num_domain_resources)]

            # sample initial states
            state = [0 for i in range(num_agents)]
            beliefs = [None for i in range(num_agents)]
            for i in range(num_agents):
                cpomdp = cpomdps[i]
                ps = ProbabilitySample([], [])
                for s in range(cpomdp.num_states):
                    ps.add_item(s, cpomdp.initial_belief.belief[s])
                state[i] = ps.sample_item()
                beliefs[i] = cpomdp.initial_belief

            for t in range(num_decisions):
                # select actions
                actions = ToolboxServer.send_request("getActionsCPOMDP_{0}".format(t))
                actions = actions.split()
                for i in range(len(actions)):
                    actions[i] = int(actions[i])

                observations = [0 for i in range(num_agents)]

                for i in range(num_agents):
                    s = state[i]
                    b = beliefs[i]
                    a = actions[i]

                    run_reward += cpomdp.get_time_reward(t, s, a)
                    for k in range(num_domain_resources):
                        run_total_cost[k] += cpomdp.get_cost(k, s, a)
                        run_instantaneous_cost[k][t] += cpomdp.get_cost(k, s, a)

                    # sample next state
                    destinations = cpomdp.get_time_transition_destinations(t, s, a)
                    probabilities = cpomdp.get_time_transition_probabilities(t, s, a)

                    ps = ProbabilitySample(destinations, probabilities)
                    s_next = ps.sample_item()

                    # sample observation
                    ps = ProbabilitySample([], [])
                    for o in range(cpomdp.num_observations):
                        prob = cpomdp.get_observation_probability(a, s_next, o)
                        ps.add_item(o, prob)
                    o = ps.sample_item()
                    observations[i] = o

                    # update the belief state
                    bao = cpomdp.update_belief(b, a, o)

                    # prepare for next step
                    state[i] = s_next
                    beliefs[i] = bao

                # communicate observations back to server
                ToolboxServer.send_request("actionObservations_{0}_{1}".format(actions, observations))


            mean_reward += (run_reward / num_runs)
            for k in range(num_domain_resources):
                self.mean_total_cost[k] += run_total_cost[k] / num_runs

                if run_total_cost[k] > self.instance.get_cost_limit(k):
                    num_total_violations[k] += 1

                for t in range(num_decisions):
                    self.mean_instantaneous_cost[k][t] += run_instantaneous_cost[k][t] / num_runs

        # compute violation probability estimates
        self.violation_prob_estimate_total = [0.0 for k in range(num_domain_resources)]
        self.violation_prob_estimate_instantaneous = [[0.0 for t in range(num_decisions)] for k in range(num_domain_resources)]
        for k in range(num_domain_resources):
            self.violation_prob_estimate_total[k] = num_total_violations[k] / num_runs

        return mean_reward


    def get_mean_instantaneous_cost(self, k, t):
        return self.mean_instantaneous_cost[k][t]

    def get_mean_total_cost(self, k):
        return self.mean_total_cost[k]

    def get_violation_probability_estimate_total(self, k):
        return self.violation_prob_estimate_total[k]

