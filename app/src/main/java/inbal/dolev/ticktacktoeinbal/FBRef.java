package inbal.dolev.ticktacktoeinbal;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FBRef {

    // הפניה לשירות האימות - התחברות, הרשמה, התנתקות
    public static FirebaseAuth refAuth = FirebaseAuth.getInstance();

    // בסיס הנתונים של Firebase (Realtime Database)
    public static FirebaseDatabase FBDB = FirebaseDatabase.getInstance();

    // הפניה לצומת הראשי בו נשמור את פרטי כל השחקנים
    public static DatabaseReference refPlayers = FBDB.getReference("Players");

    public static DatabaseReference refGames = FBDB.getReference("Games");
    // אחסון בענן - Cloud Storage
    public static FirebaseStorage storage = FirebaseStorage.getInstance();
    public static StorageReference refStorageRoot = storage.getReference();
    public static StorageReference refProfileImages = refStorageRoot.child("profile_images");


    public static DatabaseReference getCurrentPlayerRef() {
        String uid = refAuth.getCurrentUser().getUid();
        return refPlayers.child(uid);
    }
}
