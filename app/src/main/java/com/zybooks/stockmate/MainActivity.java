package com.zybooks.stockmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private Button mRegisterButton;
    private InventoryDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_registration);

        mUsernameEditText = findViewById(R.id.email_edit_text);
        mPasswordEditText = findViewById(R.id.password_edit_text);
        mLoginButton = findViewById(R.id.login_button);
        mRegisterButton = findViewById(R.id.register_button);

        // Initialize the database
        mDatabase = InventoryDatabase.getInstance(getApplicationContext());

        // Disable the buttons until the user enters text in both fields
        mLoginButton.setEnabled(false);
        mRegisterButton.setEnabled(false);

        // Add a TextWatcher to enable/disable the buttons based on text input
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean enableButtons = mUsernameEditText.getText().length() > 0 &&
                        mPasswordEditText.getText().length() > 0;
                mLoginButton.setEnabled(enableButtons);
                mRegisterButton.setEnabled(enableButtons);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        mUsernameEditText.addTextChangedListener(textWatcher);
        mPasswordEditText.addTextChangedListener(textWatcher);

        // Set up click listener for the login button
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText emailEditText = findViewById(R.id.email_edit_text);
                EditText passwordEditText = findViewById(R.id.password_edit_text);

                String username = mUsernameEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();


                if (mDatabase.validateUser(username, password)) {
                    Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up click listener for the register button
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText emailEditText = findViewById(R.id.email_edit_text);
                EditText passwordEditText = findViewById(R.id.password_edit_text);

                String username = mUsernameEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                if (mDatabase.addUser(username, password)) {
                    Toast.makeText(MainActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }
}
