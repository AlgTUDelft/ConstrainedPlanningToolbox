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

import xml.etree.ElementTree as et
from model.CMDP import CMDP
from model.CPOMDP import CPOMDP
from instances.CMDPInstance import CMDPInstance
from instances.CPOMDPInstance import CPOMDPInstance
from model.BeliefPoint import BeliefPoint

def read_reward_function(reward_function, s, root):
    a = 0
    for item in root:
        reward_function[s][a] = float(item.text)
        a += 1


def read_transition_destinations(transition_destinations, s, root):
    a = 0
    for item in root:
        dests = []
        for dest in item:
            dests.append(int(dest.text))
        transition_destinations[s][a] = dests
        a += 1


def read_transition_probabilities(transition_probabilities, s, root):
    a = 0
    for item in root:
        probs = []
        for prob in item:
            probs.append(float(prob.text))
            transition_probabilities[s][a] = probs
        a += 1

def read_time_transition_destinations(time_transition_destinations, t, root):
    s = 0
    for ii in root:
        a = 0
        for item in ii:
            dests = []
            for dest in item:
                dests.append(int(dest.text))
                time_transition_destinations[t][s][a] = dests
            a += 1
        s+=1


def read_time_transition_probabilities(time_transition_probabilities, t, root):
    s = 0
    for ii in root:
        a = 0
        for item in ii:
            probs = []
            for prob in item:
                probs.append(float(prob.text))
                time_transition_probabilities[t][s][a] = probs
            a += 1
        s += 1



def read_time_reward_function(time_reward_function, t, root):
    s = 0
    for item in root:
        a = 0
        for reward in item:
            time_reward_function[t][s][a] = float(reward.text)
            a += 1
        s += 1


def read_cost_functions(cost_function, root):
    k=0
    for cf in root:
        s=0
        for state in cf:
            a=0
            for cost in state:
                cost_function[k][s][a] = float(cost.text)
                a += 1
            s += 1
        k+=1


def read_feasible_actions(feasible_actions, t, root):
    s = 0
    for item in root:
        a = 0
        for action in item:
            feasible_actions[t][s][a].append(action.text)
            a += 1
        s += 1


def read_observation_function(observation_function, a, root):
    s_next = 0
    for item in root:
        o = 0
        for prob in item:
            observation_function[a][s_next][o] = float(prob.text)
            o += 1
        s_next += 1


