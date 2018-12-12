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
package domains.advertising;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.CMDP;
import domains.CMDPInstanceGenerator;
import instances.CMDPInstance;

public class AdvertisingInstanceGenerator implements CMDPInstanceGenerator {
	
	public CMDPInstance getInstance(int nAgents, int nDecisions) {
		CMDP cmdp = this.getCMDP(nDecisions);
		
		CMDP[] cmdps = new CMDP[nAgents];
		for(int i=0; i<nAgents; i++) {
			cmdps[i] = cmdp;
		}
		
		double[] budget = new double[]{3.0 * ((double) nAgents)};
		
		return CMDPInstance.createBudgetInstance(cmdps, budget, nDecisions);
	}
	
	private CMDP getCMDP(int T) {
		CMDP cmdp = null;
		int[][][] Tstate;
		double[][][] Tprob;
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("./domains/synthetic_ad.txt"));

			int initialState = 0;
			int numStates = Integer.valueOf(reader.readLine());
			int numActions = Integer.valueOf(reader.readLine());

			double[][] costFunction = new double[numStates][numActions];
			Tstate = new int[numStates][numActions][];
			Tprob = new double[numStates][numActions][];
			
			double[][] rewardFunction = new double[numStates][numActions];
			double[][][] transitionFunction = new double[numStates][numActions][numStates];

			// discount factor, unused
			reader.readLine();

			// repeated action description.
			for (int a = 0; a < numActions; a++) {
				int actionID = Integer.valueOf(reader.readLine());
				
				if (a != actionID) {
					throw new RuntimeException("Unexpected action ID (" + a + " != " + actionID + ").");
				}
				
				// define costs
				if (a > 0) {
					for(int s=0; s<numStates; s++) {
						costFunction[s][a] = (1<<(a-1));
					}
				}
				
				// define reward
				rewardFunction[9][a] = 200.0;

				String[] chunks = null;
				for (int s = 0; s < numStates; s++) {
					chunks = reader.readLine().split(" ");

					int stateID = Integer.valueOf(chunks[0]);
					if (s != stateID) {
						throw new RuntimeException("Unexpected state ID (" + s + " != " + stateID + ").");
					}

					int numDests = (chunks.length-1)/2;
					Tstate[s][a] = new int[numDests];
					Tprob[s][a] = new double[numDests];

					for (int i = 0; i < numDests; i++) {
						int sNext = Integer.valueOf(chunks[1+(i*2)].substring(1));
						double prob = Double.valueOf(chunks[2+(i*2)].substring(0, chunks[2+(i*2)].length()-1));

						Tstate[s][a][i] = sNext;
						Tprob[s][a][i] = prob;
						
						transitionFunction[s][a][sNext] = prob;
					}
				}

				// Rewards, unused.
				reader.readLine();

				// Resource costs, unused.
				reader.readLine();
				
				List<double[][]> costFunctions = new ArrayList<double[][]>();
				costFunctions.add(costFunction);
				
				cmdp = new CMDP(numStates, numActions, costFunctions, initialState, T);
				cmdp.setRewardFunction(rewardFunction);
				cmdp.setTransitionFunction(Tstate, Tprob);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) { }
			}
		}
		
		return cmdp;
	}
}
