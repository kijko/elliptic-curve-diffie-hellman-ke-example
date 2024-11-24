package pl.edu.prz.kijko;


public class Point {
	public static final Point INFINITY = new Point();

    public long x;
    public long y;
	private boolean infinity;

    public Point() {
        this.x = 0;
        this.y = 0;
		this.infinity = true;
    }

    public Point(Point point) {
        this.x = point.x;
        this.y = point.y;
		this.infinity = point.isInfinity();
    }

    public Point(long x, long y) {
        this.x = x;
        this.y = y;
		this.infinity = false;
    }

	public boolean isInfinity() {
		return this.infinity;
	}


    public String toString() {
        return "(" + x + ", " + y + ")"; 
    }
        // Equals method to compare two Point objects
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true; // Same object reference
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false; // Null or different class
        }
        Point other = (Point) obj;
        return this.x == other.x && this.y == other.y; // Compare coordinates
    }

    // hashCode method for hash-based collections
    @Override
    public int hashCode() {
        return Long.hashCode(x) * 31 + Long.hashCode(y);
    }


}
