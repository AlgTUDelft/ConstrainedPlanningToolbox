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
package algorithms.pomdp.cgcp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;


import solutions.pomdp.POMDPAgentSolutionPolicyBased;
import solutions.pomdp.POMDPPolicyGraph;
import solutions.pomdp.POMDPPolicyVector;
import util.ConsoleOutput;

import model.AlphaVector;
import model.BeliefPoint;
import model.CPOMDP;
import model.POMDP;

public class FiniteVI {
	private final double gapTolerance = 0.0001; // during search this tolerance is used to decide whether new belief should be added
	private double terminateTime = 3600; // if runtime exceeds the limit when performing the check, then we stop
	private final int maxIter = 1000000;
	private boolean dumpPolicyGraph = false;
	private Random rnd;
	
	// attributes set when solving
	private POMDP pomdp;
	private int T;
	private double lambda;
	private List<AlphaVector> immediateRewards;
	private ArrayList<ArrayList<BeliefPoint>> beliefLists;
	private ArrayList<HashSet<BeliefPoint>> beliefSets;
	private ArrayList<ArrayList<AlphaVector>> vectorSets;
	
	public FiniteVI(Random rnd) {
		this.rnd = rnd;
	}
	
	private void initBeliefSets() {
		// initialize data structures
		beliefLists = new ArrayList<ArrayList<BeliefPoint>>();
		beliefSets = new ArrayList<HashSet<BeliefPoint>>();
		for(int t=0; t<=T; t++) {
			beliefLists.add(new ArrayList<BeliefPoint>());
			beliefSets.add(new HashSet<BeliefPoint>());
		}
		
		assert beliefLists.size() == T+1;
		assert beliefSets.size() == T+1;
		
		// add initial belief for t=0
		pomdp.getInitialBelief().resetUpperBound();
		assert !pomdp.getInitialBelief().isUpperBoundTight();
		beliefLists.get(0).add(pomdp.getInitialBelief());
		beliefSets.get(0).add(pomdp.getInitialBelief());
		
		// for each timestep: add corner beliefs and initial belief
		for(int t=0; t<=T; t++) {
			ArrayList<BeliefPoint> beliefList = beliefLists.get(t);
			HashSet<BeliefPoint> beliefSet = beliefSets.get(t);
			
			for(int s=0; s<pomdp.getNumStates(); s++) {
				double[] belief = new double[pomdp.getNumStates()];
				belief[s] = 1.0;
				
				BeliefPoint bp = new BeliefPoint(belief);
				bp.addToHistory((s + 1) * -1);
				bp.setState(s);
				bp.setStateBelief();
				beliefList.add(bp);
				beliefSet.add(bp);
			}
			
			// add initial belief
			double[] belief = new double[pomdp.getNumStates()];
			for(int s=0; s<pomdp.getNumStates(); s++) {
				belief[s] = pomdp.getInitialBelief().getBelief(s);
				
				BeliefPoint bp = new BeliefPoint(belief);
				bp.addToHistory((s + 1) * -1);
				beliefList.add(bp);
				beliefSet.add(bp);
			}
			
		}	
		
	}
	
	private AlphaVector[][][] getBackProjections(ArrayList<AlphaVector> V) {
		int nStates = pomdp.getNumStates();
		int nActions = pomdp.getNumActions();
		int nObservations = pomdp.getNumObservations();
		
		AlphaVector[][][] gkao = new AlphaVector[V.size()][nActions][nObservations];
		
		for(int k=0; k<V.size(); k++) {
			for(int a=0; a<nActions; a++) {
				for(int o=0; o<nObservations; o++) {
					double[] entries = new double[nStates];
					
					for(int s=0; s<nStates; s++) {
						double val = 0.0;
						
						int[] transitionDestinations = pomdp.getTransitionDestinations(s, a);
						double[] transitionProbabilities = pomdp.getTransitionProbabilities(s, a);
						
						for(int j=0; j<transitionDestinations.length; j++) {
							int sPrime = transitionDestinations[j];
							double prob = transitionProbabilities[j];
							
							val += pomdp.getObservationProbability(a, sPrime, o) * prob * V.get(k).getEntry(sPrime);
						}
						
						entries[s] = val;
					}
					
					AlphaVector av = new AlphaVector(entries);					
					av.setAction(a);
					gkao[k][a][o] = av;
				}
			}
		}
		
		return gkao;
	}
	
