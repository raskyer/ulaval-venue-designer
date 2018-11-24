package app.domain.section;

import app.domain.Seat;
import app.domain.VitalSpace;
import app.domain.shape.Point;
import app.domain.shape.Rectangle;
import app.domain.shape.Shape;

import java.util.Objects;
import java.util.Vector;

public final class SeatedSection extends AbstractSection {
    private VitalSpace vitalSpace;
    private Vector<Seat> seats;

    SeatedSection(String name, int elevation, Shape shape, VitalSpace vitalSpace) {
        super(name, elevation, shape);
        this.vitalSpace = vitalSpace;
    }

    public static SeatedSection create(int x, int y, int column, int row, VitalSpace vitalSpace) {
        Objects.requireNonNull(vitalSpace);

        Vector<Point> points = new Vector<>();
        points.add(new Point(x, y));
        points.add(new Point(x + (column * vitalSpace.getWidth()), y));
        points.add(new Point(x + (column * vitalSpace.getWidth()), y + (row * vitalSpace.getHeight())));
        points.add(new Point(x, y + (row * vitalSpace.getHeight())));
        Rectangle rectangle = new Rectangle(points);

        SeatedSection section = new SeatedSection(null, 0, rectangle, vitalSpace);
        section.seats = new Vector<Seat>();

        for (int i = 0; i < column; i++) {
            for (int j = 0; j < row; j++) {
                section.seats.add( new Seat(i, j, vitalSpace, points.elementAt(0)));
            }
        }

        return section;
    }

    public Vector<Seat> getSeats() {
        return seats;
    }
}
