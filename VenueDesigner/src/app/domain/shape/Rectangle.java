package app.domain.shape;

import app.domain.section.Zone;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Vector;

public final class Rectangle extends AbstractShape {
    public static class Builder implements ShapeBuilder {
        private final Vector<Point> points = new Vector<>();
        private final int[] color = new int[4];

        Builder() {}

        Builder(int r, int g, int b, int a) {
            color[0] = r;
            color[1] = g;
            color[2] = b;
            color[3] = a;
        }

        @Override
        public void addPoint(Point point) {
            if (points.isEmpty()) {
                points.add(point);
            } else {
                points.add(new Point(point.x, points.firstElement().y));
                points.add(point);
                points.add(new Point(points.firstElement().x, point.y));
            }
        }

        @Override
        public boolean isComplete() {
            return points.size() == 4;
        }

        @Override
        public void correctLastPoint() {}

        @Override
        public Rectangle build() {
            return new Rectangle(points, color);
        }

        @Override
        public Vector<Point> getPoints() {
            return points;
        }

        @Override
        public <T> void accept(T g, Painter<T> painter) {
            painter.draw(g, this);
        }
    }
    @JsonCreator
    public Rectangle(@JsonProperty("points") Vector<Point> points,  @JsonProperty("color") int[] color) { super(points, color); }

    private Rectangle(Rectangle rectangle) {
        super(rectangle);
    }

    public static Rectangle create(int x, int y, int width, int height, int[] color) {
        Vector<Point> points = new Vector<>();
        points.add(new Point(x, y));
        points.add(new Point(x + width, y));
        points.add(new Point(x + width, y + height));
        points.add(new Point(x, y + height));

        return new Rectangle(points, color);
    }

    public static Rectangle create(int x, int y, int width, int height, int[] color, Zone zone) {
        Vector<Point> points = new Vector<>();
        switch (zone){
            case Bas:{
                return Rectangle.create(x,y,width,height,color);
            }
            case Gauche:{
                points.add(new Point(x, y));
                points.add(new Point(x, y+width));
                points.add(new Point(x-height, y +width));
                points.add(new Point(x-height, y ));
                break;
            }
            case Haut:{
                points.add(new Point(x, y));
                points.add(new Point(x-width, y));
                points.add(new Point(x-width, y -height));
                points.add(new Point(x, y -height));
                break;
            }
            case Droit:{
                points.add(new Point(x, y));
                points.add(new Point(x, y-width));
                points.add(new Point(x+height, y -width));
                points.add(new Point(x+height, y ));
                break;
            }
        }
        return new Rectangle(points, color);
    }

    @Override
    public <T> void accept(T g, Painter<T> painter) {
        painter.draw(g, this);
    }

    @Override
    public Shape clone() {
        return new Rectangle(this);
    }
}