	private AlphaVector backup(AlphaVector[][][] gkao, BeliefPoint b) {
		int nActions = pomdp.getNumActions();
		int nObservations = pomdp.getNumObservations();
		
		List<AlphaVector> ga = new ArrayList<AlphaVector>();
		
		for(int a=0; a<nActions; a++) {
			List<AlphaVector> oVectors = new ArrayList<AlphaVector>();
			for(int o=0; o<nObservations; o++) {
				double maxVal = Double.NEGATIVE_INFINITY;
				AlphaVector maxVector = null;
				
				int K = gkao.length;
				for(int k=0; k<K; k++) {
					double product = gkao[k][a][o].getDotProduct(b.getBelief());
					if(product > maxVal) {
						maxVal = product;
						maxVector = gkao[k][a][o];
					}
				}
				
				assert maxVector != null;
				oVectors.add(maxVector);
			}
			
			assert oVectors.size() > 0;
			
			// take sum of the vectors
			AlphaVector sumVector = oVectors.get(0);
			for(int j=1; j<oVectors.size(); j++) {
				sumVector = AlphaVector.sumVectors(sumVector, oVectors.get(j));
			}
			
			AlphaVector av = AlphaVector.sumVectors(immediateRewards.get(a), sumVector);
			av.setAction(a);
			ga.add(av);
		}
		
		assert ga.size() == nActions;
		
		// find the maximizing vector
		double maxVal = Double.NEGATIVE_INFINITY;
		AlphaVector vFinal = null;
		for(AlphaVector av : ga) {
			double product = av.getDotProduct(b.getBelief());
			if(product > maxVal) {
				maxVal = product;
				vFinal = av;
			}
		}
		assert vFinal != null;
		vFinal.setBeliefPoint(b);
		
		return vFinal;
	}

	private double getUpperBoundSawTooth(BeliefPoint b, List<BeliefPoint> Vupper) {
		assert pomdp != null && b != null && Vupper.size() > pomdp.getNumStates();
		
		int nStates = pomdp.getNumStates();
		
		// precompute state upper bounds
		double[] stateUpper = new double[nStates];
		int nonStateBeliefs = 0;
		for(BeliefPoint bp : Vupper) {
			if(bp.isStateBelief()) {
				int s = bp.getState();
				stateUpper[s] = bp.getUpperBound();
			}
			else {
				nonStateBeliefs++;
			}
		}
		assert nonStateBeliefs > 0 : "There are only state beliefs in vUpper";
		
		// compute c and f using the for loop, and compute bBarStar
		double[] c = new double[Vupper.size()];
		double[] f = new double[Vupper.size()];
		int bBarStarIndex = -1;
		double bBarStarValue = Double.POSITIVE_INFINITY;
		
		for(int bBarIndex=0; bBarIndex<Vupper.size(); bBarIndex++) {
			BeliefPoint bBar = Vupper.get(bBarIndex);
			if(!bBar.isStateBelief()) {
				f[bBarIndex] = bBar.getUpperBound();
				
				double minC = Double.POSITIVE_INFINITY;
				for(int s=0; s<nStates; s++) {
					if(bBar.getBelief(s) > 0.0) {
						f[bBarIndex] = f[bBarIndex] - (bBar.getBelief(s) * stateUpper[s]);
						double curC = b.getBelief(s) / bBar.getBelief(s);
						if(curC < minC) minC = curC;
					}
				}
				assert minC < Double.POSITIVE_INFINITY;
				c[bBarIndex] = minC;
				
				double z = c[bBarIndex] * f[bBarIndex];
				if(z < bBarStarValue) {
					bBarStarIndex = bBarIndex;
					bBarStarValue = z;
				}
			}
		}
		assert bBarStarIndex != -1 : "It seems that Vupper only contains state beliefs";
		
		// compute vStar
		double mv = c[bBarStarIndex]*f[bBarStarIndex];
		for(int s=0; s<nStates; s++) {
			mv += b.getBelief(s) * stateUpper[s];
		}
		double vStar = mv;
		
		return vStar;
	}
	
	private double getUpperBound(BeliefPoint b, int t) {
		assert t>=0 && t<=T;
		
		if(t == T) {
			return 0.0;
		}
		else {
			return getUpperBoundSawTooth(b, beliefLists.get(t));
		}
	}
	
