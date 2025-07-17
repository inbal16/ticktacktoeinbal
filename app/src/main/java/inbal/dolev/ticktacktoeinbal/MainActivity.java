package inbal.dolev.ticktacktoeinbal;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private Button[] buttons = new Button[9];
    private String[] board = new String[9];
    private boolean xTurn = true;
    private TextView playerTurnText;
    private TextToSpeech textToSpeech;

    private Player playerX;
    private Player playerO;
    private String currentUserId;
    private boolean isPlayerXLoaded = false;
    private boolean isPlayerOLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentUserId = FBRef.refAuth.getCurrentUser().getUid();

        FBRef.refPlayers.child(currentUserId).get().addOnSuccessListener(snapshot -> {
            playerX = snapshot.getValue(Player.class);
            isPlayerXLoaded = true;
            checkIfBothPlayersLoaded();
        });
        ImageView profileImageView = findViewById(R.id.profileImageView);
        String uid = FBRef.refAuth.getCurrentUser().getUid();

        FBRef.refPlayers.child(uid).get().addOnSuccessListener(snapshot -> {
            Player player = snapshot.getValue(Player.class);
            if (player != null && player.getProfileImageUrl() != null) {
                // טוען את התמונה מהכתובת
                Picasso.get()
                        .load(player.getProfileImageUrl())
                        .placeholder(R.drawable.ic_person) // תצוגה זמנית לפני שהטען
                        .error(R.drawable.ic_person)// תצוגה במקרה של שגיאה
                        .into(profileImageView);
            }
        });

    // אתחול של המחשב כשחקן
        playerO = new Player("Computer");
        isPlayerOLoaded = true;
        checkIfBothPlayersLoaded();



        GridLayout gridLayout = findViewById(R.id.gridLayout);
        playerTurnText = findViewById(R.id.playerTurnText);
        Button resetButton = findViewById(R.id.resetButton);


        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.US);
            }
        });


        for (int i = 0; i < 9; i++) {
            final int index = i;
            buttons[i] = (Button) gridLayout.getChildAt(i);
            buttons[i].setOnClickListener(v -> handleMove(index));
        }

        resetButton.setOnClickListener(v -> resetGame());

        updatePlayerTurnText();

        Button btnLeaderboard = findViewById(R.id.btnLeaderboard);
        btnLeaderboard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LeaderboardActivity.class);
            startActivity(intent);
        });

    }
    private void checkIfBothPlayersLoaded() {
        if (isPlayerXLoaded && isPlayerOLoaded) {
            updatePlayerTurnText(); // עכשיו אפשר להציג את שם השחקן הראשון
        }
    }
    private boolean isBoardFull() {
        for (String cell : board) {
            if (cell == null) return false;
        }
        return true;
    }



    private void handleMove(int index) {
        if (board[index] != null || !xTurn) return;

        board[index] = "X";
        buttons[index].setText("X");
        buttons[index].setEnabled(false);

        String winnerSymbol = checkWinner();
        if (winnerSymbol != null) {
            String winnerName = getWinnerName(winnerSymbol);
            announceWinner(winnerName);
            showVictoryImage(winnerName);
            Log.d("MainActivity", "Winner: " + winnerName);
            disableButtons();
            if (winnerSymbol.equals("X")) {
                updatePlayerScore(currentUserId); // ⬅️ חדש: עדכון ניקוד
            }

            saveGameToFirebase(winnerName);
        } else if (isBoardFull()) {
            Toast.makeText(this, "It's a draw! No winner.", Toast.LENGTH_SHORT).show();
            saveGameToFirebase("Draw");
        } else {
            xTurn = false;
            updatePlayerTurnText();
            new android.os.Handler().postDelayed(() -> makeComputerMove(), 800);
        }
    }
    private void updatePlayerScore(String userId) {
        FBRef.refPlayers.child(userId).get().addOnSuccessListener(snapshot -> {
            Player player = snapshot.getValue(Player.class);
            if (player != null) {
                int currentScore = player.getScore();
                FBRef.refPlayers.child(userId).child("score").setValue(currentScore + 1);
            }
        });
    }

    private void showVictoryImage(String winnerName) {
        Toast.makeText(this, "🏆 " + winnerName + " is the Ultimate Champion!", Toast.LENGTH_LONG).show();
        textToSpeech.speak(winnerName + " is the Ultimate Champion!", TextToSpeech.QUEUE_FLUSH, null, null);

        ImageView victoryImage = findViewById(R.id.victoryImage);
        victoryImage.setImageResource(R.drawable.champion_image);
        victoryImage.setVisibility(View.VISIBLE);

        // ⏱ תמונה תיעלם אחרי 3 שניות
        new android.os.Handler().postDelayed(() -> victoryImage.setVisibility(View.GONE), 3000);
    }


    private void makeComputerMove() {
        if (isBoardFull()) return;

        for (int i = 0; i < 9; i++) {
            if (board[i] == null) {
                board[i] = "O";
                buttons[i].setText("O");
                buttons[i].setEnabled(false);

                String winnerSymbol = checkWinner();
                if (winnerSymbol != null) {
                    String winnerName = getWinnerName(winnerSymbol);
                    announceWinner(winnerName);
                    disableButtons();
                    saveGameToFirebase(winnerName);
                } else if (isBoardFull()) {
                    Toast.makeText(this, "It's a draw! No winner.", Toast.LENGTH_SHORT).show();
                    saveGameToFirebase("Draw");
                } else {
                    xTurn = true;
                    updatePlayerTurnText();
                }
                return;
            }
        }
    }
    private void announceWinner(String winnerName) {
        Toast.makeText(this, "The winner is: " + winnerName, Toast.LENGTH_LONG).show();
        textToSpeech.speak("The winner is " + winnerName, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private String checkWinner() {
        int[][] winPositions = {
                {0,1,2}, {3,4,5}, {6,7,8},
                {0,3,6}, {1,4,7}, {2,5,8},
                {0,4,8}, {2,4,6}
        };

        for (int[] pos : winPositions) {
            String a = board[pos[0]];
            String b = board[pos[1]];
            String c = board[pos[2]];

            if (a != null && a.equals(b) && a.equals(c)) {
                return a;
            }
        }
        return null;
    }


        private String getWinnerName(String symbol) {
            if (symbol.equals("X") && playerX != null && playerX.getName() != null) {
                return playerX.getName();
            } else if (symbol.equals("O") && playerO != null && playerO.getName() != null) {
                return playerO.getName();
            }
            return "Unknown";
        }



    private void updatePlayerTurnText() {
        String name = xTurn ? (playerX != null ? playerX.getName() : "Player") : "Computer";
        playerTurnText.setText("Player Turn: " + name);
    }
    private void disableButtons() {
        for (Button button : buttons) {
            button.setEnabled(false);
        }
    }

    private void resetGame() {
        for (int i = 0; i < 9; i++) {
            board[i] = null;
            buttons[i].setText("");
            buttons[i].setEnabled(true);
        }
        xTurn = true;
        updatePlayerTurnText();
    }
    private void saveGameToFirebase(String winner) {
        String gameId = FBRef.refGames.push().getKey(); // מזהה ייחודי לכל משחק
        if (gameId == null) return;

        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

        Game game = new Game(winner, timestamp);
        FBRef.refGames.child(gameId).setValue(game);
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
