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

public enum Direction {

	NORTH(MazeAction.NORTH, MazeAction.SAFE_NORTH,  0, +1),
	EAST (MazeAction.EAST,  MazeAction.SAFE_EAST,  +1,  0),
	SOUTH(MazeAction.SOUTH, MazeAction.SAFE_SOUTH,  0, -1),
	WEST (MazeAction.WEST,  MazeAction.SAFE_WEST,  -1,  0);

	private final MazeAction regularMove;
	private final MazeAction safeMove;
	private final int xOffset;
	private final int yOffset;

	private Direction(MazeAction regular, MazeAction safe, int xOffset, int yOffset) {
		this.regularMove = regular;
		this.safeMove = safe;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}

	public MazeAction getRegularAction() {
		return regularMove;
	}

	public MazeAction getSafeAction() {
		return safeMove;
	}

	public int getXoffset() {
		return xOffset;
	}

	public int getYoffset() {
		return yOffset;
	}
}