	private double getLowerBound(BeliefPoint b, int t) {
		assert t>=0 && t<T;
		return AlphaVector.getValue(b.getBelief(), vectorSets.get(t));
	}
	
	private double getTransitionReward(int s, int a) {
		if(pomdp instanceof CPOMDP) {
			CPOMDP cpomdp = (CPOMDP) pomdp;
			return pomdp.getReward(s, a) - lambda * cpomdp.getCost(0, s, a);
		}
		else {
			return pomdp.getReward(s, a);
		}
	}
	
	private void findNewBeliefs() { 
		/*
		 * The for loop considers a belief at time t and finds a new belief for timestep t+1.
		 * 
		 * We skip t=T because we do not need belief points for timestep T+1
		 * We skip t=T-1 because then we would find beliefs for timestep T, for which the upper bound is already optimal
		 */
		
		BeliefPoint b = pomdp.getInitialBelief();
		int numAdded = 0;
		
		for(int t=0; t<T-1; t++) {
			pomdp.prepareBelief(b);		
			
			// choose action with the greatest upper bound
			double maxVal = Double.NEGATIVE_INFINITY;
			int maxAction = -1;
			BeliefPoint[][] beliefs = new BeliefPoint[pomdp.getNumActions()][pomdp.getNumObservations()];
			for(int a=0; a<pomdp.getNumActions(); a++) {
				double val = immediateRewards.get(a).getDotProduct(b.getBelief());
				
				for(int o=0; o<pomdp.getNumObservations(); o++) {
					if(b.getActionObservationProbability(a, o) > 0.0) {
						BeliefPoint bao = pomdp.updateBelief(b, a, o);
						beliefs[a][o] = bao;
						val += b.getActionObservationProbability(a, o) * getUpperBound(bao, t+1);
					}
					else {
						beliefs[a][o] = null;
					}
				}
				
				if(val > maxVal) {
					maxVal = val;
					maxAction = a;
				}
			}
			assert maxAction != -1;
			
			// choose observation for which resulting belief has the largest gap
			double maxGap = Double.NEGATIVE_INFINITY;
			int selectedObservation = -1;
			for(int o=0; o<pomdp.getNumObservations(); o++) {
				if(b.getActionObservationProbability(maxAction, o) > 0.0) {
					BeliefPoint bao = beliefs[maxAction][o];
					assert bao != null;
					double gap = getUpperBound(bao, t+1) - getLowerBound(bao, t+1);
					if(gap > maxGap) {
						maxGap = gap;
						selectedObservation = o;
					}
				}
			}
			assert selectedObservation != -1;
			
			// prepare for step t+1
			BeliefPoint bao = pomdp.updateBelief(b, maxAction, selectedObservation);
			bao.setHistory(b.getHistoryCopy());
			bao.addToHistory(maxAction);
			bao.addToHistory(selectedObservation);
			
			// add the belief point to the set and list of the next timestep, if necessary
			ArrayList<BeliefPoint> beliefList = beliefLists.get(t+1);
			HashSet<BeliefPoint> beliefSet = beliefSets.get(t+1);
			if(!beliefSet.contains(bao)) {
				beliefList.add(bao);
				beliefSet.add(bao);
				numAdded++;
			}
			
			// set b to the next belief
			b = bao;
		}
		
		ConsoleOutput.println("Beliefs added: "+numAdded);
	}
	
	public POMDPAgentSolutionPolicyBased solve(POMDP pomdp, int T) {
		return solveModel(pomdp, T, 0.0);
	}
	
	public POMDPAgentSolutionPolicyBased solve(CPOMDP cpomdp, int T, double lambda) {
		return solveModel(cpomdp, T, lambda);
	}
	
