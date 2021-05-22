package ghipe1.Server;


import ghipe1.Utils.Coordinate;

/**
 * Hajó osztály
 * Tartalmazza a hajó hosszát(2-3-4-5)
 * Az orientációját(horizontal-vertical)
 * Illetve a kezdeti cellájáról egy mezőt
 */
public class Ship {
    private final int length;
    private boolean vertorientation;
    private Coordinate firstCell;

    public Ship(int length, boolean orientation) {
        this.length = length;
        this.vertorientation = orientation;
    }

    //Getterek-setterek

    public boolean getOrientation() {
        return vertorientation;
    }

    public int getLength() {
        return length;
    }

    public void setFirstCell(int x, int y) { this.firstCell = new Coordinate(x,y);}

}
