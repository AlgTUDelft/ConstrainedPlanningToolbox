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
package domains.maze;

import java.util.Random;
import java.util.Set;

import domains.maze.data.GridWorld;
import domains.maze.data.Location;


public class MazeGenerator {

	private double wallProbability;
	private double taskProbability;

	private Random rndGen;

	public MazeGenerator(long seed) {
		rndGen = new Random(seed);

		wallProbability = 0.4;
		taskProbability = 0.1;
	}

	public GridWorld generateGrid(int n, int numResources, int numDecisions) {
		GridWorld maze = new GridWorld(n, numResources);

		int moveResource = -1, taskResource = -1, taskRelease = -1;
		double taskReward = 0;
		for (int x = 0; x < n; x++) {
			for (int y = 0; y < n; y++) {
				if (rndGen.nextDouble() >= wallProbability) {
					moveResource = rndGen.nextInt(numResources);

					maze.addLocation(x, y, moveResource);

					if (rndGen.nextDouble() < taskProbability) {
						taskResource = rndGen.nextInt(numResources);
						taskRelease = rndGen.nextInt(numDecisions-2);
						taskReward = rndGen.nextDouble();

						maze.addTask(x, y, taskResource, taskRelease, taskReward);
					}
				}
			}
		}

		int startX = (n-1)/2;
		int startY = (n-1)/2;
		Set<Location> reachable = maze.getReachable(startX, startY);

		if (reachable.size() > n*n/2) {

			maze.setInitialLocation(startX, startY, reachable);

			return maze;
		}
		
		return generateGrid(n, numResources, numDecisions);
	}
}
