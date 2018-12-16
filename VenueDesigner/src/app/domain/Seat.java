package app.domain;

import app.domain.section.Zone;
import app.domain.selection.Selection;
import app.domain.selection.SelectionVisitor;
import app.domain.shape.Point;
import app.domain.shape.Rectangle;
import app.domain.shape.Shape;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Vector;

public final class Seat implements Selection {
    private final int column;
    private final int row;
    private final Shape shape;

    private int price;
    private double discountPrice;
    private int color;

    public Seat(int column, int row, VitalSpace vs, Point p0) {
        this.column = column;
        this.row = row;
        Vector<Point> points = new Vector<>();
        points.add(new Point(p0.x+(column)*vs.getWidth(), p0.y+(row)*vs.getHeight()));
        points.add(new Point(p0.x+(column+1)*vs.getWidth(), p0.y+(row)*vs.getHeight()));
        points.add(new Point(p0.x+(column+1)*vs.getWidth(), p0.y+(row+1)*vs.getHeight()));
        points.add(new Point(p0.x+(column)*vs.getWidth(), p0.y+(row+1)*vs.getHeight()));
        shape = new Rectangle(points, new int[4]);
    }

    public Seat(int row, int column, VitalSpace vs, Point p0, Zone zone) {
        this.column = column;
        this.row = row;
        Vector<Point> points = new Vector<>();
        int x=0;
        int y=0;
        switch (zone){
            case Down:{
                x= p0.x+(column)*vs.getWidth();
                y=p0.y+(row)*vs.getHeight();
                break;
            }
            case Left:{
                x= p0.x-(row)*vs.getHeight();
                y=p0.y+(column)*vs.getWidth();
                break;
            }
            case Up:{
                x= p0.x-(column)*vs.getWidth();
                y=p0.y-(row)*vs.getHeight();
                break;
            }
            case Right:{
                x= p0.x+(row)*vs.getHeight();
                y=p0.y-(column)*vs.getWidth();
                break;
            }
        }
        int[] color = {0,0,0,255};
        shape = Rectangle.create(x,y,vs.getWidth(),vs.getHeight(), color, zone);
    }

    @JsonCreator
    public Seat(@JsonProperty("column") int column, @JsonProperty("row") int row, @JsonProperty("shape") Shape shape) {
        this.column = column;
        this.row = row;
        this.shape = shape;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public double getDiscountPrice(){return discountPrice;}

    public void setDoublePrice(double price){this.discountPrice = price;}

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public boolean isSelected() {
        return shape.isSelected();
    }

    @Override
    public void setSelected(boolean selected) {
        shape.setSelected(selected);
    }

    @Override
    public void move(int x, int y) {
        shape.move(x, y);
    }

    @Override
    public void move(int x, int y, Point offset){
        shape.move(x, y, offset);
    }

    @Override
    public Shape getShape(){
        return shape;
    }

    @Override
    public void accept(SelectionVisitor visitor) {
        visitor.visit(this);
    }
}
