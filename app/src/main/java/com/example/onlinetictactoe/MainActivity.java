package com.example.onlinetictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
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
    private final List<String> doneBoxes = new ArrayList<>();//done boxes positive by users so users won't select the box again

    // Player unique id
    private String playerUniqueId = "0";

    // Getting firebase database reference from URL
//    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://tictactoe-17bd6-default-rtdb.firebaseio.com/");
//    DatabaseReference databaseReference = database.getReference();

    // True when opponent will be found to play the game
    private boolean opponentFound = false;

    // Unique Id of opponent
    private String opponentUniqueId = "0";

    // Values must be matching or waiting. When a user create a new connection/room and that person is waiting for other to join then the value will be waiting.
    private String status = "matching";

    // Player Turn
    private String playerTurn = "";

    //connection id in which player has join to play the game
    private String connectionId ="";

    //Generating ValueEventListeners for firebase Database
    //turnsEvenListener listen for player turns and wonEventListener listen if the player the match
    ValueEventListener turnsEventListener, wonEventListener;

    //selected boxes by players. empty fields will be replaced by player id
    private final String[]boxesSelectedBy = {"","","","","","","","",""};


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
                if(!opponentFound) {

                    // Checking if there are others in the firebase realtime database
                    if(snapshot.hasChildren()) {

                        // Checking all connections if other users are also waiting for a user to play the match
                        for(DataSnapshot connections : snapshot.getChildren()){

                            // Getting connection unique id
                             String conId = connections.getKey();

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

                                     //true when player found in connections
                                     boolean playerFound = false;

                                     //getting player in connection
                                     for(DataSnapshot players : connections.getChildren()){
                                         String getPlayerUniqueId = players.getKey();

                                      //Check if player id match with user who created connection(this user) .if match then geet opponent detail
                                      if(getPlayerUniqueId.equals(playerUniqueId)){
                                          playerFound = true;

                                      }
                                      else if (playerFound){
                                          String getOpponentPlayerName = players.child("player_name").getValue(String.class);
                                          opponentUniqueId = players.getKey();

                                          //set opponent playername to the textview
                                          player2TV.setText(getOpponentPlayerName);

                                          //assigning connection id
                                          connectionId = conId;
                                          opponentFound = true;

                                          //adding turn listener and won listener to the database
                                          databaseReference.child("turns").child(connectionId).addValueEventListener(turnsEventListener);
                                          databaseReference.child("won").child(connectionId).addValueEventListener(wonEventListener);

                                          //hide progress dialog if showing
                                          if (progressDialog.isShowing()){
                                              progressDialog.dismiss();
                                          }

                                          //once the connection has mase remove connectionListener from Database Reference
                                          databaseReference.child("connections").removeEventListener(this);
                                      }

                                     }

                                 }
                             }
                             //in case user has not create the connection/room because of other rooms are available to join
                             else{
                                 //checking if the connection has 1 player ans need 1 more player to play the match then join the this connection
                                 if(getPlayersCount == 1){

                                     //add player to the coonection
                                     connections.child(playerUniqueId).child("player_name").getRef().setValue(getPlayerName);

                                     //getting both players
                                     for(DataSnapshot players : connections.getChildren()){

                                         String getOpponentName = players.child("player_name").getValue(String.class);
                                         opponentUniqueId = players.getKey();

                                         //first turn will be of who created the coonection/ room
                                         playerTurn = opponentUniqueId;
                                         applyPlayerTurn(playerTurn);

                                         //setting playername to the textview
                                         player2TV.setText(getOpponentName);

                                         //assigning connecting
                                         connectionId = conId;
                                         opponentFound = true;

                                         //adding turn listener and won listener to the database
                                         databaseReference.child("turns").child(connectionId).addValueEventListener(turnsEventListener);
                                         databaseReference.child("won").child(connectionId).addValueEventListener(wonEventListener);

                                         //hide progress dialog if showing
                                         if (progressDialog.isShowing()){
                                             progressDialog.dismiss();
                                         }

                                         //once the connection has mase remove connectionListener from Database Reference
                                         databaseReference.child("connections").removeEventListener(this);

                                         break;
                                     }
                                 }
                             }

                        }
                        //check if opponent is not found and user is not waiting for the opponent anymore then create a new connection
                        if (!opponentFound && !status.equals("waiting")){

                            // Generating unique id for the connection
                            String connectionUniqueId = String.valueOf(System.currentTimeMillis());

                            // Adding first player to the connection and waiting for the other to complete-confirm the connection and play the game
                            snapshot.child(connectionUniqueId).child(playerUniqueId).child("player_name").getRef().setValue(getPlayerName);

                            status = "waiting";

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
        turnsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //getting all turns of the connection
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                    if(dataSnapshot.getChildrenCount() == 2){
                        //getting box position selected by the user
                        final int getBoxPosition = Integer.parseInt(dataSnapshot.child("box_position").getValue(String.class));

                        //getting player id who selected the box
                        final String getPlayerId = dataSnapshot.child("player_id").getValue(String.class);


                        //checking if user has not selected the box before
                        if(!doneBoxes.contains(String.valueOf(getBoxPosition))){

                            //select the box
                            doneBoxes.add(String.valueOf(getBoxPosition));

                            if(getBoxPosition == 1){
                                selectBox(image1, getBoxPosition, getPlayerId);
                            }
                            else if(getBoxPosition == 2){
                                selectBox(image2, getBoxPosition, getPlayerId);
                            }
                            else if(getBoxPosition == 3){
                                selectBox(image3, getBoxPosition, getPlayerId);

                            }
                            else if(getBoxPosition == 4){
                                selectBox(image4, getBoxPosition, getPlayerId);

                            }
                            else if(getBoxPosition == 5){
                                selectBox(image5, getBoxPosition, getPlayerId);

                            }
                            else if(getBoxPosition == 6){
                                selectBox(image6, getBoxPosition, getPlayerId);

                            }
                            else if(getBoxPosition == 7){
                                selectBox(image7, getBoxPosition, getPlayerId);

                            }
                            else if(getBoxPosition == 8){
                                selectBox(image8, getBoxPosition, getPlayerId);

                            }
                            else if(getBoxPosition == 9){
                                selectBox(image9, getBoxPosition, getPlayerId);

                            }

                        }

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        wonEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check if a user has won the match
                if(snapshot.hasChild("player_id")){
                     String getWinPlayerId = snapshot.child("player_id").getValue(String.class);

                     final WinDialog winDialog;

                     if(getWinPlayerId.equals(playerUniqueId)){

                         //show win dialog
                         winDialog = new WinDialog(MainActivity.this,"You won the game!");// s phai la meessage
                     }
                     else{
                         //show win dialog
                         winDialog = new WinDialog(MainActivity.this,"Oh,Opponent won!");// s phai la meessage

                     }
                     winDialog.setCancelable((false));
                     winDialog.show();

                     //remove listeners from database
                    databaseReference.child("turns").child(connectionId).removeEventListener(turnsEventListener);
                    databaseReference.child("won").child(connectionId).removeEventListener(wonEventListener);
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if the box is not selected before and current user's player turn
                if(!doneBoxes.contains("1") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)v).setImageResource(R.drawable.cross_icon);

                    //send selected box position and player unique id to Firebase Database
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1 )).child("box_position").setValue("1");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1 )).child("player_id").setValue(playerUniqueId);

                    //chNGE PLAYER TURN
                    playerTurn = opponentUniqueId;
                }
            }
        });
        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if the box is not selected before and current user's player turn
                if(!doneBoxes.contains("2") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)v).setImageResource(R.drawable.cross_icon);

                    //send selected box position and player unique id to Firebase Database
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1 )).child("box_position").setValue("2");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1 )).child("player_id").setValue(playerUniqueId);

                    //chNGE PLAYER TURN
                    playerTurn = opponentUniqueId;
                }
            }
        });
        image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if the box is not selected before and current user's player turn
                if(!doneBoxes.contains("3") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)v).setImageResource(R.drawable.cross_icon);

                    //send selected box position and player unique id to Firebase Database
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1 )).child("box_position").setValue("3");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1 )).child("player_id").setValue(playerUniqueId);

                    //chNGE PLAYER TURN
                    playerTurn = opponentUniqueId;
                }
            }
        });
        image4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if the box is not selected before and current user's player turn
                if(!doneBoxes.contains("4") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)v).setImageResource(R.drawable.cross_icon);

                    //send selected box position and player unique id to Firebase Database
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1 )).child("box_position").setValue("4");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1 )).child("player_id").setValue(playerUniqueId);

                    //chNGE PLAYER TURN
                    playerTurn = opponentUniqueId;
                }
            }
        });
        image5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if the box is not selected before and current user's player turn
                if(!doneBoxes.contains("5") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)v).setImageResource(R.drawable.cross_icon);

                    //send selected box position and player unique id to Firebase Database
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1 )).child("box_position").setValue("5");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1 )).child("player_id").setValue(playerUniqueId);

                    //chNGE PLAYER TURN
                    playerTurn = opponentUniqueId;
                }
            }
        });
        image6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if the box is not selected before and current user's player turn
                if(!doneBoxes.contains("6") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)v).setImageResource(R.drawable.cross_icon);

                    //send selected box position and player unique id to Firebase Database
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1 )).child("box_position").setValue("6");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1 )).child("player_id").setValue(playerUniqueId);

                    //chNGE PLAYER TURN
                    playerTurn = opponentUniqueId;
                }
            }
        });
        image7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if the box is not selected before and current user's player turn
                if(!doneBoxes.contains("7") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)v).setImageResource(R.drawable.cross_icon);

                    //send selected box position and player unique id to Firebase Database
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1 )).child("box_position").setValue("7");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1 )).child("player_id").setValue(playerUniqueId);

                    //chNGE PLAYER TURN
                    playerTurn = opponentUniqueId;
                }
            }
        });
        image8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if the box is not selected before and current user's player turn
                if(!doneBoxes.contains("8") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)v).setImageResource(R.drawable.cross_icon);

                    //send selected box position and player unique id to Firebase Database
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1 )).child("box_position").setValue("8");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1 )).child("player_id").setValue(playerUniqueId);

                    //chNGE PLAYER TURN
                    playerTurn = opponentUniqueId;
                }
            }
        });
        image9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if the box is not selected before and current user's player turn
                if(!doneBoxes.contains("9") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)v).setImageResource(R.drawable.cross_icon);

                    //send selected box position and player unique id to Firebase Database
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1 )).child("box_position").setValue("9");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1 )).child("player_id").setValue(playerUniqueId);

                    //chNGE PLAYER TURN
                    playerTurn = opponentUniqueId;
                }
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
    private void selectBox(ImageView imageView, int selectedBoxPosition, String selectedByPlayer){

        boxesSelectedBy[selectedBoxPosition - 1] = selectedByPlayer;

        if(selectedByPlayer.equals(playerUniqueId)){
            imageView.setImageResource(R.drawable.cross_icon);
            playerTurn = opponentUniqueId;
        }
        else{
            imageView.setImageResource(R.drawable.zero_icon);
            playerTurn = playerUniqueId;
        }
        applyPlayerTurn(playerTurn);

        //checking whether player has won the match
        if(checkPlayerWin(selectedByPlayer)){

            //sending won player unique id to firebase database wo oppenent can be notified
            databaseReference.child("won").child(connectionId).child("player_id").setValue(selectedByPlayer);
        }

        //over the game if there is no box left to be selected
        if (doneBoxes.size() == 9){
            final WinDialog winDialog = new WinDialog(MainActivity.this,"It is a Draw!");// s no phai la message
             winDialog.setCancelable(false);
             winDialog.show();
        }

    }
    private boolean checkPlayerWin(String playerId){
        boolean isPlayerWon = false;

        //compare player turns with every wining combination
        for (int i = 0; i<combinationsList.size(); i++){
            final int[]combination = combinationsList.get(i);

            //checking last three turn of user
            if(boxesSelectedBy[combination[0]].equals(playerId) &&
                    boxesSelectedBy[combination[1]].equals(playerId) &&
                    boxesSelectedBy[combination[2]].equals(playerId)){
                isPlayerWon = true;
            }
        }
        return isPlayerWon;
    }
}