package inbal.dolev.ticktacktoeinbal;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button[] buttons = new Button[9];
    private String[] board = new String[9];
    private boolean xTurn = true;
    private TextView playerTurnText;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    }

    private boolean isBoardFull() {
        for (String cell : board) {
            if (cell == null) return false;
        }
        return true;
    }

    private void handleMove(int index) {
        if (board[index] != null) return;

        board[index] = xTurn ? "X" : "O";
        buttons[index].setText(board[index]);

        if (checkWinner()) {
            Toast.makeText(this, "The winner is: " + board[index], Toast.LENGTH_LONG).show();

            textToSpeech.speak("The winner is " + board[index], TextToSpeech.QUEUE_FLUSH, null, null);
            disableButtons();
        } else if (isBoardFull()) {
            Toast.makeText(this, "It's a draw! No winner.", Toast.LENGTH_SHORT).show();
        } else {
            xTurn = !xTurn;
            updatePlayerTurnText();
        }
    }

    private void updatePlayerTurnText() {
        playerTurnText.setText("Player Turn: " + (xTurn ? "X" : "O"));
    }

    private boolean checkWinner() {
        int[][] winCombinations = {
                {0,1,2}, {3,4,5}, {6,7,8},
                {0,3,6}, {1,4,7}, {2,5,8},
                {0,4,8}, {2,4,6}
        };

        for (int[] combo : winCombinations) {
            String a = board[combo[0]], b = board[combo[1]], c = board[combo[2]];
            if (a != null && a.equals(b) && a.equals(c)) return true;
        }

        return false;
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

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
