//Tutorial: Author Henry - ANDROID LOGIN AND REGISTRATION WITH PHP AND MYSQL
//Link: https://inducesmile.com/android-snippets/android-login-and-registration-with-php-and-mysql/

package ie.ucc.bis.a114355681.learnerlog.LoginActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import ie.ucc.bis.a114355681.learnerlog.InstructorActivities.RequestActivity;
import ie.ucc.bis.a114355681.learnerlog.R;
import ie.ucc.bis.a114355681.learnerlog.StudentActivities.ScheduleActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //define edit text variables
    EditText EmailEt, PasswordEt;

    TextView txtWrongPass;

    //Declare FirebaseAuth listener object
    private FirebaseAuth mAuth;

    DatabaseReference myRef, usersReference;

    //progress dialog to show a message that you are being logged in
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //set the layout for the activity.

        // Initiate edit text variables
        EmailEt = (EditText) findViewById(R.id.EmailEt);
        PasswordEt = (EditText) findViewById(R.id.PasswordEt);

        //Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        usersReference = FirebaseDatabase.getInstance().getReference().child("users");

        //initialize progress dialog
        progressDialog = new ProgressDialog(this);

        //register on click listeners
        findViewById(R.id.btnReg).setOnClickListener(this);
        findViewById(R.id.btnLogIn).setOnClickListener(this);

        txtWrongPass = (TextView) findViewById(R.id.txtWrongPass);
    }

    private void userLogin() {

        //create username and password variables
        String userEmail = EmailEt.getText().toString().trim();
        String userPass = PasswordEt.getText().toString().trim();

        //checks if email has been entered
        if (userEmail.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please enter your email address."
                    , Toast.LENGTH_SHORT).show();
            EmailEt.requestFocus();
            return;
        }

        //validates email address
        if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            EmailEt.setError("Please enter a valid email.");
            EmailEt.requestFocus();
            return;
        }

        //checks if password has been entered
        if (userPass.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please enter your password."
                    , Toast.LENGTH_SHORT).show();
            PasswordEt.requestFocus();
            return;
        }

        //minimum password for Firebase Auth is 6 characters, validates this
        if (userPass.length() < 6) {
            PasswordEt.setError("Password must be greater than 6 characters.");
            PasswordEt.requestFocus();
            return;
        }

        /*in each of the above errors, .requestFocus() will bring the cursor to the required
        text field. 'return;' ensures the app doesn't continue. */

        //set progress dialog message and call show() method to display it
        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        //call the login method
        mAuth.signInWithEmailAndPassword(userEmail, userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //hide progress dialog
                progressDialog.dismiss();
                //check login success
                if (task.isSuccessful()) {
                    //gets the current user logging in and finds their userID
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String UserID = currentUser.getUid();

                    //database reference to the userID in the users table
                    myRef = FirebaseDatabase.getInstance().getReference().child("users").child(UserID);

                    String DeviceToken = FirebaseInstanceId.getInstance().getToken();
                    usersReference.child(UserID).child("device_token").setValue(DeviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            myRef.addValueEventListener(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    //creates a string which pulls the type of user from the database
                                    String type = dataSnapshot.child("type").getValue().toString();
                                    //if the user logging in is a student display the student navigation screen
                                    if (type.equals("student")) {
                                        //Brings you to the login activity
                                        Intent intent = new Intent (MainActivity.this, ScheduleActivity.class);
                                        //clear all open activities on the stack and open the new one
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        //start the new activity
                                        startActivity(intent);
                                        finish();
                                        //if the user logging in is an instructor display the instructor navigation screen
                                    } else if (type.equals("instructor")) {
                                        //Brings you to the login activity
                                        Intent intent = new Intent (MainActivity.this, RequestActivity.class);
                                        //clear all open activities on the stack and open the new one
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        //start the new activity
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        //incorrect username/password alert message
                                        Toast.makeText(MainActivity.this, "Failed Login. Please Try Again", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    });

                } else {
                    txtWrongPass.setVisibility(TextView.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onClick(View view){
        //switch statement to decide which button was pressed and what to do in each case.
        switch (view.getId()){
            case R.id.btnReg: //Display the register activity
                startActivity(new Intent(this, RegisterActivity.class));
                break;
            case R.id.btnLogIn:
                userLogin(); //call the method to log a user in
                break;
        }
    }
}
