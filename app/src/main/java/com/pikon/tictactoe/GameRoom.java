package com.pikon.tictactoe;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.UUID;

public class GameRoom {
//    private UUID uuid;
    private String roomKey;
    private int[] board;
    private int turn;
    private int turnNum;
    private int lastTileChanged;
    private boolean open;
    public int gameState;

    private DatabaseReference dbRef;

    private final int[][] winningPositions = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8},
    			{0, 3, 6}, {1, 4, 7}, {2, 5, 8},
    			{0, 4, 8}, {2, 4, 6}};


    public GameRoom() {
        this.roomKey = UUID.randomUUID().toString().substring( 0, 7 );
        this.board = new int[9];
        this.turn = 1;
        this.turnNum = 1;
        this.lastTileChanged = -1;
        this.open = true;
        this.dbRef = FirebaseDatabase.getInstance().getReference( "/rooms/" + this.roomKey );
        this.gameState = 0;
        // 0 => none, 1 => x win, -1 => o win, -100 => draw
    }

    public boolean nextTurn( int tile, boolean online )
    {
        if( this.board[tile] != 0 )
            return false;
        this.lastTileChanged = tile;
        this.board[tile] = this.turn;
        this.turn *= -1;
        this.turnNum++;
        int currResult = checkForWin();
        // -100 = draw
        if( turnNum > 9 || currResult != 0 ) {
            this.gameState = currResult;
        }
        if( online )
            this.pushRoomToDB();
        return true;
    }

    public void close()
    {
        this.open = false;
        this.pushRoomToDB();
    }

    public void assignReadRoomData( GameRoom readRoom )
    {
        this.roomKey = readRoom.roomKey;
//        this.board = readRoom.board;
        if( readRoom.getLastTileChanged() != -1 )
            this.board[ readRoom.getLastTileChanged() ] = readRoom.getTurn() * -1;
        this.turn = readRoom.getTurn();
        this.turnNum = readRoom.getTurnNum();
        this.lastTileChanged = readRoom.getLastTileChanged();
        this.open = readRoom.isOpen();
        this.gameState = readRoom.gameState;
        this.dbRef = FirebaseDatabase.getInstance().getReference( "/rooms/" + readRoom.roomKey );
    }

    public void pushRoomToDB()
    {
        Log.d( "DEBUG", "push - " + this.roomKey );
        this.dbRef.setValue( new MappableGameRoom( this.roomKey, this.turn, this.turnNum, this.lastTileChanged, this.open, this.gameState ) );
    }

    private int checkForWin()
    {
        Log.d( "DEBUG", Arrays.toString( board ) );
        for( int[] winCase : winningPositions ) {
            if( board[winCase[0]] == board[winCase[1]] && board[winCase[1]] == board[winCase[2]] && board[winCase[0]] == 1 )
                return 1;
            else if( board[winCase[0]] == board[winCase[1]] && board[winCase[1]] == board[winCase[2]] && board[winCase[0]] == -1 )
                return -1;
        }
        if( turnNum > 9 )
            return -100;
        return 0;
    }

    @Override
    public String toString() {
        return "GameRoom{" +
                "roomKey='" + roomKey + '\'' +
                ", turn=" + turn +
                ", turnNum=" + turnNum +
                ", lastMove=" + lastTileChanged +
                ", open=" + open +
                ", dbRef=" + dbRef +
                ", winningPositions=" + Arrays.toString(winningPositions) +
                '}';
    }

    public String getRoomKey() {
        return roomKey;
    }

    public int getTurn() {
        return turn;
    }

    public int getTurnNum() {
        return turnNum;
    }

    public int getLastTileChanged() {
        return lastTileChanged;
    }

    public DatabaseReference getDbRef() {
        return dbRef;
    }

    public boolean isOpen() {
        return open;
    }
}

