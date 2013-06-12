package stead.alistair.com.soundcoder;

public class Coordinate {
	public float x, y;

	public Coordinate(float xInput, float yInput) {
		x = xInput;
		y = yInput;
	}

	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	public boolean equals(Coordinate otherCoordinate) {
		if (otherCoordinate.x == x && otherCoordinate.y == y)
			return true;
		else
			return false;

	}

}
