package com.example.onlinetictactoe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class PlayerName extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_name);

        final EditText playerNameEt = findViewById(R.id.playerNameEt);
        final AppCompatButton startGameBtn = findViewById(R.id.startGameBtn);


        startGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Getting PlayerName from EditText to a String variable
                final String getPlayerName = playerNameEt.getText().toString();

                // Checking whether player has entered own name
                if(getPlayerName.isEmpty()){
                    Toast.makeText(PlayerName.this, "Please enter player name", Toast.LENGTH_SHORT).show();
                }else{

                    // Creating intent to open MainActivity
                    Intent intent = new Intent(PlayerName.this, MainActivity.class);

                    // Adding player name along with intent
                    intent.putExtra("playerName", getPlayerName);

                    // Opening MainActivity
                    startActivity(intent);

                    // Destroy this(PlayerName) activity
                    finish();
                }
            }
        });

    }
}