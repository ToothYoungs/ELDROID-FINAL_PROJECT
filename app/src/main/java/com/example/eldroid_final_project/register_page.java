package com.example.eldroid_final_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class register_page extends AppCompatActivity {

    EditText et_firstName, et_lastName, et_contactNumber, et_username, et_password_signup;
    TextView tv_signIn;
    Button btn_signUp;
    FirebaseAuth fAuth;
    FirebaseUser user;
    DatabaseReference userDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userDatabase = FirebaseDatabase.getInstance().getReference("Users");
        tv_signIn = findViewById(R.id.tv_signIn);
        et_firstName = findViewById(R.id.et_firstName);
        et_lastName = findViewById(R.id.et_lastName);
        et_contactNumber = findViewById(R.id.et_contactNumber);
        et_username = findViewById(R.id.et_username);
        et_password_signup = findViewById(R.id.et_password_signup);
        btn_signUp = findViewById(R.id.btn_signUp);
        fAuth = FirebaseAuth.getInstance();

        clicks();

    }

    private void clicks() {

        tv_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(register_page.this, MainActivity.class);
                startActivity(intent);
            }
        });

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String firstName = et_firstName.getText().toString();
                String lastName = et_lastName.getText().toString();
                String username = et_username.getText().toString();
                String password = et_password_signup.getText().toString();
                String contactNum = "0" + et_contactNumber.getText().toString();
                String imageName = "";
                String imageUrl = "";
                String ratings = "0";


                if (TextUtils.isEmpty(firstName))
                {
                    et_firstName.setError("This field is required");
                }
                else if (TextUtils.isEmpty(lastName))
                {
                    et_lastName.setError("This field is required");
                }
                else if (TextUtils.isEmpty(contactNum))
                {
                    et_contactNumber.setError("This field is required");
                }
                else if (TextUtils.isEmpty(username) )
                {
                    et_username.setError("This field is required");
                }
                else if ( !Patterns.EMAIL_ADDRESS.matcher(username).matches())
                {
                    et_username.setError("Incorrect Email Format");
                }
                else if (contactNum.length() != 12)
                {
                    et_contactNumber.setError("Contact number must be 11 digit");
                }
                else if (TextUtils.isEmpty(password))
                {
                    Toast.makeText(register_page.this, "Password is required", Toast.LENGTH_SHORT).show();
                }
                else if (!isValidPassword(password))
                {
                    Toast.makeText(register_page.this, "Please choose a stronger password. Try a mix of letters, numbers, and symbols.", Toast.LENGTH_LONG).show();
                }
                else
                {
                    final ProgressDialog progressDialog = new ProgressDialog(register_page.this);
                    progressDialog.setTitle("Creating account");
                    progressDialog.show();

                    fAuth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            fAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        //String uid = fAuth.getCurrentUser().getUid().toString();

                                        user = fAuth.getCurrentUser();
                                        String uid  = user.getUid();


                                        Tech tech = new Tech(uid, firstName, lastName, contactNum, username, password, imageName, imageUrl, ratings);

                                        userDatabase.child(user.getUid())
                                                .setValue(tech).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    progressDialog.dismiss();
                                                    fAuth.getInstance().signOut();
                                                    Intent intent = new Intent(register_page.this, MainActivity.class);
                                                    startActivity(intent);
                                                    Toast.makeText(register_page.this, "User Created", Toast.LENGTH_LONG).show();

                                                } else {
                                                    Toast.makeText(register_page.this, "Creation Failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });


                                    } else
                                    {
                                        Toast.makeText(register_page.this, "Creation Failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                        }
                    });
                }
            }
        });

    }

    private static boolean isValidPassword(String password) {

        String regex = "^(?=.*[0-9])"
                + "(?=.*[a-z])(?=.*[A-Z])"
                + "(?=.*[@#$%^&+=?!#$%&()*+,./])"
                + "(?=\\S+$).{8,15}$";


        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(password);

        return m.matches();
    }
}