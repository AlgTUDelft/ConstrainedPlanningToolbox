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

public class Location {

	private final int locationID;
	private final int moveResource;
	private final int x;
	private final int y;

	private int stateID;
	private Task task;

	public Location(int id, int x, int y, int resourceID) {
		assert (id >= 0);

		locationID = id;
		stateID = locationID;
		moveResource = resourceID;

		this.x = x;
		this.y = y;

		task = null;
	}

	public void setID(int id) {
		stateID = id;
	}

	public void setTask(int task, int taskRelease, double taskReward) {
		assert (task >= 0);

		this.task = new Task(task, taskRelease, taskReward);
	}

	public int getID() {
		return stateID;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getMoveResource() {
		return moveResource;
	}

	public Task getTask() {
		return task;
	}

	public boolean hasTask() {
		return (task != null);
	}

	@Override
	public String toString() {
		return String.format("%d:(%d,%s)", 
								getID(), 
								getMoveResource(), 
								(hasTask()) ? Integer.toString(getTask().getTaskResource()) : "-");
	}

	@Override
	public int hashCode() {
		return locationID;
	}

	@Override
	public boolean equals(Object that) {
		if (that instanceof Location) {
			return this.equalTo((Location) that);
		}

		return false;
	}

	private boolean equalTo(Location that) {
		return ((this.hashCode()        == that.hashCode()) &&
				(this.getID()           == that.getID()) &&
				(this.getMoveResource() == that.getMoveResource()) &&
				(this.getX()            == that.getX()) &&
				(this.getY()            == that.getY()) &&
				((this.getTask() == null && that.getTask() == null) ||
					this.getTask().equals(that.getTask())));
	}
}
