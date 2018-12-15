package app.domain;

import app.domain.section.SeatedSection;
import app.domain.section.Section;
import app.domain.section.StandingSection;
import app.domain.selection.Selection;
import app.domain.selection.SelectionAdapter;
import app.domain.selection.SelectionVisitor;
import app.domain.shape.Point;
import app.domain.shape.Shape;
import app.domain.shape.ShapeBuilder;
import app.domain.shape.ShapeBuilderFactory;
import app.domain.section.SectionFactory;

import java.util.*;
import java.util.function.BiConsumer;

public class Controller {
    private final Collider collider;
    private final Point cursor = new Point(-1, -1);
    private final Point offset = new Point(15, 15);
    private final HashMap<Mode, BiConsumer<Integer, Integer>> clickActions = new HashMap<>();

    private Room room;
    private Mode mode = Mode.None;
    private UIPanel ui;
    private ShapeBuilder current;
    private double scale = 1.0;
    private Selection selection;
    private Section preSelection;
    private Seat hoveredSeat;
    private Section hoveredSection;
    private boolean seatHovered = false;
    private Timer timer = new Timer();
    private Observer observer;


    public Controller(Collider collider) {
        this.collider = Objects.requireNonNull(collider);
        this.room = new Room(900, 900, new VitalSpace(30, 30));
    }

    public Room getRoom() {
        return room;
    }

    public void setDrawingPanel(UIPanel ui) {
        this.ui = Objects.requireNonNull(ui);
    }

    public void createRoom(double roomWidth, double roomHeight, double vitalSpaceWidth, double vitalSpaceHeight) {
        room = new Room(roomWidth, roomHeight, new VitalSpace(vitalSpaceWidth, vitalSpaceHeight));
    }

    public void save(String path) {
        JSONSerialize.serializeToJson(room, path);
    }

    public void load(String path) {
        room = JSONSerialize.deserializeFromJson(path);
        ui.repaint();
    }

    public double getXCursor () {
        return  cursor.x;
    }

    public double getYCursor () {
        return  cursor.y;
    }

    public double getScale() {
        return scale;
    }

    public void mouseDragged(int x, int y) {
        observer.hideSeatInfo();
        double scaleX = x / scale;
        double scaleY = y / scale;
        double dx = scaleX - cursor.x;
        double dy = scaleY - cursor.y;
        cursor.set(scaleX, scaleY);

        if (selection != null) {
            selection.accept(new SelectionVisitor() {
                @Override
                public void visit(Stage stage) {
                    move(stage);
                    autoSetSeat();
                }

                @Override
                public void visit(SeatedSection section) {
                    move(section);
                    autoSetSeat();
                }

                @Override
                public void visit(StandingSection section) {
                    move(section);
                }

                @Override
                public void visit(Seat seat) {
                    move(preSelection);
                }

                @Override
                public void visit(SeatSection seatSection) {
                    move(preSelection);
                }

                private void move(Selection selection) {
                    if (isMovable(selection.getShape(), scaleX, scaleY)) {
                        selection.move(scaleX, scaleY, offset);
                    }
                }
            });
        } else {
            offset.offset(dx, dy);
        }
        ui.repaint();
    }
    public void mouseClicked(int x, int y) {
        observer.hideSeatInfo();
        if (room == null) {
            return;
        }
        double scaleX = x / scale;
        double scaleY = y / scale;
        if (mode == Mode.None || mode == Mode.Selection) {
            doSelection(scaleX, scaleY);
        } else {
            doShape(scaleX, scaleY);
        }
        ui.repaint();
    }

    public void mouseMoved(int x, int y) {
        seatHovered = false;
        double scaleX = x / scale;
        double scaleY = y / scale;
        cursor.set(scaleX, scaleY);
        for (Section s: room.getSections()){
            s.forEachSeats( seat -> {
                if (selectionCheck(scaleX,scaleY,seat.getShape())){
                    seatHovered = true;
                    if (hoveredSeat!=seat) {
                        observer.hideSeatInfo();
                        hoveredSection=s;
                        hoveredSeat=seat;
                        timerReset();
                    }
                }
            });
        }
        if (!seatHovered){
            observer.hideSeatInfo();
            timer.cancel();
            timer.purge();
            timer = new Timer();
        }
        ui.repaint();
    }

    public void mouseWheelMoved(double rotation) {
        this.scale += (0.1 *-(rotation));
        ui.repaint();
    }

    public void zoom(double scale) {
        this.scale += scale;
        ui.repaint();
    }

    public boolean toggleMode(Mode mode) {
        if (room == null) {
            return false;
        }
        current = null;
        if (this.mode == mode) {
            this.mode = Mode.None;
            return false;
        }
        this.mode = Objects.requireNonNull(mode);
        resetSelection();
        ui.repaint();
        return true;
    }

