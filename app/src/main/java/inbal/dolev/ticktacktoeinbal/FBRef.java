package inbal.dolev.ticktacktoeinbal;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FBRef {
    public static FirebaseAuth refAuth = FirebaseAuth.getInstance();

    public static FirebaseDatabase FBDB = FirebaseDatabase.getInstance();//עצם בבסיס הנתונים שלנו בFirebase
    public static DatabaseReference refPlayers=FBDB.getReference("Players");//עצם המצביע לשורש שהגדרנו ב Firebase




}
