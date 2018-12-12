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
package domains.maze.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GridWorld {

	private int numLocations;
	private int numResources;
	private Location outOfWorld;
	private Location initialLocation;
	private Location[][] maze;

	private List<Location> locations;

	public GridWorld(int n, int nResources) {
		maze = new Location[n][n];
		locations = new ArrayList<Location>();

		numResources = nResources;
		numLocations = 0;
		outOfWorld = createLocation(-2, -2, -1); // -2 so that for all directions, hasWall(outOfWorld) == false.
		initialLocation = outOfWorld;
	}

	private Location createLocation(int x, int y, int moveResource) {
		return new Location(numLocations++, x, y, moveResource);
	}

	public void addLocation(int x, int y, int moveResource) {
		maze[x][y] = createLocation(x, y, moveResource);
	}

	public void addTask(int x, int y, int taskResource, int taskRelease, double taskReward) {
		maze[x][y].setTask(taskResource, taskRelease, taskReward);
	}

	public void setInitialLocation(int startX, int startY, Set<Location> reachable) {
		locations.add(outOfWorld);

		for (int x = 0; x < maze.length; x++) {
			for (int y = 0; y < maze[x].length; y++) {
				if (reachable.contains(maze[x][y])) {
					maze[x][y].setID(locations.size());
					locations.add(maze[x][y]);
				} else {
					maze[x][y] = null;
				}
			}
		}

		initialLocation = maze[startX][startY];
	}

	public int getSize() {
		return maze.length;
	}

	public int getNumLocations() {
		return locations.size();
	}

	public int getNumResources() {
		return numResources;
	}

	public Location getLocation(int pos) {
		return locations.get(pos);
	}

	public Location getInitialLocation() {
		return initialLocation;
	}

	public Location getOutOfWorld() {
		return outOfWorld;
	}

	public boolean hasWall(int x, int y) {
		return (x < 0 || x >= maze.length    ||
				y < 0 || y >= maze[x].length || 
				maze[x][y] == null);
	}

	public boolean hasDirection(Location location, Direction direction) {
		return !hasWall(location.getX() + direction.getXoffset(), 
						location.getY() + direction.getYoffset());
	}

	public Location getDirection(Location location, Direction direction) {
		return maze[location.getX() + direction.getXoffset()][location.getY() + direction.getYoffset()];
	}

	public Location getLocation(int x, int y) {
		assert (!hasWall(x,y));

		return maze[x][y];
	}

	public Set<Location> getReachable(int x, int y) {
		Set<Location> reachable = new HashSet<>();

		if (!hasWall(x,y)) {
			reachable.add(getLocation(x, y));

			reachable = findReachable(reachable);
		}

		return reachable;
	}

	private Set<Location> findReachable(Set<Location> openSet) {
		Set<Location> reachable = new HashSet<>();
		Set<Location> newOpenSet = new HashSet<>();

		while (!openSet.isEmpty()) {
			reachable.addAll(openSet);

			for (Location location : openSet) {
				int x = location.getX();
				int y = location.getY();

				checkReachable(reachable, newOpenSet, x-1, y  );
				checkReachable(reachable, newOpenSet, x+1, y  );
				checkReachable(reachable, newOpenSet, x  , y-1);
				checkReachable(reachable, newOpenSet, x  , y+1);
			}

			openSet.clear();
			openSet.addAll(newOpenSet);
			newOpenSet.clear();
		}

		return reachable;
	}

	private void checkReachable(Set<Location> reachable, Set<Location> openSet, int x, int y) {
		if (!hasWall(x, y) && !reachable.contains(getLocation(x, y)))
			openSet.add(getLocation(x, y));
	}

	public String toString() {
		final int n = getSize();

		String result = "";
		for (int y = n-1; y >= 0; y--) {
			for (int x = 0; x < n; x++) {
				result += String.format("%10s", (hasWall(x,y)) ? "  wall  " : getLocation(x,y).toString());
			}
			result += "\n";
		}

		return result;
	}
}
