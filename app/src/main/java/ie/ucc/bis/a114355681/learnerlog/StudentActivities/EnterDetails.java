package ie.ucc.bis.a114355681.learnerlog.StudentActivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ie.ucc.bis.a114355681.learnerlog.LoginActivities.MainActivity;
import ie.ucc.bis.a114355681.learnerlog.HelperClasses.BottomNavigationViewHelper;
import ie.ucc.bis.a114355681.learnerlog.R;
import ie.ucc.bis.a114355681.learnerlog.datamodel.User;

public class EnterDetails extends AppCompatActivity {

    //To distinguish which activity class has logged the information in logcat
    private static final String TAG = "Enter Details";

    //declare buttons, edit texts and strings
    private Button btnSave;
    private EditText etName, etAddress, etPhone;
    private String userID;

    //String for user type to enter "student" into database
    private String type = "student";

    //declare firebase database objects
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_details);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //removes animation from bottom navigation bar
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView_Bar);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        //highlights the selected menu item
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);

        //sets the listener for the bottom nav bar
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    //displays the home activity
                    case R.id.ic_lessons:
                        startActivity(new Intent(EnterDetails.this, ScheduleActivity.class));
                        break;

                    //displays the booking activity
                    case R.id.ic_book:
                        startActivity(new Intent(EnterDetails.this, BookingActivity.class));
                        break;

                    //displays the payments activity
                    case R.id.ic_payments:
                        startActivity(new Intent(EnterDetails.this, LessonPaymentActivity.class));
                        break;

                    //current activity
                    case R.id.ic_settings:
                        break;
                }
                return true;
            }
        });

        //init buttons and edit texts
        btnSave = (Button) findViewById(R.id.btnSave);
        etName = (EditText) findViewById(R.id.etName);
        etAddress = (EditText) findViewById(R.id.etAddress);
        etPhone = (EditText) findViewById(R.id.etPhone);

        //declare the database reference object. This is what we use to access the database.
        //NOTE: Unless you are signed in, this will not be useable.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        //determine is user is logged in
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Log.d(TAG, "onDataChange: Added information to database: \n" +
                        dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Submit pressed.");
                String name = etName.getText().toString();
                String address = etAddress.getText().toString();
                String phoneNum = etPhone.getText().toString();

                Log.d(TAG, "onClick: Attempting to submit to database: \n" +
                        "name: " + name + "\n" +
                        "address: " + address + "\n" +
                        "phone number: " + phoneNum + "\n" +
                        "type: " + type + "\n"
                );

                //handle the exception if the EditText fields are null
                if(!name.equals("") && !address.equals("") && !phoneNum.equals("")){
                    User user = new User(address,name,phoneNum,type);
                    myRef.child("users").child(userID).setValue(user);
                    toastMessage("New Information has been saved.");
                    etName.setText("");
                    etAddress.setText("");
                    etPhone.setText("");
                }else{
                    toastMessage("Fill out all the fields");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_navigation_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                //logout of app
                Toast.makeText(EnterDetails.this, "Signing out...", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                startActivity(new Intent(EnterDetails.this, MainActivity.class));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    //customized toast message
    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}
