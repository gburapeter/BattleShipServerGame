package ghipe1.Utils;

/**
 * Package osztályon keresztül küld a kliens üzeneteket a szervernek
 * Az üzenetet tartalmazza mindig az adott COMMAND enumot,
 * emellett a játék állapotáról infókat
 */
public class Package {

    /**
     * A toString függvényhez, mindent nullra, int-ek -1-re
     */
    private String command;
    private String gameType = null;
    private String gameName = null;
    private String information=null;


    private int ship2 = -1;
    private int ship3 = -1;
    private int ship4 = -1;
    private int ship5 = -1;
    private int x = -1;
    private int y = -1;
    private int length = -1;
    private int nrPlayer = -1;

    /**
     * Különböző konstruktorok a különféle infokat tartalmazó Packagekhez
     * @param command
     * @param information
     * @param x
     * @param y
     * @param length
     */
    public Package(String command, String information, int x, int y, int length) {
        this.command = command;
        this.information = information;
        this.x = x;
        this.y = y;
        this.length = length;
    }

    public Package(String command,int x,int y){
        this.command = command;
        this.x = x;
        this.y = y;
    }

    public Package(String command,String information){
        this.command = command;
        this.information = information;
    }

    public Package(String command, String gameName, String gameType, int ship2, int ship3, int ship4, int ship5, int nrPlayer){
        this.command = command;
        this.gameName = gameName;
        this.gameType = gameType;
        this.ship2=ship2;
        this.ship3=ship3;
        this.ship4=ship4;
        this.ship5=ship5;
        this.nrPlayer=nrPlayer;

    }

    /**
     * Ha a Packageben benne volt az adott mező, akkor annak az értéke nem null és nem is -1 lesz,
     * tehát elküldődik
     * @return
     */
    public String toString(){
        String message = "";
        message = message + this.command +"#";
        if(this.information != null)
            message = message + this.information + "#";
        if(this.gameName != null)
            message = message + this.gameName + "#";
        if(this.gameType != null)
            message = message + this.gameType + "#";
        if(this.x != -1)
            message = message + this.x + "#";
        if(this.y != -1)
            message = message + this.y + "#";
        if (this.length != -1)
            message = message + this.length + "#";
        if (this.ship2 != -1)
            message = message + this.ship2 + "#";
        if (this.ship3 != -1)
            message = message + this.ship3 + "#";
        if (this.ship4 != -1)
            message = message + this.ship4 + "#";
        if (this.ship5 != -1)
            message = message + this.ship5 + "#";
        if (this.nrPlayer != -1)
            message = message + this.nrPlayer + "#";
        return message;
    }
}
