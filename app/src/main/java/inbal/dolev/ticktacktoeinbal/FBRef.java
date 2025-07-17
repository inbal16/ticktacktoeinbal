package inbal.dolev.ticktacktoeinbal;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FBRef {

    // הפניה לשירות האימות - התחברות, הרשמה, התנתקות
    public static FirebaseAuth refAuth = FirebaseAuth.getInstance();

    // בסיס הנתונים של Firebase (Realtime Database)
    public static FirebaseDatabase FBDB = FirebaseDatabase.getInstance();

    // הפניה לצומת הראשי בו נשמור את פרטי כל השחקנים
    public static DatabaseReference refPlayers = FBDB.getReference("Players");

    public static DatabaseReference refGames = FBDB.getReference("Games");



    public static DatabaseReference getCurrentPlayerRef() {
        String uid = refAuth.getCurrentUser().getUid();
        return refPlayers.child(uid);
    }
}
