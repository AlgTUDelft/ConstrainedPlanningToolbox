/*******************************************************************************
 * ConstrainedPlanningToolbox
 * Copyright (C) 2019 Algorithmics group, Delft University of Technology
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *******************************************************************************/
package xml;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import model.CPOMDP;

@XmlRootElement(name = "cpomdp")
@XmlType(propOrder = { 
		"numStates", 
		"numActions",
		"numObservations",
		"initialState",
		"numDecisions",
		"feasibleActions",
		"hasTimeDependentReward",
		"rewardFunction",
		"timeRewardFunction",
		"hasTimeDependentTransitions",
		"transitionDestinations",
		"transitionProbabilities",
		"timeTransitionDestinations",
		"timeTransitionProbabilities",
		"numCostFunctions",
		"costFunctions",
		"observationFunction",
		"initialBelief"
		})

public class XMLCPOMDP {
	
	private int numStates;
	private int numActions;
	private int initialState;
	private int numDecisions;
	
	private int[][][] feasibleActions;
	
	private boolean hasTimeDependentReward;
	private double[][] rewardFunction;
	private double[][][] timeRewardFunction;
	
	private boolean hasTimeDependentTransitions = false;
	private int[][][] transitionDestinations;
	private double[][][] transitionProbabilities;
	private int[][][][] timeTransitionDestinations;
	private double[][][][] timeTransitionProbabilities;
	
	private int numCostFunctions = 0;
	
	@XmlElementWrapper(name = "costFunctions")
    @XmlElement(name = "costFunction")	
	private List<double[][]> costFunctions = new ArrayList<double[][]>();
	
	private int numObservations;
	private double[][][] observationFunction;
	private double[] initialBelief;
	
	public XMLCPOMDP() {
		
	}
	
	public XMLCPOMDP(CPOMDP cpomdp) {
		initialState = cpomdp.getInitialState();
		numStates = cpomdp.getNumStates();
		numActions = cpomdp.getNumActions();
		numDecisions = cpomdp.getNumDecisions();
		setNumObservations(cpomdp.getNumObservations());
		setObservationFunction(cpomdp.getObservationFunction());
		setInitialBelief(cpomdp.getInitialBelief().getBelief());
		
		if(cpomdp.hasTimeDependentReward()) {
			hasTimeDependentReward = true;
			timeRewardFunction = cpomdp.getTimeRewardFunction();
		}
		else {
			hasTimeDependentReward = false;
			rewardFunction = cpomdp.getRewardFunction();
		}
		
		if(cpomdp.hasTimeDependentTransitions()) {
			hasTimeDependentTransitions = true;
			timeTransitionDestinations = cpomdp.getTimeTransitionDestinations();
			timeTransitionProbabilities = cpomdp.getTimeTransitionProbabilities();
		}
		else {
			hasTimeDependentTransitions = false;
			transitionDestinations = cpomdp.getTransitionDestinations();
			transitionProbabilities = cpomdp.getTransitionProbabilities();
		}
		
		costFunctions = cpomdp.getCostFunctions();
		numCostFunctions = costFunctions.size();
		feasibleActions = cpomdp.getFeasibleActions();
	}
	
	public int getNumStates() {
		return numStates;
	}
	
	public void setNumStates(int numStates) {
		this.numStates = numStates;
	}
	
	public int getNumActions() {
		return numActions;
	}
	
	public void setNumActions(int numActions) {
		this.numActions = numActions;
	}
	
	public int getInitialState() {
		return initialState;
	}
	
	public void setInitialState(int initialState) {
		this.initialState = initialState;
	}
	
	public int getNumDecisions() {
		return numDecisions;
	}
	
	public void setNumDecisions(int numDecisions) {
		this.numDecisions = numDecisions;
	}

	public double[][] getRewardFunction() {
		return rewardFunction;
	}

	public void setRewardFunction(double[][] rewardFunction) {
		this.rewardFunction = rewardFunction;
		this.setHasTimeDependentReward(false);
	}

	public double[][][] getTimeRewardFunction() {
		return timeRewardFunction;
	}

	public void setTimeRewardFunction(double[][][] timeRewardFunction) {
		this.timeRewardFunction = timeRewardFunction;
		this.setHasTimeDependentReward(true);
	}

	public boolean isHasTimeDependentReward() {
		return hasTimeDependentReward;
	}

	public void setHasTimeDependentReward(boolean hasTimeDependentReward) {
		this.hasTimeDependentReward = hasTimeDependentReward;
	}

	public boolean isHasTimeDependentTransitions() {
		return hasTimeDependentTransitions;
	}

	public void setHasTimeDependentTransitions(boolean hasTimeDependentTransitions) {
		this.hasTimeDependentTransitions = hasTimeDependentTransitions;
	}

	public int[][][] getTransitionDestinations() {
		return transitionDestinations;
	}

	public void setTransitionDestinations(int[][][] transitionDestinations) {
		this.transitionDestinations = transitionDestinations;
	}

	public double[][][] getTransitionProbabilities() {
		return transitionProbabilities;
	}

	public void setTransitionProbabilities(double[][][] transitionProbabilities) {
		this.transitionProbabilities = transitionProbabilities;
	}

	public int[][][][] getTimeTransitionDestinations() {
		return timeTransitionDestinations;
	}

	public void setTimeTransitionDestinations(int[][][][] timeTransitionDestinations) {
		this.timeTransitionDestinations = timeTransitionDestinations;
	}

	public double[][][][] getTimeTransitionProbabilities() {
		return timeTransitionProbabilities;
	}

	public void setTimeTransitionProbabilities(double[][][][] timeTransitionProbabilities) {
		this.timeTransitionProbabilities = timeTransitionProbabilities;
	}

	public List<double[][]> getCostFunctions() {
		return costFunctions;
	}
	
	public void addCostFunction(double[][] costFunction) {
		setNumCostFunctions(getNumCostFunctions() + 1);
		costFunctions.add(costFunction);
	}

	public int getNumCostFunctions() {
		return numCostFunctions;
	}

	public void setNumCostFunctions(int numCostFunctions) {
		this.numCostFunctions = numCostFunctions;
	}

	public int[][][] getFeasibleActions() {
		return feasibleActions;
	}

	public void setFeasibleActions(int[][][] feasibleActions) {
		this.feasibleActions = feasibleActions;
	}

	public int getNumObservations() {
		return numObservations;
	}

	public void setNumObservations(int numObservations) {
		this.numObservations = numObservations;
	}

	public double[][][] getObservationFunction() {
		return observationFunction;
	}

	public void setObservationFunction(double[][][] observationFunction) {
		this.observationFunction = observationFunction;
	}

	public double[] getInitialBelief() {
		return initialBelief;
	}

	public void setInitialBelief(double[] initialBelief) {
		this.initialBelief = initialBelief;
	}
}