	private POMDPAgentSolutionPolicyBased solveModel(POMDP pomdp, int T, double lambda) {
		this.pomdp = pomdp;
		this.T = T;
		this.lambda = lambda;
		
		// for each timestep we initialize a set of beliefs
		initBeliefSets();
		
		// initialize sets for alphavectors
		vectorSets = new ArrayList<ArrayList<AlphaVector>>();
		for(int t=0; t<=T; t++) {
			vectorSets.add(new ArrayList<AlphaVector>());
		}
		
		// initialize immediate reward vectors
		immediateRewards = new ArrayList<AlphaVector>();
		for(int a=0; a<pomdp.getNumActions(); a++) {
			double[] entries = new double[pomdp.getNumStates()];
			for(int s=0; s<pomdp.getNumStates(); s++) {
				entries[s] = getTransitionReward(s,a);
			}
			
			AlphaVector av = new AlphaVector(entries);
			av.setAction(a);
			immediateRewards.add(av);
		}
		assert immediateRewards.size() == pomdp.getNumActions();
		
		// initialize value function for t=T containing just zeros
		ArrayList<AlphaVector> vectorSet = vectorSets.get(T);
		for(int a=0; a<pomdp.getNumActions(); a++) {
			double[] entries = new double[pomdp.getNumStates()];			
			AlphaVector av = new AlphaVector(entries);
			av.setAction(a);
			vectorSet.add(av);
		}
		assert vectorSets.get(T).size() == pomdp.getNumActions();
		
		// compute upper bound for belief points in t=T
		for(BeliefPoint bp : beliefLists.get(T)) {
			bp.setUpperBound(0.0);
		}
		
		// run value iteration
		int iter = 0;
		double lastValueUpperBound = Double.POSITIVE_INFINITY;
		long startTime = System.currentTimeMillis();
		while(true) {			
			// compute value function lower bound and upper bound backwards from t=T-1 to t=0
			for(int t=T-1; t>=0; t--) {
				// randomized backup stage, upper bound updates on all points
				backupStagePerseus(t, iter);
				upperBoundUpdateSkip(t, beliefLists.get(t));
			}
			
			// print information about current solution
			BeliefPoint initialBelief = pomdp.getInitialBelief();
			double currentLower = AlphaVector.getValue(initialBelief.getBelief(), vectorSets.get(0));
			double currentUpper = initialBelief.getUpperBound();
			double currentGap = currentUpper - currentLower;
			double allowedGap = Math.pow(10.0, Math.ceil(Math.log10(Math.max(currentLower, currentUpper))) - 3.0);
			double elapsed = (System.currentTimeMillis()-startTime) * 0.001;
			ConsoleOutput.println(currentLower+" "+currentUpper+" (gap: "+currentGap+", allowed: "+allowedGap+", elapsed: "+elapsed+"s)");
			
			
			assert currentGap >= -0.0001 : "Gap must be positive";
			lastValueUpperBound = currentUpper;
			
			// check for convergence of the algorithm
			iter++;
			double elapsedTime = (System.currentTimeMillis()-startTime) * 0.001;
			if(iter > maxIter 
					|| elapsedTime > terminateTime 
					|| currentGap < allowedGap
					|| Math.abs(currentGap) < 0.01) {
				break;
			}
			
			// find new beliefs before starting the next iteration
			findNewBeliefs();
		}
		
		// return policy represented by vectors, or create a graph		
		if(dumpPolicyGraph) {			
			POMDPPolicyGraph pg = computePolicyGraph();
			pg.setExpectedValueUpperbound(lastValueUpperBound);
			return pg;
		}
		else {
			double finalLowerBound = AlphaVector.getValue(pomdp.getInitialBelief().getBelief(), vectorSets.get(0));
			POMDPPolicyVector pvf = new POMDPPolicyVector(vectorSets, finalLowerBound);
			pvf.setExpectedValueUpperbound(pomdp.getInitialBelief().getUpperBound());
			return pvf;
		}
	}
	
