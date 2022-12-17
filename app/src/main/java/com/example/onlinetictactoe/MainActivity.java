package com.example.onlinetictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LinearLayout player1Layout, player2Layout;
    private ImageView image1, image2, image3, image4, image5, image6, image7, image8, image9;
    private TextView player1TV, player2TV;

    // Winning Combinations
    private final List<int[]> combinationsList = new ArrayList<>();

    // Player unique id
    private String playerUniqueId = "0";

    // Getting firebase database reference from URL
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://tictactoe-17bd6-default-rtdb.firebaseio.com/");

    // True when opponent will be found to play the game
    private boolean opponentFound = false;

    // Unique Id of opponent
    private String opponentUniqueId = "0";

    // Values must be matching or waiting. When a user create a new connection/room and that person is waiting for other to join then the value will be waiting.
    private String status = "matching";

    // Player Turn
    private String playerTurn = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        player1Layout = findViewById(R.id.player1Layout);
        player2Layout = findViewById(R.id.player2Layout);

        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        image4 = findViewById(R.id.image4);
        image5 = findViewById(R.id.image5);
        image6 = findViewById(R.id.image6);
        image7 = findViewById(R.id.image7);
        image8 = findViewById(R.id.image8);
        image9 = findViewById(R.id.image9);

        player1TV = findViewById(R.id.player1TV);
        player2TV = findViewById(R.id.player2TV);

        // Getting PlayerName from PlayerName.class file
        final String getPlayerName = getIntent().getStringExtra("playerName");

        // Generating winning combinations
        combinationsList.add(new int[]{0,1,2});
        combinationsList.add(new int[]{3,4,5});
        combinationsList.add(new int[]{6,7,8});
        combinationsList.add(new int[]{0,3,6});
        combinationsList.add(new int[]{1,4,7});
        combinationsList.add(new int[]{2,5,8});
        combinationsList.add(new int[]{2,4,6});
        combinationsList.add(new int[]{0,4,8});


        //Showing progress dialog while waiting for opponent
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Waiting for Opponent");
        progressDialog.show();

        // Generate player unique id. Player will be identified by this id.
        playerUniqueId = String.valueOf(System.currentTimeMillis());

        // Setting player name to the TextView
        player1TV.setText(getPlayerName);

        databaseReference.child("connections").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Check if Opponent Found Or Not. If not then look for the opponent
                if(opponentFound) {

                    // Checking if there are others in the firebase realtime database
                    if(snapshot.hasChildren()) {

                        // Checking all connections if other users are also waiting for a user to play the match
                        for(DataSnapshot connections : snapshot.getChildren()){

                            // Getting connection unique id
                             long conId = Long.parseLong(connections.getKey());

                             // 2 players are required to play the game.
                             // If getPlayersCount is 1 it means other player is waiting for a opponent to play the game.
                             // else if getPlayersCount is 2 it means this connection has completed with 2 players.
                             int getPlayersCount = (int)connections.getChildrenCount();

                             // After created a new connection waiting for other to join
                             if(status.equals("waiting")){

                                 // If getPlayersCount is 2 means other player joined the match
                                 if(getPlayersCount == 2){

                                     playerTurn = playerUniqueId;

                                     applyPlayerTurn(playerTurn);
                                 }
                             }

                        }
                    }

                    // If there is no connection available in the firebase then create a new connection.
                    // It is like creating a room and waiting for other players to join the room.
                    else{

                        // Generating unique id for the connection
                        String connectionUniqueId = String.valueOf(System.currentTimeMillis());

                        // Adding first player to the connection and waiting for the other to complete-confirm the connection and play the game
                        snapshot.child(connectionUniqueId).child(playerUniqueId).child("player_name").getRef().setValue(getPlayerName);

                        status = "waiting";
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void applyPlayerTurn(String playerUniqueId2){
        if(playerUniqueId2.equals(playerUniqueId)){
            player1Layout.setBackgroundResource(R.drawable.round_back_vio_stroke);
            player2Layout.setBackgroundResource(R.drawable.round_back_vio_20);
        }else{
            player2Layout.setBackgroundResource(R.drawable.round_back_vio_stroke);
            player1Layout.setBackgroundResource(R.drawable.round_back_vio_20);
        }
    }
}