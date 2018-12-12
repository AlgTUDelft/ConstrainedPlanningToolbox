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

public class Task {
	private final int taskResource;
	private final int taskRelease;
	private final double taskReward;

	public Task(int taskResource, int taskRelease, double taskReward) {
		this.taskResource = taskResource;
		this.taskRelease = taskRelease;
		this.taskReward = taskReward;
	}

	public int getTaskResource() {
		return taskResource;
	}

	public int getTaskRelease() {
		return taskRelease;
	}

	public double getTaskReward() {
		return taskReward;
	}

	public boolean isAvailable(int time) {
		return (time >= taskRelease && time < taskRelease + 3);
	}

	@Override
	public boolean equals(Object that) {
		if (that instanceof Task) {
			return this.equalTo((Task) that);
		}

		return false;
	}

	private boolean equalTo(Task that) {
		return ((this.getTaskResource() == that.getTaskResource()) &&
				(this.getTaskRelease()  == that.getTaskRelease())  &&
				(this.getTaskReward()   == that.getTaskReward()));
	}
}
