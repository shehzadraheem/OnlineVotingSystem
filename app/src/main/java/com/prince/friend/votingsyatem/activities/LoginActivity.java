package com.prince.friend.votingsyatem.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.prince.friend.votingsyatem.R;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText userEmail,userPassword;
    private Button loginBtn;
    private TextView forgetPassword;
    private FirebaseAuth mAuth;

    public static final String PREFERENCES = "prefKey";
    public static final String Name = "nameKey";
    public static final String Email = "emailKey";
    public static final String Password = "passwordKey";
    public static final String NationalId = "nationalIdKey";
    public static final String Image = "imageKey";
    public static final String UploadData = "uploaddata";
    SharedPreferences sharedPreferences;

    StorageReference reference ;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getApplicationContext().getSharedPreferences(PREFERENCES,MODE_PRIVATE);
        reference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        findViewById(R.id.dont_have_acc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,SignUpActivity.class));
            }
        });


        userEmail = findViewById(R.id.user_email);
        userPassword = findViewById(R.id.user_password);
        loginBtn = findViewById(R.id.login_btn);
        forgetPassword = findViewById(R.id.forget_password);
        mAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = userEmail.getText().toString().trim();
                String password = userPassword.getText().toString().trim();


                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){

                            verifyEmail();

                        }else{
                            Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        forgetPassword.findViewById(R.id.forget_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,ForgetPasswordActivity.class));
            }
        });

    }

    private void verifyEmail() {

        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        if(user.isEmailVerified()){

            boolean bol = sharedPreferences.getBoolean(UploadData,false);
            if(bol){
                // if email is verified and data is already uploaded then we do not need to upload it again
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            }else {

                String name = sharedPreferences.getString(Name, null);
                String password = sharedPreferences.getString(Password, null);
                String email = sharedPreferences.getString(Email, null);
                String nationalId = sharedPreferences.getString(NationalId, null);
                String image = sharedPreferences.getString(Image, null);


                // first we sent email for verification
                // store data in shared preference if user verify email
                // and login then we upload data to Firestore and save image in Firebase Storage

                if (name != null && password != null && email != null && nationalId != null && image != null) {

                    String uid = mAuth.getUid();

                    StorageReference imagePath = reference.child("image_profile").child(uid + ".jpg");
                    imagePath.putFile(Uri.parse(image)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if (task.isSuccessful()) {

                                imagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        Map<String, String> map = new HashMap<>();
                                        map.put("name", name);
                                        map.put("email", email);
                                        map.put("password", password);
                                        map.put("nationalId", nationalId);
                                        map.put("image", uri.toString());
                                        map.put("uid", uid);

                                        firebaseFirestore.collection("Users")
                                                .document(uid)
                                                .set(map)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {


                                                            sharedPreferences = getApplicationContext().getSharedPreferences(PREFERENCES, MODE_PRIVATE);
                                                            SharedPreferences.Editor pref = sharedPreferences.edit();
                                                            pref.putBoolean(UploadData, true);
                                                            pref.commit();

                                                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                                            finish();
                                                        } else {
                                                            Toast.makeText(LoginActivity.this, "Data not store", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                    }
                                });

                            } else {
                                Toast.makeText(LoginActivity.this, "" + task.getException(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                } else {
                    Toast.makeText(LoginActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }

            }

        }else{
            mAuth.signOut();
            Toast.makeText(LoginActivity.this, "Please Verify your Email", Toast.LENGTH_SHORT).show();
        }
    }


}