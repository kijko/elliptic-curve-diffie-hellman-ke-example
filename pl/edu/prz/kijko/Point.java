package pl.edu.prz.kijko;


public class Point {

    public long x;
    public long y;

    public Point() {
        this.x = 0;
        this.y = 0;
    }

    public Point(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
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