def read_cmdp(root):
    num_states = 0
    num_actions = 0
    initial_state = 0
    num_decisions = 0

    has_time_dependent_reward = False
    reward_function = None
    time_reward_function = None

    num_cost_functions = 0
    cost_function = None

    has_time_dependent_transitions = False
    transition_destinations = None
    transition_probabilities = None
    time_transition_destinations = None
    time_transition_probabilities = None

    reward_function_state_index = 0
    time_reward_function_time_index = 0
    transition_destinations_state_index = 0
    transition_probabilities_state_index = 0
    time_transition_destinations_state_index = 0
    time_transition_probabilities_state_index = 0

    feasible_actions = None
    feasible_actions_time_index = 0

    for child in root:
        if child.tag == "numStates":
            num_states = int(child.text)
        elif child.tag == "numActions":
            num_actions = int(child.text)
        elif child.tag == "initialState":
            initial_state = int(child.text)
        elif child.tag == "numDecisions":
            num_decisions = int(child.text)
            feasible_actions = [[[[] for a in range(num_actions)] for s in range(num_states)] for t in range(num_decisions)]
        elif child.tag == "hasTimeDependentReward":
            if child.text == "true":
                has_time_dependent_reward = True
                time_reward_function = [[[0 for a in range(num_actions)] for s in range(num_states)] for t in range(num_decisions)]
            else:
                has_time_dependent_reward = False
                reward_function = [[0 for a in range(num_actions)] for s in range(num_states)]
        elif child.tag == "rewardFunction":
            read_reward_function(reward_function, reward_function_state_index, child)
            reward_function_state_index += 1
        elif child.tag == "timeRewardFunction":
            read_time_reward_function(time_reward_function, time_reward_function_time_index, child)
            time_reward_function_time_index += 1
        elif child.tag == "hasTimeDependentTransitions":
            if child.text == "true":
                has_time_dependent_transitions = True
                time_transition_destinations = [[[[] for a in range(num_actions)] for s in range(num_states)] for t in range(num_decisions)]
                time_transition_probabilities = [[[[] for a in range(num_actions)] for s in range(num_states)] for t in range(num_decisions)]
            else:
                has_time_dependent_transitions = False
                transition_destinations = [[[] for a in range(num_actions)] for s in range(num_states)]
                transition_probabilities = [[[] for a in range(num_actions)] for s in range(num_states)]
        elif child.tag == "transitionDestinations":
            read_transition_destinations(transition_destinations, transition_destinations_state_index, child)
            transition_destinations_state_index += 1
        elif child.tag == "transitionProbabilities":
            read_transition_probabilities(transition_probabilities, transition_probabilities_state_index, child)
            transition_probabilities_state_index += 1
        elif child.tag == "timeTransitionDestinations":
            read_time_transition_destinations(time_transition_destinations, time_transition_destinations_state_index, child)
            time_transition_destinations_state_index += 1
        elif child.tag == "timeTransitionProbabilities":
            read_time_transition_probabilities(time_transition_probabilities, time_transition_probabilities_state_index, child)
            time_transition_probabilities_state_index += 1
        elif child.tag == "numCostFunctions":
            num_cost_functions = int(child.text)
            cost_function = [[[0 for a in range(num_actions)] for s in range(num_states)] for k in range(num_cost_functions)]
        elif child.tag == "costFunctions":
            read_cost_functions(cost_function, child)
        elif child.tag == "feasibleActions":
            read_feasible_actions(feasible_actions, feasible_actions_time_index, child)
            feasible_actions_time_index += 1

    assert len(cost_function) == num_cost_functions
    cmdp = CMDP(num_states, num_actions, initial_state, num_decisions)
    cmdp.set_cost_functions(cost_function)

    if has_time_dependent_reward:
        cmdp.set_time_reward_function(time_reward_function)
    else:
        cmdp.set_reward_function(reward_function)

    if has_time_dependent_transitions:
        cmdp.set_time_transitions(time_transition_destinations, time_transition_probabilities)
    else:
        cmdp.set_transitions(transition_destinations, transition_probabilities)

    return cmdp


