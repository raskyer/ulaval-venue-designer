package app.domain.section;

import app.domain.Seat;
import app.domain.VitalSpace;
import app.domain.shape.Painter;
import app.domain.shape.Point;
import app.domain.shape.Rectangle;
import app.domain.shape.Shape;

import java.util.Objects;
import java.util.Vector;

public final class SeatedSection extends AbstractSection {
    private final VitalSpace vitalSpace;
    private final Vector<Seat> seats;

    SeatedSection(String name, int elevation, Shape shape, VitalSpace vitalSpace) {
        super(name, elevation, shape);
        this.vitalSpace = vitalSpace;
        this.seats = new Vector<>();
    }

    public static SeatedSection create(int x, int y, int column, int row, VitalSpace vitalSpace) {
        Objects.requireNonNull(vitalSpace);

        Vector<Point> points = new Vector<>();
        points.add(new Point(x, y));
        points.add(new Point(x + (column * vitalSpace.getWidth()), y));
        points.add(new Point(x + (column * vitalSpace.getWidth()), y + (row * vitalSpace.getHeight())));
        points.add(new Point(x, y + (row * vitalSpace.getHeight())));
        Rectangle rectangle = new Rectangle(points, new int[3]);

        SeatedSection section = new SeatedSection(null, 0, rectangle, vitalSpace);

        for (int i = 0; i < column; i++) {
            for (int j = 0; j < row; j++) {
                section.seats.add(new Seat(i, j, vitalSpace, points.elementAt(0)));
            }
        }

        return section;
    }

    @Override
    public  void move(int x, int y, Point offset) {
        Shape shape = getShape();
        for (Seat seat : seats){
            Point sectionCenter = shape.computeCentroid();
            Point seatCenter = seat.getShape().computeCentroid();
            int dx = sectionCenter.x - seatCenter.x;
            int dy = sectionCenter.y - seatCenter.y;
            seat.move(x + dx,y+dy, offset);
        }
        shape.move(x, y, offset);
    }

    @Override
    public <T> void accept(T g, Painter<T> painter) {
        getShape().accept(g, painter);
        boolean selectionExist = false;
        Seat selectedSeat = new Seat(0,0,new VitalSpace(1,1), new Point());
        for (Seat seat : seats) {
            if (seat.getShape().isSelected()){
                selectionExist=true;
                selectedSeat=seat;
            }
            seat.getShape().accept(g, painter);
        }
        if (selectionExist) {
            selectedSeat.getShape().accept(g, painter);
        }
    }

    @Override
    public Vector<Seat> getSeats(){
        return seats;
    }
}
