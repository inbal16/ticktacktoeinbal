package inbal.dolev.ticktacktoeinbal;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LeaderboardActivity extends AppCompatActivity {

    private ListView playersListView;
    private ArrayList<String> playersDisplayList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        playersListView = findViewById(R.id.playersListView);

        // יצירת האדפטר
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playersDisplayList);
        playersListView.setAdapter(adapter);

        // טעינת נתונים ממיוני Firebase
        loadSortedPlayers();
    }

    private void loadSortedPlayers() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Players");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList<Player> tempPlayers = new ArrayList<>();
                playersDisplayList.clear();

                // איסוף כל השחקנים
                for (DataSnapshot s : snapshot.getChildren()) {
                    Player p = s.getValue(Player.class);
                    if (p != null) {
                        tempPlayers.add(p);
                    }
                }

                // מיון בסדר יורד לפי score
                Collections.sort(tempPlayers, (p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));

                // יצירת טקסט תצוגה לכל שחקן
                for (Player p : tempPlayers) {
                    playersDisplayList.add(p.getName() + " - ניצחונות: " + p.getScore());
                }

                adapter.notifyDataSetChanged(); // רענון ListView
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // אפשר להוסיף Toast או הודעת שגיאה כאן
            }
        });
    }
}