    public Optional<ShapeBuilder> getCurrent() {
        return Optional.ofNullable(current);
    }

    public Mode getMode() {
        return mode;
    }

    public Point getOffset() {
        return offset;
    }

    public void editSelected(SelectionVisitor visitor) {
        if (selection == null) {
            return;
        }
        selection.accept(visitor);
    }

    public void removeSelected() {
        if (selection == null) {
            return;
        }
        selection.accept(new SelectionAdapter() {
            @Override
            public void visit(Stage stage) {
                room.setStage(null);
                mode = Mode.None;
            }

            @Override
            public void visit(SeatedSection section) {
                room.getSections().remove(section);
            }

            @Override
            public void visit(StandingSection section) {
                room.getSections().remove(section);
            }
        });
        ui.repaint();
    }

    public void rotateSelected(boolean direction) {
        if (selection == null) {
            return;
        }
        selection.accept(new SelectionAdapter() {
            @Override
            public void visit(Stage stage) {
                rotate(stage);
                autoSetSeat();
            }

            @Override
            public void visit(SeatedSection section) {
                rotate(section);
                autoSetSeat();
            }

            @Override
            public void visit(StandingSection section) {
                rotate(section);
            }

            private void rotate(Selection select){
                if (isRotatable(select.getShape(), direction)){
                    if (direction){
                        select.rotate(Math.PI/32);
                    } else {
                        select.rotate(-Math.PI/32);
                    }
                }
            }
        });

        ui.repaint();
    }
    public boolean isAutoSelected(){
        if (selection == null) {
            return false;
        }
        return selection.isAuto();
    }

    public void autoSetSeatSelected(){
        if (selection == null) {
            return;
        }
        selection.accept(new SelectionAdapter() {
            @Override
            public void visit(SeatedSection section) {
                section.autoSetSeat=!section.autoSetSeat;
            }
        });
    }

    public void autoSetSeat() {
        for (Section s: room.getSections()){
            if(!room.getStage().isPresent()){
                return;
            }
            s.accept(new SelectionAdapter() {
                @Override
                public void visit(SeatedSection section) {
                    if (section.autoSetSeat){
                        section.autoSetSeats(room.getStage().get(),collider);
                    }
                }
            });
        }
        ui.repaint();
    }

    public void createRegularSection(int x, int y, int xInt, int yInt) {
        if (room != null && room.getStage().isPresent()) {
            Section section = SeatedSection.create(x - offset.x, y - offset.y, xInt, yInt, room.getVitalSpace(), room.getStage().get());
            if (!room.validShape(section.getShape(), new Point())) {
                return;
            }
            if (room.getStage().isPresent()) {
                if (collider.hasCollide(room.getStage().get().getShape(), section.getShape())) {
                    return;
                }
            }
            for (Section s : room.getSections()) {
                if (collider.hasCollide(s.getShape(), section.getShape())) {
                    return;
                }
            }
            room.addSection(section);
            mode = Mode.None;
        }
    }

    private void doSelection(double x, double y) {
        mode = Mode.None;
        Selection s = selection;
        resetSelection();
        if (s != null) {
            s.accept(new SelectionAdapter() {
                @Override
                public void visit(SeatedSection section) {
                    section.forEachSeats(seat -> {
                        if (selectionCheck(x, y, seat.getShape())) {
                            selection = seat;
                            selection.setSelected(true);
                            preSelection.setSelected(true);
                        }
                    });
                }

                @Override
                public void visit(Seat seat) {
                    Seat[] seats = preSelection.getSeats()[seat.getRow()];
                    for (Seat s : seats) {
                        if (selectionCheck(x, y, s.getShape())) {
                            selection = new SeatSection(seats);
                            selection.setSelected(true);
                            preSelection.setSelected(true);
                        }
                    }
                }
            });
            return;
        }
        Optional<Stage> opt = room.getStage();
        if (opt.isPresent()) {
            Stage stage = opt.get();
            if (selectionCheck(x, y, stage.getShape())) {
                selection = stage;
                selection.setSelected(true);
                return;
            }
        }
        for (Section section : room.getSections()) {
            if (selectionCheck(x, y, section.getShape())) {
                selection = section;
                selection.setSelected(true);
                preSelection = section;
                return;
            }
        }
    }

    private void doShape(double x, double y) {
        if (current == null) {
            current = ShapeBuilderFactory.create(mode);
        }
        current.addPoint(new Point(x, y));
        if (current.isComplete()) {
            createShape();
        }
    }

