package app.domain.shape;

import java.util.Vector;

public final class Polygon extends AbstractShape {
    public static class Builder implements ShapeBuilder {
        private final Vector<Point> points = new Vector<>();
        private final int[] color = new int[3];

        Builder() {}

        Builder(int r, int g, int b) {
            color[0] = r;
            color[1] = g;
            color[2] = b;
        }

        @Override
        public void addPoint(Point point) {
            points.add(point);
        }

        @Override
        public boolean isComplete() {
            return points.size() >= 3 && dist(points.firstElement(), points.lastElement()) <= 10;
        }

        @Override
        public void correctLastPoint() {
            points.removeElementAt(points.size() - 1);
        }

        @Override
        public Shape build() {
            return new Polygon(points, color);
        }

        @Override
        public void setSelected(boolean selected) {}

        @Override
        public void move(int x, int y) {}

        @Override
        public boolean isSelected() {
            return true;
        }

        @Override
        public Vector<Point> getPoints() {
            return points;
        }

        @Override
        public int[] getColor() {
            return color;
        }

        @Override
        public <T> void accept(T g, Painter<T> painter) {
            painter.draw(g, this);
        }

        private double dist(Point p_p1, Point p_p2) {
            int x1 = p_p1.x;
            int x2 = p_p2.x;
            int y1 = p_p1.y;
            int y2 = p_p2.y;

            return Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
        }

        @Override
        public Point computeCentroid(){
            return new Point(-1,-1);
        }
    }

    private Polygon(Vector<Point> points, int[] color) {
        super(points, color);
    }

    @Override
    public <T> void accept(T g, Painter<T> painter) {
        painter.draw(g,this);
    }
}