def read_cpomdp(root):
    num_states = 0
    num_actions = 0
    num_observations = 0
    initial_state = 0
    initial_belief = []
    num_decisions = 0

    has_time_dependent_reward = False
    reward_function = None
    time_reward_function = None

    num_cost_functions = 0
    cost_function = None

    has_time_dependent_transitions = False
    transition_destinations = None
    transition_probabilities = None
    time_transition_destinations = None
    time_transition_probabilities = None

    reward_function_state_index = 0
    time_reward_function_time_index = 0
    transition_destinations_state_index = 0
    transition_probabilities_state_index = 0
    time_transition_destinations_state_index = 0
    time_transition_probabilities_state_index = 0

    feasible_actions = None
    feasible_actions_time_index = 0

    observation_function = None
    observation_function_action_index = 0

    for child in root:
        if child.tag == "numStates":
            num_states = int(child.text)
        elif child.tag == "numActions":
            num_actions = int(child.text)
        elif child.tag == "numObservations":
            num_observations = int(child.text)
            observation_function = [[[0 for o in range(num_observations)] for s in range(num_states)] for a in range(num_actions)]
        elif child.tag == "initialState":
            initial_state = int(child.text)
        elif child.tag == "initialBelief":
            initial_belief.append(float(child.text))
        elif child.tag == "numDecisions":
            num_decisions = int(child.text)
            feasible_actions = [[[[] for a in range(num_actions)] for s in range(num_states)] for t in range(num_decisions)]
        elif child.tag == "hasTimeDependentReward":
            if child.text == "true":
                has_time_dependent_reward = True
                time_reward_function = [[[0 for a in range(num_actions)] for s in range(num_states)] for t in range(num_decisions)]
            else:
                has_time_dependent_reward = False
                reward_function = [[0 for a in range(num_actions)] for s in range(num_states)]
        elif child.tag == "rewardFunction":
            read_reward_function(reward_function, reward_function_state_index, child)
            reward_function_state_index += 1
        elif child.tag == "timeRewardFunction":
            read_time_reward_function(time_reward_function, time_reward_function_time_index, child)
            time_reward_function_time_index += 1
        elif child.tag == "hasTimeDependentTransitions":
            if child.text == "true":
                has_time_dependent_transitions = True
                time_transition_destinations = [[[[] for a in range(num_actions)] for s in range(num_states)] for t in range(num_decisions)]
                time_transition_probabilities = [[[[] for a in range(num_actions)] for s in range(num_states)] for t in range(num_decisions)]
            else:
                has_time_dependent_transitions = False
                transition_destinations = [[[] for a in range(num_actions)] for s in range(num_states)]
                transition_probabilities = [[[] for a in range(num_actions)] for s in range(num_states)]
        elif child.tag == "transitionDestinations":
            read_transition_destinations(transition_destinations, transition_destinations_state_index, child)
            transition_destinations_state_index += 1
        elif child.tag == "transitionProbabilities":
            read_transition_probabilities(transition_probabilities, transition_probabilities_state_index, child)
            transition_probabilities_state_index += 1
        elif child.tag == "timeTransitionDestinations":
            read_time_transition_destinations(time_transition_destinations, time_transition_destinations_state_index, child)
            time_transition_destinations_state_index += 1
        elif child.tag == "timeTransitionProbabilities":
            read_time_transition_probabilities(time_transition_probabilities, time_transition_probabilities_state_index, child)
            time_transition_probabilities_state_index += 1
        elif child.tag == "numCostFunctions":
            num_cost_functions = int(child.text)
            cost_function = [[[0 for a in range(num_actions)] for s in range(num_states)] for k in range(num_cost_functions)]
        elif child.tag == "costFunctions":
            read_cost_functions(cost_function, child)
        elif child.tag == "feasibleActions":
            read_feasible_actions(feasible_actions, feasible_actions_time_index, child)
            feasible_actions_time_index += 1
        elif child.tag == "observationFunction":
            read_observation_function(observation_function, observation_function_action_index, child);
            observation_function_action_index += 1


    assert len(cost_function) == num_cost_functions

    belief = BeliefPoint(initial_belief)

    cpomdp = CPOMDP(num_states, num_actions, num_observations, belief, num_decisions)
    cpomdp.set_cost_functions(cost_function)
    cpomdp.set_observation_function(observation_function)

    if has_time_dependent_reward:
        cpomdp.set_time_reward_function(time_reward_function)
    else:
        cpomdp.set_reward_function(reward_function)

    if has_time_dependent_transitions:
        cpomdp.set_time_transitions(time_transition_destinations, time_transition_probabilities)
    else:
        cpomdp.set_transitions(transition_destinations, transition_probabilities)

    return cpomdp


def read_cost_limits_instantaneous(cost_limits_instantaneous, k, root):
    t = 0
    for item in root:
        cost_limits_instantaneous[k][t] = float(item.text)
        t += 1



def write_line(file, line):
    line += "\n"
    file.write(line)


def write_feasible_actions(file, feasible_actions):
    for t in feasible_actions:
        write_line(file, '\t\t\t<feasibleActions>')
        for s in t:
            write_line(file, '\t\t\t\t<item>')
            for a in s:
                write_line(file, '\t\t\t\t\t<item>{0}</item>'.format(a))
            write_line(file, '\t\t\t\t</item>')
        write_line(file, '\t\t\t</feasibleActions>')


def write_reward_function(file, reward_function):
    for s in reward_function:
        write_line(file, '\t\t\t<rewardFunction>')
        for a in s:
            write_line(file, '\t\t\t\t<item>{0}</item>'.format(a))
        write_line(file, '\t\t\t</rewardFunction>')
    return