    private void createShape() {
        current.correctLastPoint();
        Shape shape = current.build();
        current = null;
        if (!room.validShape(shape, offset)) {
            return;
        }
        if (room.getStage().isPresent()) {
            if (collider.hasCollide(room.getStage().get().getShape(), shape)) {
                return;
            }
        }
        for (Section section : room.getSections()) {
            if (collider.hasCollide(shape, section.getShape())) {
                return;
            }
        }
        for (Point p : shape.getPoints()) {
            p.x -= offset.x;
            p.y -= offset.y;
        }
        if (mode == Mode.Stage) {
            room.setStage(new Stage(shape));
        } else {
            Section s = SectionFactory.create(mode, shape, room.getVitalSpace());
            if (mode == Mode.IrregularSeatedSection){s.autoSetSeats(room.getStage().get(), collider);}
            room.addSection(s);
        }
        mode = Mode.None;
    }

    private boolean isMovable(Shape shape, double x, double y) {
        Shape predict = shape.clone();
        predict.move(x, y, offset);
        return validPredictedShape(shape, predict);
    }

    private boolean isRotatable(Shape shape, boolean direction) {
        Shape predict = shape.clone();
        if (direction){
            predict.rotate(Math.PI/32,predict.computeCentroid());
        } else {
            predict.rotate(31*Math.PI/32,predict.computeCentroid());
        }
        return validPredictedShape(shape, predict);
    }

    private boolean validPredictedShape(Shape shape, Shape predict){
        if (!room.validShape(predict, new Point())) {
            return false;
        }
        if (room.getStage().isPresent() && shape != room.getStage().get().getShape()) {
            if (collider.hasCollide(room.getStage().get().getShape(), predict)) {
                return false;
            }
        }
        for (Section section : room.getSections()) {
            if (section.getShape() != shape && collider.hasCollide(section.getShape(), predict)) {
                return false;
            }
        }
        return true;
    }

    private boolean selectionCheck(double x, double y, Shape shape){
        if (collider.hasCollide(x - offset.x, y - offset.y, shape)){
            mode = Mode.Selection;
            return true;
        }
        return false;
    }

    private void resetSelection() {
        if (selection != null) {
            selection.setSelected(false);
            selection = null;
        }
        room.getStage().ifPresent(stage -> stage.setSelected(false));
        room.getSections().forEach(section -> {
            section.setSelected(false);
            section.forEachSeats(seat -> seat.setSelected(false));
        });
    }

    public void autoScaling(int panelWidth, int panelHeight) {
        double maxRoom;
        int maxPanel;
        double roomWidth = room.getWidth();
        double roomHeight = room.getHeight();
        if (roomHeight > roomWidth || roomWidth < 2 * roomHeight) {
            maxRoom = roomHeight;
            maxPanel = panelHeight;
        } else {
            maxRoom = roomWidth;
            maxPanel = panelWidth;
        }
        scale = (0.8 * maxPanel) / maxRoom;
        offset.x = (int)(((panelWidth - roomWidth * scale) / 2) / scale);
        offset.y = (int)(((panelHeight - roomHeight * scale) / 2) / scale);
        ui.repaint();
    }

    public boolean validateSectionDimensions(Section section, int nbColums, int nbRows, double spaceWidth, double spaceHeight) {
        VitalSpace vs = new VitalSpace(spaceWidth, spaceHeight);
        Section predict = SeatedSection.create(section.getShape().getPoints().firstElement().x, section.getShape().getPoints().firstElement().y, nbColums, nbRows, vs, room.getStage().get());
        if (!room.validShape(predict.getShape(), new Point())) {
            return false;
        }
        if (room.getStage().isPresent()) {
            if (collider.hasCollide(room.getStage().get().getShape(), predict.getShape())) {
                return false;
            }
        }
        for (Section s : room.getSections()) {
            if (!s.equals(section)) {
                if (collider.hasCollide(s.getShape(), predict.getShape())) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean validateStageDimensions(Stage stage, double width, double height) {
        Shape shape = stage.getShape().clone();
        Stage predict = new Stage(shape);
        predict.setWidth(width);
        predict.setHeight(height);
        if (!room.validShape(predict.getShape(), new Point())) {
            return false;
        }
        for (Section s : room.getSections()) {
            if (collider.hasCollide(s.getShape(), predict.getShape())) {
                return false;
            }
        }
        return true;
    }

    public void setObserver(Observer obs){
        observer=obs;
    }
    private void timerReset(){
        timer.cancel();
        timer.purge();
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                observer.displaySeatInfo();
            }
        };
        timer.schedule(task, 500);
    }

    public Seat getHoveredSeat() {
        return hoveredSeat;
    }

    public Section getHoveredSection(){
        return hoveredSection;
    }
}