	/**
	 * Execute perseus backup stage
	 * @param t current time step
	 * @param iter current iteration of value iteration
	 */
	private void backupStagePerseus(int t, int iter) {
		// compute back projections based on next-timestep value function
		AlphaVector[][][] gkao = getBackProjections(vectorSets.get(t+1));
		
		if(iter == 0) {
			ArrayList<AlphaVector> vectorSet = vectorSets.get(t);
			vectorSet.clear();
			
			for(int i=0; i<beliefLists.get(t).size(); i++) {
				BeliefPoint b = beliefLists.get(t).get(i);
				
				// perform the backup (or take a vector from cache)
				AlphaVector backupVector = backup(gkao, b);
				
				// add vector to the list for this timestep
				vectorSet.add(backupVector);
			}
		}
		else {
			// perseus-like backup stage
			ArrayList<AlphaVector> vectorSet = vectorSets.get(t);
			ArrayList<BeliefPoint> Btilde = new ArrayList<BeliefPoint>(beliefLists.get(t));
			ArrayList<AlphaVector> newVectorSet = new ArrayList<AlphaVector>();
			
			while(Btilde.size() > 0) {
				// sample belief and compute backup
				BeliefPoint sampledPoint = Btilde.get(rnd.nextInt(Btilde.size()));
				AlphaVector backupVector = backup(gkao,sampledPoint);
				
				// check which vector we need to add
				double newValue = backupVector.getDotProduct(sampledPoint.getBelief());
				int bestOldVectorIndex = AlphaVector.getBestVectorIndex(sampledPoint.getBelief(), vectorSet);
				
				double oldValue = vectorSet.get(bestOldVectorIndex).getDotProduct(sampledPoint.getBelief());
				if(newValue >= oldValue) {
					newVectorSet.add(backupVector);
				}
				else {
					newVectorSet.add(vectorSet.get(bestOldVectorIndex));
				}
				
				// compute new Btilde
				ArrayList<BeliefPoint> newBtilde = new ArrayList<BeliefPoint>();
				for(BeliefPoint bp : Btilde) {
					double nValue = AlphaVector.getValue(bp.getBelief(), newVectorSet);
					double oValue = AlphaVector.getValue(bp.getBelief(), vectorSet);
					
					if(nValue < oValue) {
						newBtilde.add(bp);
					}
				}
				Btilde = newBtilde;
			}
			
			vectorSets.set(t, newVectorSet);
		}
	}
	
	
	
	/**
	 * Update the upper bounds for all points in boundUpdateList, and skip points with zero gap
	 * @param t current time step
	 * @param boundUpdateList ponits for which upper bound should be updated
	 */
	private void upperBoundUpdateSkip(int t, ArrayList<BeliefPoint> boundUpdateList) {
		// update the upper bound for all belief points
		for(BeliefPoint b : boundUpdateList) {
			pomdp.prepareBelief(b);
			
			if(b.isUpperBoundTight()) {
				continue;
			}
			
			// if we computed b_a_o beliefs in the past already, then we retrieve the array containing them, otherwise empty array
			BeliefPoint[][] beliefTransitionMap = b.hasBeliefTransitionMap() ? b.getBeliefTransitionMap() : new BeliefPoint[pomdp.getNumActions()][pomdp.getNumObservations()];
			
			// compute new upper bound
			double upperBound = Double.NEGATIVE_INFINITY;
			for(int a=0; a<pomdp.getNumActions(); a++) {
				double val = immediateRewards.get(a).getDotProduct(b.getBelief());
				
				for(int o=0; o<pomdp.getNumObservations(); o++) {
					if(b.getActionObservationProbability(a, o) > 0.0) {
						// either obtain belief from the array, or compute the new one.
						BeliefPoint bao = beliefTransitionMap[a][o] == null ? pomdp.updateBelief(b, a, o) : beliefTransitionMap[a][o];
						
						// add belief to the array
						if(beliefTransitionMap[a][o] == null) {
							beliefTransitionMap[a][o] = bao;
						}
						
						// compute value
						val += b.getActionObservationProbability(a, o) * getUpperBound(bao, t+1);
					}
				}
				
				if(val > upperBound) {
					upperBound = val;
				}
			}
			
			b.setUpperBound(upperBound);
			
			// set belief transition map
			if(!b.hasBeliefTransitionMap()) {
				b.setBeliefTransitionMap(beliefTransitionMap);
			}
			
			// check if upper bound is tight
			double lowerBound = getLowerBound(b, t);
			assert lowerBound <= upperBound+0.001 : "Lower must be <= Upper: "+lowerBound+" "+upperBound;
			double gap = Math.abs(upperBound - lowerBound);
			
			if(gap < gapTolerance) {
				b.setUpperBoundTight();
			}
			
			// bound update done
		}
	}
	