def write_time_reward_function(file, time_reward_function):
    for t in time_reward_function:
        write_line(file, '\t\t\t<timeRewardFunction>')
        for s in t:
            write_line(file, '\t\t\t\t<item>')
            for a in s:
                write_line(file, '\t\t\t\t\t<item>{0}</item>'.format(a))
            write_line(file, '\t\t\t\t</item>')
        write_line(file, '\t\t\t</timeRewardFunction>')


def write_transition_destinations(file, transition_destinations):
    for s in transition_destinations:
        write_line(file, '\t\t\t<transitionDestinations>')
        for a in s:
            write_line(file, '\t\t\t\t<item>')
            for s_next in a:
                write_line(file, '\t\t\t\t\t<item>{0}</item>'.format(s_next))
            write_line(file, '\t\t\t\t</item>')
        write_line(file, '\t\t\t</transitionDestinations>')


def write_transition_probabilities(file, transition_probabilities):
    for s in transition_probabilities:
        write_line(file, '\t\t\t<transitionProbabilities>')
        for a in s:
            write_line(file, '\t\t\t\t<item>')
            for s_next in a:
                write_line(file, '\t\t\t\t\t<item>{0}</item>'.format(s_next))
            write_line(file, '\t\t\t\t</item>')
        write_line(file, '\t\t\t</transitionProbabilities>')



def write_time_transition_destinations(file, time_transition_destinations):
    for t in time_transition_destinations:
        write_line(file, '\t\t\t<timeTransitionDestinations>')
        for s in t:
            write_line(file, '\t\t\t\t<item>')
            for a in s:
                write_line(file, '\t\t\t\t\t<item>{0}</item>'.format(a))
            write_line(file, '\t\t\t\t</item>')
        write_line(file, '\t\t\t</timeTransitionDestinations>')



def write_time_transition_probabilities(file, time_transition_probabilities):
    for t in time_transition_probabilities:
        write_line(file, '\t\t\t<timeTransitionProbabilities>')
        for s in t:
            write_line(file, '\t\t\t\t<item>')
            for a in s:
                write_line(file, '\t\t\t\t\t<item>{0}</item>'.format(a))
            write_line(file, '\t\t\t\t</item>')
        write_line(file, '\t\t\t</timeTransitionProbabilities>')


def write_cost_functions(file, cost_functions):
    write_line(file, '\t\t\t<costFunctions>')
    for k in cost_functions:
        write_line(file, '\t\t\t\t<costFunction>')
        for s in k:
            write_line(file, '\t\t\t\t\t<item>')
            for cost in s:
                write_line(file, '\t\t\t\t\t\t<item>{0}</item>'.format(cost))
            write_line(file, '\t\t\t\t\t</item>')
        write_line(file, '\t\t\t\t</costFunction>')
    write_line(file, '\t\t\t</costFunctions>')


def write_observation_function(file, observation_function):
    for a in observation_function:
        write_line(file, '\t\t\t<observationFunction>')
        for s_next in a:
            write_line(file, '\t\t\t\t<item>')
            for prob in s_next:
                write_line(file, '\t\t\t\t\t<item>{0}</item>'.format(prob))
            write_line(file, '\t\t\t\t</item>')
        write_line(file, '\t\t\t</observationFunction>')

