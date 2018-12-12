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
package model;

public class POMDP extends MDP {
	private int nObservations;
	private double[][][] observationFunction;
	private BeliefPoint b0;
	
	public POMDP(int nStates, int nActions, int nObservations, double[][][] observationFunction, BeliefPoint b0, int nDecisions) {
		super(nStates, nActions, 0, nDecisions);
		this.nObservations = nObservations;
		this.observationFunction = observationFunction;
		this.b0 = b0;
	}
	
	/**
	 * Get number of observations
	 * @return number of observations
	 */
	public int getNumObservations() {
		return nObservations;
	}
	
	/**
	 * Get probability to observe o after executing a and reaching sNext
	 * @param a action
	 * @param sNext next state
	 * @param o observation
	 * @return probability
	 */
	public double getObservationProbability(int a, int sNext, int o) {
		assert a<this.getNumActions() && sNext<this.getNumStates() && o<nObservations;
		return observationFunction[a][sNext][o];
	}
	
	/** 
	 * Get observation function
	 * @return observation function
	 */
	public double[][][] getObservationFunction() {
		return observationFunction;
	}
	
	/**
	 * Get initial belief
	 */
	public BeliefPoint getInitialBelief() {
		return b0;
	}
	
	/**
	 * Compute action observation probabilities for given belief
	 * @param b belief
	 */
	public void prepareBelief(BeliefPoint b) {
		if(b.hasActionObservationProbabilities()) return;
		
		double[][] aoProbs = new double[this.getNumActions()][nObservations];
		
		for(int a=0; a<this.getNumActions(); a++) {
			for(int o=0; o<nObservations; o++) {
				double prob = 0.0;
				
				for(int s=0; s<this.getNumStates(); s++) {
					int[] transitionDestinations = this.getTransitionDestinations(s, a);
					double[] transitionProbabilities = this.getTransitionProbabilities(s, a);
					
					for(int i=0; i<transitionDestinations.length; i++) {
						int sNext = transitionDestinations[i];
						double sNextProb = transitionProbabilities[i];
						
						prob += this.getObservationProbability(a, sNext, o) * sNextProb * b.getBelief(s);
					}
				}
				
				aoProbs[a][o] = prob;
			}
		}
		
		b.setActionObservationProbabilities(aoProbs);
	}
	
	/**
	 * Perform belief update
	 * @param b belief
	 * @param a action
	 * @param o observation
	 * @return resulting belief
	 */
	public BeliefPoint updateBelief(BeliefPoint b, int a, int o) {
		assert a<this.getNumActions() && o<nObservations;
		double[] newBelief = new double[this.getNumStates()];
		
		// check if belief point has been prepared
		if(!b.hasActionObservationProbabilities()) {
			prepareBelief(b);
		}
		
		// compute normalizing constant
		double nc = b.getActionObservationProbability(a, o);
		assert nc > 0.0 : "o cannot be observed when executing a in belief b";
		
		// compute the new belief vector
		for(int s=0; s<this.getNumStates(); s++) {
			int[] transitionDestinations = this.getTransitionDestinations(s, a);
			double[] transitionProbabilities = this.getTransitionProbabilities(s, a);
			
			for(int i=0; i<transitionDestinations.length; i++) {
				int sNext = transitionDestinations[i];
				double sNextProb = transitionProbabilities[i];
				
				newBelief[sNext] += getObservationProbability(a, sNext, o) * sNextProb * (1.0 / nc) * b.getBelief(s);
			}
		}
		
		return new BeliefPoint(newBelief);
	}
}
