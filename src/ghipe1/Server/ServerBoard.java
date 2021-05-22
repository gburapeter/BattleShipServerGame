package ghipe1.Server;


/**
 * Szerver oldali Board
 * Tartalmazza az össz hajük számát
 * És a tábla méretét(ami kiszámolódik még korábban a kliens által megadott adatokból)
 * Emellett egy 2D-s arrayt ami a cellákaz tartalmazza
 */
public class ServerBoard {

    private int totalShips;
    private int boardsize;

    private ServerCell[][] board = null;

    /**
     * Beállitódnak a mezők, illetve létrehozódik az új tábla,
     * boardsize x boardsize méretü
     * @param boardsize
     * @param totalShips
     */
    public ServerBoard(int boardsize, int totalShips){
        this.boardsize=boardsize;
        this.totalShips=totalShips;

        if (this.board == null) {
            this.board = new ServerCell[boardsize][boardsize];
        }
        for(int i=0;i<boardsize;i++){
            for(int j=0;j<boardsize;j++){
                board[i][j] = new ServerCell();
            }
        }

    }

    // Getter-setterek
    public int getBoardsize() {
        return boardsize;
    }

    public int getTotalShips() {
        return totalShips;
    }

    public void setTotalShips(int totalShips) {
        this.totalShips = totalShips;
    }


    /**
     * Adott pozición levő cellát adja vissza
     * @param x
     * @param y
     * @return
     */
    public ServerCell getCell(int x, int y){
        return this.board[y][x];
    }


    /**
     * Megadja, hogy az adott pont a táblán helyezkedik e el, vagy kivüle
     * @param x
     * @param y
     * @return
     */
    private boolean isPointValid(int x, int y) {
        return x >= 0 && x < boardsize && y >= 0 && y < boardsize;
    }


    /**
     * Visszaadja, hogy elhelyezhető e az adott hajó, a megfelelő pozicióra
     * @param shipToPlace Hajó amit elhelyeznénk
     * @param x x koordináta
     * @param y y koordináta
     * @return
     */

    private boolean isPlacementPossible(Ship shipToPlace, int x, int y){
        //Lekérjük a hajó hosszát
        int length = shipToPlace.getLength();

        //Meghatározzuk, hogy milyen az elhelyezkedése(vertical,horizontal)
        if (shipToPlace.getOrientation()) {
            for (int i = y; i < y + length; i++) {

                //Ha nem maradna a táblán, akkor false
                if (!isPointValid(x, i))
                    return false;

                //Ha nem diszjunkt akkor false
                if (getCell(x,i).getShip()!= null)
                    return false;
            }

        }

        //Ugyanez, csak a horizontal esetben
        else{
            for (int i = x; i < x + length; i++) {
                if (!isPointValid(i, y))
                    return false;

                ServerCell cell = getCell(i, y);
                if (cell.getShip() != null)
                    return false;
            }

        }
        //Ha semmi nem dobott falset, akkor elhelyezhető a hajó
        return true;
    }

    /**
     * Visszaadja, hogy hajó-e az adott (x,y) koordináta pár
     * @param x
     * @param y
     * @return
     */
    public boolean checkIfMissed(int x,int y) {
        return (!(getCell(x,y).isShip()));
    }

    /**
     * Hajót helyez az adott mezőre, amiután ellenőrizte, hogy ez megtehető-e
     * @param shipToPlace
     * @param x
     * @param y
     * @return
     */
    public boolean placeShip(Ship shipToPlace, int x, int y){
        // Ha elhelyezhető
        if(isPlacementPossible(shipToPlace, x, y)){
            shipToPlace.setFirstCell(x,y);
            if(shipToPlace.getOrientation()){
                //Akkor a hosszáig megfelelő mezőkre Ship-et helyez
                for(int i=y; i<y+shipToPlace.getLength(); i++){
                    this.getCell(x,i).placeShip(shipToPlace);
                }
            }
            else{ //horizontal esetben ugyanez
                for(int i=x; i<x+shipToPlace.getLength(); i++){
                    this.getCell(i,y).placeShip(shipToPlace);
                }
            }
            return true;
        }
        return false;
    }

}