def write_cmdp(cmdp, file):
    write_line(file, '\t\t<cmdp>')

    write_line(file, '\t\t\t<numStates>{0}</numStates>'.format(cmdp.num_states))
    write_line(file, '\t\t\t<numActions>{0}</numActions>'.format(cmdp.num_actions))
    write_line(file, '\t\t\t<initialState>{0}</initialState>'.format(cmdp.initial_state))
    write_line(file, '\t\t\t<numDecisions>{0}</numDecisions>'.format(cmdp.num_decisions))

    write_feasible_actions(file, cmdp.feasible_actions)

    if cmdp.has_time_dependent_reward:
        write_line(file, '\t\t\t<hasTimeDependentReward>true</hasTimeDependentReward>')
        write_time_reward_function(file, cmdp.time_reward_function)
    else:
        write_line(file, '\t\t\t<hasTimeDependentReward>false</hasTimeDependentReward>')
        write_reward_function(file, cmdp.reward_function)

    if cmdp.has_time_dependent_transitions:
        write_line(file, '\t\t\t<hasTimeDependentTransitions>true</hasTimeDependentTransitions>')
        write_time_transition_destinations(file, cmdp.time_transition_destinations)
        write_time_transition_probabilities(file, cmdp.time_transition_probabilities)
    else:
        write_line(file, '\t\t\t<hasTimeDependentTransitions>false</hasTimeDependentTransitions>')
        write_transition_destinations(file, cmdp.transition_destinations)
        write_transition_probabilities(file, cmdp.transition_probabilities)

    write_line(file, '\t\t\t<numCostFunctions>{0}</numCostFunctions>'.format(cmdp.get_num_domain_resources()))
    write_cost_functions(file, cmdp.cost_function)

    write_line(file, '\t\t</cmdp>')
    return


def write_cpomdp(cmdp, file):
    write_line(file, '\t\t<cpomdp>')

    write_line(file, '\t\t\t<numStates>{0}</numStates>'.format(cmdp.num_states))
    write_line(file, '\t\t\t<numActions>{0}</numActions>'.format(cmdp.num_actions))
    write_line(file, '\t\t\t<numObservations>{0}</numObservations>'.format(cmdp.num_observations))
    write_line(file, '\t\t\t<initialState>{0}</initialState>'.format(cmdp.initial_state))
    write_line(file, '\t\t\t<numDecisions>{0}</numDecisions>'.format(cmdp.num_decisions))

    write_feasible_actions(file, cmdp.feasible_actions)

    if cmdp.has_time_dependent_reward:
        write_line(file, '\t\t\t<hasTimeDependentReward>true</hasTimeDependentReward>')
        write_time_reward_function(file, cmdp.time_reward_function)
    else:
        write_line(file, '\t\t\t<hasTimeDependentReward>false</hasTimeDependentReward>')
        write_reward_function(file, cmdp.reward_function)

    if cmdp.has_time_dependent_transitions:
        write_line(file, '\t\t\t<hasTimeDependentTransitions>true</hasTimeDependentTransitions>')
        write_time_transition_destinations(file, cmdp.time_transition_destinations)
        write_time_transition_probabilities(file, cmdp.time_transition_probabilities)
    else:
        write_line(file, '\t\t\t<hasTimeDependentTransitions>false</hasTimeDependentTransitions>')
        write_transition_destinations(file, cmdp.transition_destinations)
        write_transition_probabilities(file, cmdp.transition_probabilities)

    write_line(file, '\t\t\t<numCostFunctions>{0}</numCostFunctions>'.format(cmdp.get_num_domain_resources()))
    write_cost_functions(file, cmdp.cost_function)

    write_observation_function(file, cmdp.observation_function)

    for s in cmdp.initial_belief.belief:
        write_line(file, '\t\t\t<initialBelief>{0}</initialBelief>'.format(s))

    write_line(file, '\t\t</cpomdp>')
    return


def write_cmdp_instance(instance, file_name):
    file = open(file_name, 'w')

    write_line(file, '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>')
    write_line(file, '<ns2:probleminstance xmlns:ns2="instance">')

    write_line(file, '\t<numAgents>{0}</numAgents>'.format(instance.get_num_agents()))
    write_line(file, '\t<numDecisions>{0}</numDecisions>'.format(instance.num_decisions))
    write_line(file, '\t<numDomainResources>{0}</numDomainResources>'.format(instance.num_domain_resources))

    # write cost limits to file
    if (instance.use_budget_constraints):
        write_line(file, '\t<useBudgetConstraints>true</useBudgetConstraints>')
        for budget in instance.cost_limits_budget:
            write_line(file, '\t<costLimitsBudget>{0}</costLimitsBudget>'.format(budget))
    else:
        write_line(file, '<useBudgetConstraints>false</useBudgetConstraints>')
        for k in instance.cost_limits_instantaneous:
            write_line(file, '\t<costLimitsInstantaneous>')
            for limit in k:
                write_line(file, '\t\t<item>{0}</item>'.format(limit))
            write_line(file, '\t</costLimitsInstantaneous>')

    # write agents to file
    write_line(file, '\t<agents>')
    for cmdp in instance.cmdps:
        write_cmdp(cmdp, file)
    write_line(file, '\t</agents>')

    write_line(file, '</ns2:probleminstance>')
    file.close()


