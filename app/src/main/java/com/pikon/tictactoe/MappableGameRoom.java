package com.pikon.tictactoe;

public class MappableGameRoom {
    public String roomKey;
    public int turn;
    public int turnNum;
    public int lastTileChanged;
    public boolean open;
    public int gameState;

    public MappableGameRoom(String roomKey, int turn, int turnNum, int lastTileChanged, boolean open, int gameState) {
        this.roomKey = roomKey;
        this.turn = turn;
        this.turnNum = turnNum;
        this.lastTileChanged = lastTileChanged;
        this.open = open;
        this.gameState = gameState;
    }
}
