package inbal.dolev.ticktacktoeinbal;

import static inbal.dolev.ticktacktoeinbal.FBRef.refAuth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;

public class LoginActivity extends AppCompatActivity {
    private static final int IMAGE_PICK_CODE = 1000;
    private Uri selectedImageUri;
    private ImageView profileImageView;

    EditText  eTName,   eTEmail, eTPass;
    View btnRegister;
    TextView tVMsg;
   // FirebaseAuth mAuth;


      @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        eTName = findViewById(R.id.eTName);
        eTEmail = findViewById(R.id.eTEmail);
        eTPass = findViewById(R.id.eTPass);
        btnRegister = findViewById(R.id.btnRegister);
        tVMsg = findViewById(R.id.tVMsg);


        profileImageView = findViewById(R.id.profilePreview);
        Button btnSelectImage = findViewById(R.id.btnSelectImage);

          btnSelectImage.setOnClickListener(v -> {
              Intent intent = new Intent(Intent.ACTION_PICK);
              intent.setType("image/*");
              startActivityForResult(intent, IMAGE_PICK_CODE);
          });

          btnRegister.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  registerUser();
              }
          });
      }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            profileImageView.setImageURI(selectedImageUri);
        }
    }

    private void registerUser() {
        String name = eTName.getText().toString().trim();
        String email = eTEmail.getText().toString().trim();
        String password = eTPass.getText().toString().trim();

        if ( name.isEmpty() ||email.isEmpty() || password.isEmpty()) {
            tVMsg.setText("יש למלא את כל השדות");
            return;
        }

        if (!email.contains("@")) {
            tVMsg.setText("האימייל לא חוקי");
            return;
        }

        if (password.length() < 6) {
            tVMsg.setText("הסיסמה חייבת להכיל לפחות 6 תווים");
            return;
        }

        // קריאה ל-Firebase
        refAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = FBRef.refAuth.getCurrentUser();
                            String uid = user.getUid();
                            String name = eTName.getText().toString().trim();
                            String email = eTEmail.getText().toString().trim();

                            Player newPlayer = new Player(uid, name, email, 0);
                            FBRef.refPlayers.child(uid).setValue(newPlayer);

// בדוק אם תמונה נבחרה
                            if (selectedImageUri != null) {
                                StorageReference imageRef = FBRef.refProfileImages.child(uid + ".jpg");
                                imageRef.putFile(selectedImageUri)
                                        .addOnSuccessListener(taskSnapshot -> {
                                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                                String imageUrl = uri.toString();
                                                FBRef.refPlayers.child(uid).child("profileImageUrl").setValue(imageUrl);
                                            });
                                        });
                            }


                            // המשך הניווט ל-MainActivity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }

                    else {
                            tVMsg.setText("שגיאה: " + task.getException().getMessage());
                        }
                    }
                });
    }
}