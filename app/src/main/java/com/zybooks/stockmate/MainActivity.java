package com.zybooks.stockmate;

import android.content.Context;
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

//    private ArrayList<Item> items;
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private Button mRegisterButton;
    private InventoryDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the database
        mDatabase = InventoryDatabase.getInstance(getApplicationContext());

        mUsernameEditText = findViewById(R.id.email_edit_text);
        mPasswordEditText = findViewById(R.id.password_edit_text);
        mLoginButton = findViewById(R.id.login_button);
        mRegisterButton = findViewById(R.id.register_button);

        // Initialize the database
//        mDatabase = InventoryDatabase.getInstance(getApplicationContext());

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
        mLoginButton.setOnClickListener(new LoginClickListener(this));

        // Set up click listener for the register button
        mRegisterButton.setOnClickListener(new RegisterClickListener(this));

        // Get an instance of the InventoryDatabase
        InventoryDatabase inventoryDb = InventoryDatabase.getInstance(this);


        // Log the list of users
        inventoryDb.printListOfUsers();

//        InventoryDatabase inventoryDb = InventoryDatabase.getInstance(this);
        inventoryDb.printListOfPasswords();

    }

    private class LoginClickListener implements View.OnClickListener {

        private final Context mContext;

        public LoginClickListener(Context context) {
            mContext = context;
        }

        @Override
        public void onClick(View v) {

            String username = mDatabase.sanitizeInput(mUsernameEditText.getText().toString());
            String password = mDatabase.sanitizeInput(mPasswordEditText.getText().toString());

            // Check if either of the fields are empty
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(mContext, "Please enter a username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Sanitize and hash the username and password
//            byte[] hashedUsername = mDatabase.sanitizeAndHash(username);
//            byte[] hashedPassword = mDatabase.sanitizeAndHash(password);

            // Convert the hashed bytes to hex strings
//            String hashedUsernameStr = mDatabase.bytesToHexString(hashedUsername);
//            String hashedPasswordStr = mDatabase.bytesToHexString(hashedPassword);

            // Validate user credentials with sanitized and hashed inputs
            boolean loginSuccessful = mDatabase.validateUserCredentials(username, password);

            if (loginSuccessful) {
                Toast.makeText(mContext, "Login successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, InventoryListActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(mContext, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class RegisterClickListener implements View.OnClickListener {

        private final Context mContext;

        public RegisterClickListener(Context context) {
            mContext = context;
        }

        @Override
        public void onClick(View v) {

            String username = mUsernameEditText.getText().toString();
            String password = mPasswordEditText.getText().toString();

            // Check if either of the fields are empty
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(mContext, "Please enter a username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mDatabase.addUser(mDatabase.sanitizeInput(username), password)) {
                Toast.makeText(mContext, "Registration successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, InventoryListActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(mContext, "Username already exists", Toast.LENGTH_SHORT).show();
            }
        }
    }


}