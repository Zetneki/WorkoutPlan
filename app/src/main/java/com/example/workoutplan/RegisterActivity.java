package com.example.workoutplan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private static final String LOG_TAG = LoginActivity.class.getName();
    private static final int SECRET_KEY = 99;

    TextView userNameInput;
    TextView emailInput;
    TextView passwordInput;
    TextView passwordConfirmInput;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);
        //if (secret_key != SECRET_KEY) finish();

        userNameInput = findViewById(R.id.userNameEditText);
        emailInput = findViewById(R.id.emailEditText);
        passwordInput= findViewById(R.id.passwordEditText);
        passwordConfirmInput= findViewById(R.id.passwordConfirmEditText);

        mAuth = FirebaseAuth.getInstance();
    }

    public void registration(View view) {
        String userName = userNameInput.getText().toString();
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        String passwordConfirm = passwordConfirmInput.getText().toString();

        if (!password.equals(passwordConfirm)) {
            Log.e(LOG_TAG, "Passwords don't match");
            //TODO: ki kene iratni h nem egyeznek meg a jelszavak
            return;
        }

        Log.i(LOG_TAG, "Username: " + userName + ", email: " + email + ", password: " + password + ", password confirm: " + passwordConfirm);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "Successful registration");
                    startWorkingOut();
                } else {
                    Log.d(LOG_TAG, "Unsuccessful registration");
                    Toast.makeText(RegisterActivity.this, "User wasn't created successfully: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void startWorkingOut() {
        Intent intent = new Intent(this, MainActivity.class);
        //intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }

    public void navigateToLogin(View view) {
        finish();
    }
}