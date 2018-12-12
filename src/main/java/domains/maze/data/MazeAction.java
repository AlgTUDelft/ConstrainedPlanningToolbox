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

public enum MazeAction {
	WAIT(      0, false, false),
	     NORTH(1, true,  false),
	SAFE_NORTH(2, true,  true),
	     EAST( 3, true,  false),
	SAFE_EAST( 4, true,  true),
	     SOUTH(5, true,  false),
	SAFE_SOUTH(6, true,  true),
	     WEST( 7, true,  false),
	SAFE_WEST( 8, true,  true),
	DO_TASK(   9, false, true);

	private final int id;
	private final boolean isMove;
	private final boolean usesResource;

	private MazeAction(int id, boolean move, boolean resource) {
		this.id = id;
		this.isMove = move;
		this.usesResource = resource;
	}

	public int getID() {
		return id;
	}

	public Direction getDirection() {
		Direction direction;

		switch (this) {
			case NORTH: case SAFE_NORTH: direction = Direction.NORTH; break;
			case EAST:  case SAFE_EAST:  direction = Direction.EAST;  break;
			case SOUTH: case SAFE_SOUTH: direction = Direction.SOUTH; break;
			case WEST:  case SAFE_WEST:  direction = Direction.WEST;  break;
			default: direction = null; break;
		}

		return direction;
	}

	public boolean isMoveAction() {
		return isMove;
	}

	public boolean usesResource() {
		return usesResource;
	}
}