	/**
	 * Compute a policy graph based on the current solution
	 * @return finite horizon policy graph
	 */
	private POMDPPolicyGraph computePolicyGraph() {
		int numLayers = T+1;
		int[] layerNodeCount = new int[numLayers];
		ArrayList<int[]> layerNodeActions = new ArrayList<int[]>();
		ArrayList<BeliefPoint[]> layerNodeBeliefs = new ArrayList<BeliefPoint[]>();
		ArrayList<int[][]> layerTransitions = new ArrayList<int[][]>();
		for(int t=0; t<=T; t++) {
			ArrayList<AlphaVector> vectors = vectorSets.get(t);
			layerNodeCount[t] = vectors.size();
			
			int[] actions = new int[vectors.size()];
			BeliefPoint [] beliefs = new BeliefPoint[vectors.size()];
			int[][] transitions = new int[vectors.size()][pomdp.getNumObservations()];
			
			if(t < T) {
				for(int q=0; q<vectors.size(); q++) {
					// determine the action to be executed in node q at time t
					AlphaVector av = vectors.get(q);
					assert av.hasBeliefPoint();
					BeliefPoint b = av.getBeliefPoint();
					beliefs[q] = b;
					pomdp.prepareBelief(b);
					
					int a = vectors.get(q).getAction();
					actions[q] = a;
					
					// based on the vectors of time t+1, we determine the transitions to the next layer
					ArrayList<AlphaVector> nextVectors = vectorSets.get(t+1);
					
					for(int o=0; o<pomdp.getNumObservations(); o++) {
						if(b.getActionObservationProbability(a, o) > 0.0) {
							BeliefPoint bao = pomdp.updateBelief(b, a, o);
							int nextIndex = AlphaVector.getBestVectorIndex(bao.getBelief(), nextVectors);
							transitions[q][o] = nextIndex;
							assert nextIndex >= 0 && nextIndex < nextVectors.size();
						}
						else {
							transitions[q][o] = 0;
						}
					}
				}
			}
			
			layerNodeActions.add(actions);
			layerNodeBeliefs.add(beliefs);
			layerTransitions.add(transitions);
		}
		int startNode = AlphaVector.getBestVectorIndex(pomdp.getInitialBelief().getBelief(), vectorSets.get(0));
		
		POMDPPolicyGraph pg = new POMDPPolicyGraph(numLayers, layerNodeCount, pomdp.getNumActions(), pomdp.getNumObservations(), 
				layerNodeActions, layerNodeBeliefs, layerTransitions, startNode);
		
		// evaluate policy graph
		PolicyGraphEvaluatorFinite eval = new PolicyGraphEvaluatorFinite(pomdp, pg);
		eval.evaluate(lambda);
		
		pg.setExpectedValue(eval.getExpectedValue());
		pg.setExpectedReward(eval.getExpectedReward());
		pg.setExpectedCost(eval.getExpectedCost());
		
		return pg;
	}
	
	/**
	 * Compute a policy graph which only executes one action
	 * @return finite horizon policy graph
	 */
	private POMDPPolicyGraph computePolicyGraphSingleAction(int a) {
		int numLayers = T+1;
		int[] layerNodeCount = new int[numLayers];
		ArrayList<int[]> layerNodeActions = new ArrayList<int[]>();
		ArrayList<int[][]> layerTransitions = new ArrayList<int[][]>();
		for(int t=0; t<=T; t++) {
			layerNodeCount[t] = 1;
			
			int[] actions = new int[1];
			int[][] transitions = new int[1][pomdp.getNumObservations()];
			
			if(t < T) {
				actions[0] = a;
				for(int o=0; o<pomdp.getNumObservations(); o++) {
					transitions[0][o] = 0;
				}
			}
			
			layerNodeActions.add(actions);
			layerTransitions.add(transitions);
		}
		int startNode = 0;
		
		POMDPPolicyGraph pg = new POMDPPolicyGraph(numLayers, layerNodeCount, pomdp.getNumActions(), pomdp.getNumObservations(), 
				layerNodeActions, null, layerTransitions, startNode);
		
		// evaluate policy graph
		PolicyGraphEvaluatorFinite eval = new PolicyGraphEvaluatorFinite(pomdp, pg);
		eval.evaluate(lambda);
		
		pg.setExpectedValue(eval.getExpectedValue());
		pg.setExpectedReward(eval.getExpectedReward());
		pg.setExpectedCost(eval.getExpectedCost());
		
		return pg;
	}
	
	public void setTerminateTime(double t) {
		this.terminateTime = t;
	}
	
	public void increaseRuntime(double t) {
		this.terminateTime += t;
	}
	
	public String getName() {
		return "FiniteVI";
	}
	
	public void enableDumpPolicyGraph() {
		this.dumpPolicyGraph = true;
	}
	
	public POMDPAgentSolutionPolicyBased getNoConsumptionSolution(CPOMDP cpomdp, int maxT, int a) {
		this.pomdp = cpomdp;
		this.T = maxT;
		return computePolicyGraphSingleAction(a);
	}
	
	public ArrayList<ArrayList<AlphaVector>> getVectorSets() {
		return vectorSets;
	}
}
