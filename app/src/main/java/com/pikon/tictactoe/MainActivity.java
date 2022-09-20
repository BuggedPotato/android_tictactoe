package com.pikon.tictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

	private RelativeLayout rlRoot;
	private TextView tvRoomKey;
	private TextView tvPlayer;
	private int side = 1;
	private boolean online = false;
	// 1 = x, -1 = o
	private GameRoom room;
	private String roomKey;

	private DatabaseReference dbRooms = FirebaseDatabase.getInstance().getReference("/rooms/");

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		Bundle bundle = getIntent().getExtras();
		this.online = bundle.getBoolean( "online" );
		if( !this.online )
			this.room = new GameRoom();
		else {
			this.findRoom();
		}

		rlRoot = findViewById( R.id.rlRoot );
		tvRoomKey = findViewById( R.id.tvRoomKey );
		tvPlayer = findViewById( R.id.tvPlayer );
		generateBoardViews();
	}

	private void findRoom(){
		dbRooms.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				Iterable<DataSnapshot> children = snapshot.getChildren();
				for( DataSnapshot child : children ){
					GameRoom foundRoom = child.getValue( GameRoom.class );
					if( foundRoom == null )
						return;
					if( foundRoom.isOpen() )
					{
						room = new GameRoom();
						room.assignReadRoomData( foundRoom );
						setDBListeners();
						room.close();
						side = -1;
						tvPlayer.setText( "Grasz kółkami" );
						setTurnButtonColour( room.getTurn() == 1 );
						Log.d( "DEBUG", "FOUND ROOM - " + room.toString() );
						return;
					}
				}
				room = new GameRoom();
				setDBListeners();
				tvPlayer.setText( "Grasz krzyżykami" );
				room.pushRoomToDB();
				Log.d( "DEBUG", "NO ROOM FOUND CREATED - " + room.getRoomKey() );
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				Log.w("DEBUG", "Failed to read value.", error.toException());
			}
		});
	}

	private void setDBListeners()
	{
		this.room.getDbRef().addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				Log.w( "DEBUG", "LISTENER" );
				GameRoom readRoom = ( snapshot.getValue( GameRoom.class ) );
				if( readRoom == null )
					return;
				room.assignReadRoomData( readRoom );
				tvRoomKey.setText( room.getRoomKey() );
				setTurnButtonColour( room.getTurn() == 1 );
				int id = room.getLastTileChanged();
				if( id != -1 )
					updateBoard( id );
				Log.d( "DEBUG", "state: " + room.gameState );
				if( room.gameState != 0 )
				{
					showEndGameDialog( room.gameState );
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				Log.e("DEBUG", "Failed to read value.", error.toException());
			}
		});
	}

	private void generateBoardViews() {
		LayoutInflater li = (LayoutInflater) getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		ImageView tmp = new ImageView( getApplicationContext() );
		tmp.setImageResource( R.drawable.o );
		// o and x images original size
		int h = tmp.getDrawable().getIntrinsicHeight();
		int w = tmp.getDrawable().getIntrinsicWidth();

		for( int i = 0; i < 9; i++ ) {
			ImageView img = (ImageView) li.inflate( R.layout.game_field, null );
			img.setTag( i );
			LinearLayout.LayoutParams imgLayoutParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
			// @formatter:off
			int row;
			if( i < 3 ) { row = 1; }
			else if( i < 6 ) { row = 2; }
			else { row = 3; }
			// @formatter:on
			imgLayoutParams.setMargins( 10 + w * ( i % 3 ), 100 + h * ( row - 1 ), 0, 0 );
			img.setLayoutParams( imgLayoutParams );
//			img.setImageResource( i % 2 == 0 ? R.drawable.o : R.drawable.x );
			img.setImageResource( R.drawable.blank );

			img.setOnClickListener( new View.OnClickListener() {
				@Override
				public void onClick( View view ) {
					onTileClick( view );
				}
			} );

			rlRoot.addView( img );
		}
	}

	private void updateBoard( int tile )
	{
		this.setViewImageOnClick( rlRoot.findViewWithTag( tile ), this.room.getTurn() == -1 ); // accomodate for change in data
	}

	private void onTileClick( View view )
	{
		boolean isXTurn = room.getTurn() == 1;
		if( room.getTurn() != this.side && this.online )
		{
			Log.d( "DEBUG", "Interaction blocked!" );
			return;
		}
		if( !room.nextTurn( (int)view.getTag(), this.online ) ) // override block
			return;
		setViewImageOnClick( view, isXTurn );
		view.setEnabled( false );

		if( !online )
		{
			setTurnButtonColour( room.getTurn() == 1 );
			if( room.gameState != 0 )
				showEndGameDialog( room.gameState );
		}
	}

	private void setViewImageOnClick( View view, boolean xTurn )
	{
		ImageView ivView = (ImageView) view;
		if( xTurn ){
			ivView.setImageResource( R.drawable.x );
		}
		else {
			ivView.setImageResource( R.drawable.o );
		}
	}

	private void setTurnButtonColour( boolean xTurn )
	{
		ImageButton red = (ImageButton) findViewById( xTurn ? R.id.ibtnOTurn : R.id.ibtnXTurn );
		ImageButton yellow = (ImageButton) findViewById( xTurn ? R.id.ibtnXTurn : R.id.ibtnOTurn );
		red.setBackground( ResourcesCompat.getDrawable( getResources(), R.drawable.rounded_button_r, null ) );
		yellow.setBackground( ResourcesCompat.getDrawable( getResources(), R.drawable.rounded_button_y, null ) );
	}



	private void showEndGameDialog( int result )
	{
		// result = 1 => x, result = -1 => o, result = 0 => draw
		AlertDialog.Builder bob = new AlertDialog.Builder( this );
		String title;
		if( result == 1 )
			title = "Wygrana krzyżyków!";
		else if ( result == -1 )
			title = "Wygrana kółek!";
		else
			title = "Remis";
		bob.setTitle( title );

		bob.setPositiveButton( "Zagraj jeszcze raz", new DialogInterface.OnClickListener() {
			@Override
			public void onClick( DialogInterface dialogInterface, int i ) {
				restart();
			}
		} );
//		if( this.online )
//		{
//			bob.setNeutralButton("Rewanż!", new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialogInterface, int i) {
//					// TODO
//				}
//			});
//		}
		bob.setNegativeButton( "Do menu", new DialogInterface.OnClickListener() {
			@Override
			public void onClick( DialogInterface dialogInterface, int i ) {
				Intent intent = new Intent( getApplicationContext(), MenuActivity.class );
				startActivity( intent );
			}
		} );
		bob.setCancelable( false );
		bob.show();
	}

	private void restart()
	{
		if( !this.online )
			this.room = new GameRoom();
		else {
			this.findRoom();
		}

		for( int i = 0; i < 9; i++ ) {
			ImageView iv = (ImageView) rlRoot.findViewWithTag( i );
			iv.setEnabled( true );
			iv.setImageResource( R.drawable.blank );
		}
//		setTurnButtonColour(turn);
	}

}