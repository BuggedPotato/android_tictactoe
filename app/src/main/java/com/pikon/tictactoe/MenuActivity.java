package com.pikon.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button btnStart = (Button) findViewById( R.id.btnStart );
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonStartClick( view, false );
            }
        });

        Button btnOnline = (Button) findViewById( R.id.btnOnline );
        btnOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonStartClick( view, true );
            }
        });
    }

    public void onButtonStartClick( View view, boolean online ) {
        Intent intent = new Intent( getApplicationContext(), MainActivity.class );
        intent.putExtra( "online", online );
        startActivity( intent );
    }
}