def write_cpomdp_instance(instance, file_name):
    file = open(file_name, 'w')

    write_line(file, '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>')
    write_line(file, '<ns2:probleminstance xmlns:ns2="instance">')

    write_line(file, '\t<numAgents>{0}</numAgents>'.format(instance.get_num_agents()))
    write_line(file, '\t<numDecisions>{0}</numDecisions>'.format(instance.num_decisions))
    write_line(file, '\t<numDomainResources>{0}</numDomainResources>'.format(instance.num_domain_resources))

    # write cost limits to file
    for budget in instance.cost_limits:
        write_line(file, '\t<costLimits>{0}</costLimits>'.format(budget))

    # write agents to file
    write_line(file, '\t<agents>')
    for cpomdp in instance.cpomdps:
        write_cpomdp(cpomdp, file)
    write_line(file, '\t</agents>')

    write_line(file, '</ns2:probleminstance>')
    file.close()


def read_cmdp_instance(file_name):
    tree = et.parse(file_name)
    root = tree.getroot()

    num_agents = 0
    num_decisions = 0
    num_domain_resources = 0
    use_budget_constraints = False
    cost_limits_budget = None
    cost_limits_instantaneous = None
    cmdps = []

    cost_limits_instantaneous_counter = 0
    for child in root:
        if child.tag == "numAgents":
            num_agents = int(child.text)
        elif child.tag == "numDecisions":
            num_decisions = int(child.text)
        elif child.tag == "numDomainResources":
            num_domain_resources = int(child.text)
            cost_limits_budget = []
            cost_limits_instantaneous = [[[] for t in range(num_decisions)] for k in range(num_domain_resources)]
        elif child.tag == "useBudgetConstraints":
            use_budget_constraints = (child.text == "true")
        elif child.tag == "costLimitsBudget":
            cost_limits_budget.append(float(child.text))
        elif child.tag == "costLimitsInstantaneous":
            read_cost_limits_instantaneous(cost_limits_instantaneous, cost_limits_instantaneous_counter, child)
            cost_limits_instantaneous_counter += 1
        elif child.tag == "agents":
            for cmdp in child:
                cmdp_object = read_cmdp(cmdp)
                cmdps.append(cmdp_object)

    assert len(cmdps) == num_agents
    instance = CMDPInstance(cmdps, num_decisions)

    if use_budget_constraints:
        assert len(cost_limits_budget) == num_domain_resources
        instance.set_cost_limits_budget(cost_limits_budget)
    else:
        assert len(cost_limits_instantaneous) == num_domain_resources
        instance.set_cost_limits_instantaneous(cost_limits_instantaneous)

    return instance


def read_cpomdp_instance(file_name):
    tree = et.parse(file_name)
    root = tree.getroot()

    num_agents = 0
    num_decisions = 0
    num_domain_resources = 0
    cost_limits = None
    cpomdps = []

    cost_limits_instantaneous_counter = 0
    for child in root:
        if child.tag == "numAgents":
            num_agents = int(child.text)
        elif child.tag == "numDecisions":
            num_decisions = int(child.text)
        elif child.tag == "numDomainResources":
            num_domain_resources = int(child.text)
            cost_limits = []
        elif child.tag == "costLimits":
            cost_limits.append(float(child.text))
        elif child.tag == "agents":
            for cpomdp in child:
                cpomdp_object = read_cpomdp(cpomdp)
                cpomdps.append(cpomdp_object)

    assert len(cpomdps) == num_agents
    instance = CPOMDPInstance(cpomdps, num_decisions)
    instance.set_cost_limits(cost_limits)

    return instance
