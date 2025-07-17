package inbal.dolev.ticktacktoeinbal;

import static inbal.dolev.ticktacktoeinbal.FBRef.refAuth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;

public class LoginActivity extends AppCompatActivity {

    private static final int IMAGE_PICK_CODE = 1000;
    private Uri selectedImageUri;

    private EditText eTName, eTEmail, eTPass;
    private ImageView profileImageView;
    private TextView tVMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 🛡️ בקשת הרשאות לגישה לתמונות
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 123);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
            }
        }

        // 🎯 קישור ל־Views
        eTName = findViewById(R.id.eTName);
        eTEmail = findViewById(R.id.eTEmail);
        eTPass = findViewById(R.id.eTPass);
        profileImageView = findViewById(R.id.profileImageView);
        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        Button btnRegister = findViewById(R.id.btnRegister);
        tVMsg = findViewById(R.id.tVMsg);

        // 📸 פתיחת גלריה
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, IMAGE_PICK_CODE);
        });

        // 🔐 התחברות או רישום לפי מצב
        btnRegister.setOnClickListener(v -> loginOrRegisterUser());
    }

    // ✨ בחירת תמונה וחזרה
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            profileImageView.setImageURI(selectedImageUri);
        }
    }

    private void loginOrRegisterUser() {
        String name = eTName.getText().toString().trim();
        String email = eTEmail.getText().toString().trim();
        String password = eTPass.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            tVMsg.setText("יש למלא את כל השדות");
            return;
        }

        if (!email.contains("@")) {
            tVMsg.setText("האימייל לא חוקי");
            return;
        }

        if (password.length() < 6) {
            tVMsg.setText("הסיסמה חייבת לפחות 6 תווים");
            return;
        }

        // 🔓 נסיון התחברות
        refAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        moveToMain();
                    } else {
                        // 🆕 רישום חדש
                        refAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(regTask -> {
                                    if (regTask.isSuccessful()) {
                                        FirebaseUser user = refAuth.getCurrentUser();
                                        String uid = user.getUid();

                                        Player player = new Player(uid, name, email, 0);
                                        FBRef.refPlayers.child(uid).setValue(player);

                                        // ⬆️ העלאת תמונה
                                        if (selectedImageUri != null) {
                                            StorageReference imageRef = FBRef.refProfileImages.child(uid + ".jpg");
                                            imageRef.putFile(selectedImageUri)
                                                    .addOnSuccessListener(snapshot -> {
                                                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                                            String url = uri.toString();
                                                            FBRef.refPlayers.child(uid).child("profileImageUrl").setValue(url);
                                                        });
                                                    });
                                        }

                                        moveToMain();
                                    } else {
                                        tVMsg.setText("שגיאה: " + regTask.getException().getMessage());
                                    }
                                });
                    }
                });
    }

    private void moveToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
