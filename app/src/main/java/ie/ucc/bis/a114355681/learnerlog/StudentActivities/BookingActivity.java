package ie.ucc.bis.a114355681.learnerlog.StudentActivities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ie.ucc.bis.a114355681.learnerlog.LoginActivities.MainActivity;
import ie.ucc.bis.a114355681.learnerlog.datamodel.Booking;
import ie.ucc.bis.a114355681.learnerlog.HelperClasses.BottomNavigationViewHelper;
import ie.ucc.bis.a114355681.learnerlog.R;

public class BookingActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    //To distinguish which activity class has logged the information in logcat
    private static final String TAG = "BookingActivity";

    //declare variables that will be assigned to layout
    private Button btnPickDate, btnBook;
    private TextView txtDateResult, txtLessonNum;
    private EditText etLocation;
    private RadioButton rdoRegular, rdoTest;
    Spinner spTimes;

    String date;

    //Will hold the userID
    private String userID, sender_user_id, reciever_user_id;

    //to hold date picked
    int day, month, year;
    int dayFinal, monthFinal, yearFinal;

    //Declare firebase objects
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private FirebaseUser mCurrentUser;

    //reference to firebase declared
    DatabaseReference bookingTable, databaseUser, mUserAddress, mAvailability, mPaidLessons, notificationReference;

    Query availabilityQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking); //set the activities layout

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //removes animation from bottom navigation bar
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView_Bar);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        //highlights the selected menu item
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        //sets the listener for the bottom nav bar
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    //displays the home activity
                    case R.id.ic_lessons:
                        startActivity(new Intent(BookingActivity.this, ScheduleActivity.class));
                        break;

                    //current activity
                    case R.id.ic_book:
                        break;

                    //displays the payments activity
                    case R.id.ic_payments:
                        startActivity(new Intent(BookingActivity.this, LessonPaymentActivity.class));
                        break;

                    //displays the settings activity
                    case R.id.ic_settings:
                        startActivity(new Intent(BookingActivity.this, EnterDetails.class));
                        break;
                }
                return true;
            }
        });

        //initialize buttons, textview, edittext and spinners by finding their id in the layout file
        btnBook = (Button) findViewById(R.id.btnBook);
        txtDateResult = (TextView) findViewById(R.id.txtDateResult);
        btnPickDate = (Button)findViewById(R.id.btnPickDate);
        spTimes = (Spinner) findViewById(R.id.spTimes);
        etLocation = (EditText) findViewById(R.id.etLocation);
        txtLessonNum = (TextView)findViewById(R.id.txtLessonNum);
        rdoRegular = (RadioButton)findViewById(R.id.rdoRegular);
        rdoTest= (RadioButton)findViewById(R.id.rdoTest);

        //declare the database reference object. This is what we use to access the database.
        //NOTE: Unless you are signed in, this will not be useable.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mCurrentUser = mAuth.getCurrentUser();

        sender_user_id = mAuth.getCurrentUser().getUid();
        reciever_user_id = "gAwViFMz48Qsz6hVI07z3iHibtv1";

        //initalize firebase reference to booking table
        bookingTable = FirebaseDatabase.getInstance().getReference("bookings");

        notificationReference = FirebaseDatabase.getInstance().getReference().child("Notifications");
        notificationReference.keepSynced(true);

        //reference to the userID of the currently logged in user
        databaseUser = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentUser.getUid());
        databaseUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //updates lessonNum value
                Long lessonNum = (Long) dataSnapshot.child("lessonNum").getValue();
                if (dataSnapshot.child("lessonNum").exists()){
                    txtLessonNum.setText("Lesson " + String.valueOf(lessonNum + 1));
                } else {
                    txtLessonNum.setText("Lesson " + 1);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //database reference to the address of the currently logged in user to get their address
        mUserAddress = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentUser.getUid()).child("address");
        //the ValueEventListener will get the value of the database reference and convert it to String. Then it will display in the location text view.
        mUserAddress.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userAddress = dataSnapshot.getValue().toString();
                etLocation.setText(userAddress);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //gets the currently logged in user and their userID
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

        //onClick listener for date picker.
        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);

                //create a new DatePickerDialog for the current activity
                DatePickerDialog datePickerDialog = new DatePickerDialog(BookingActivity.this, BookingActivity.this,
                        year, month, day);

                //disable past dates so they can't be selected
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

                //display the DatePicker
                datePickerDialog.show();
            }
        });

        //onClickListener for btnBook
        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mPaidLessons = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentUser.getUid()).child("lessons");

                mPaidLessons.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Long lessons = (Long) dataSnapshot.getValue();
                            if (dataSnapshot.getValue() == null) {
                                Toast.makeText(BookingActivity.this, "You must pay for lessons before you can secure a booking.", Toast.LENGTH_SHORT).show();
                                return;
                            } else if (lessons == 0) {
                                Toast.makeText(BookingActivity.this, "You must pay for lessons before you can secure a booking.", Toast.LENGTH_SHORT).show();
                                return;
                            } else {
                                //calls addBooking() method to transfer to database if booking is available
                                addBooking();
                            }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void addBooking(){

        //create strings to hold the entered details
        final String date = txtDateResult.getText().toString().trim();
        final String time = spTimes.getSelectedItem().toString();
        final String location = etLocation.getText().toString().trim();
        final String status = "requested";
        final String date_time = date + " " + time;

        //If all details are filled in, save to database
        if(!TextUtils.isEmpty(date) && !TextUtils.isEmpty(time) && !TextUtils.isEmpty(location) && (rdoRegular.isChecked() || (rdoTest.isChecked()))){

            //addValueEventListener for databaseUser, this will set the values for the booking into the database
            myRef.child("users").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            //add booking details
                public void onDataChange(DataSnapshot dataSnapshot) {

                final Map<String, String> bookingData = new HashMap<String, String>();

                String name = dataSnapshot.child("name").getValue().toString();
                String userID = mAuth.getCurrentUser().getUid();

                final Long lessonNum = (Long) dataSnapshot.child("lessonNum").getValue();
                if (dataSnapshot.child("lessonNum").exists()){
                    //add lesson number
                    Long lessonNumber = lessonNum + 1;
                    bookingData.put("lessonNum", String.valueOf(lessonNumber));
                    myRef.child("users").child(mCurrentUser.getUid()).child("lessonNum").setValue(lessonNumber);
                } else {
                    Long lessonNumber = Long.valueOf(1);
                    bookingData.put("lessonNum", String.valueOf(lessonNumber));
                    myRef.child("users").child(mCurrentUser.getUid()).child("lessonNum").setValue(lessonNumber);
                }

                bookingData.put("date", date);
                bookingData.put("time", time);
                bookingData.put("location", location);
                bookingData.put("status", status);
                bookingData.put("date_time", date_time);
                bookingData.put("name", name);
                bookingData.put("uid", userID);
                if (rdoRegular.isChecked()) {
                    bookingData.put("lessonType", "Regular Lesson");
                } else if (rdoTest.isChecked()) {
                    bookingData.put("lessonType", "Pre-Test");
                }

                Long lessons = (Long) dataSnapshot.child("lessons").getValue();
                myRef.child("users").child(mCurrentUser.getUid()).child("lessons").setValue(lessons - 1);

                myRef.child("bookings").push().setValue(bookingData).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        notifyInstructor();
                    }
                });
            }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //auto generated
                }
            });


        } else {
            //alert user to fill in all fields.
            Toast.makeText(BookingActivity.this, "Please fill in all required details.", Toast.LENGTH_SHORT).show();
            return;
        }

    }

    private void notifyInstructor() {
        //create a hash map to hold notification data
        HashMap<String, String> notificationsData = new HashMap<String, String>();
        notificationsData.put("from", sender_user_id);
        notificationsData.put("type", "request");

        //create a new notification in the database with a unique key
        myRef.child("Notifications").child(reciever_user_id).push().setValue(notificationsData);
        //display the schedule activity
        startActivity(new Intent(BookingActivity.this, ScheduleActivity.class));
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
                Toast.makeText(BookingActivity.this, "Signing out...", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                startActivity(new Intent(BookingActivity.this, MainActivity.class));
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
        //add the logged in user to the AuthStateListener
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            //remove logged in user
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    //method to display date in text view depending on what date the user selects
    @Override
    public void onDateSet(DatePicker view, int i, int i1, int i2) {
        yearFinal = i;
        monthFinal = i1 + 1;
        dayFinal = i2;

        //display the selected date in the text box.
        txtDateResult.setText(dayFinal + "/" + monthFinal + "/" + yearFinal);

        if(monthFinal < 10){
            txtDateResult.setText(dayFinal + "/0" + monthFinal + "/" + yearFinal);
        }
        if(dayFinal < 10) {
            txtDateResult.setText("0" + dayFinal + "/" + monthFinal + "/" + yearFinal);
        }
        if ((monthFinal < 10) && (dayFinal < 10)) {
            txtDateResult.setText("0" + dayFinal + "/0" + monthFinal + "/" + yearFinal);
        }
            date = txtDateResult.getText().toString();

        //creates a spinner array to store the times that can be selected
        //https://stackoverflow.com/questions/11920754/android-fill-spinner-from-java-code-programmatically
        final List<String> spinnerArray =  new ArrayList<String>();
        spinnerArray.add("08:00 AM");
        spinnerArray.add("09:00 AM");
        spinnerArray.add("10:00 AM");
        spinnerArray.add("11:00 AM");
        spinnerArray.add("12:00 PM");
        spinnerArray.add("13:00 PM");
        spinnerArray.add("14:00 PM");
        spinnerArray.add("15:00 PM");
        spinnerArray.add("16:00 PM");
        spinnerArray.add("17:00 PM");
        spinnerArray.add("18:00 PM");
        spinnerArray.add("19:00 PM");

        //fills the array adapter with the spinner values and sets the layout of each spinner item
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //sets the spinner to the spinner created in layout file
        final Spinner sItems = (Spinner) findViewById(R.id.spTimes);
        //fills the spinner using the adapter
        sItems.setAdapter(adapter);

        //query the bookings table to find all existing bookings with the selected date
        mAvailability = FirebaseDatabase.getInstance().getReference().child("bookings");
        availabilityQuery = mAvailability.orderByChild("date").equalTo(date);

        availabilityQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

               /* gets a snapshot of all the children within the nodes that have the date selected
                it will then create a model of these values to compare it to a string time value
                if the value exists, its removed from the spinner so it can't be selected. */

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Booking booking = snapshot.getValue(Booking.class);
                    if (booking.getTime().equals("08:00 AM")) {
                        spinnerArray.remove(0);
                        adapter.notifyDataSetChanged();

                    }
                    if (booking.getTime().equals("09:00 AM")) {
                        spinnerArray.remove(1);
                        adapter.notifyDataSetChanged();

                    }
                    if (booking.getTime().equals("10:00 AM")) {
                        spinnerArray.remove(2);
                        adapter.notifyDataSetChanged();

                    }
                    if (booking.getTime().equals("11:00 AM")) {
                        spinnerArray.remove(3);
                        adapter.notifyDataSetChanged();

                    }
                    if (booking.getTime().equals("12:00 PM")) {
                        spinnerArray.remove(4);
                        adapter.notifyDataSetChanged();

                    }
                    if (booking.getTime().equals("13:00 PM")) {
                        spinnerArray.remove(5);
                        adapter.notifyDataSetChanged();

                    }
                    if (booking.getTime().equals("14:00 PM")) {
                        spinnerArray.remove(6);
                        adapter.notifyDataSetChanged();

                    }
                    if (booking.getTime().equals("15:00 PM")) {
                        spinnerArray.remove(7);
                        adapter.notifyDataSetChanged();

                    }
                    if (booking.getTime().equals("16:00 PM")) {
                        spinnerArray.remove(8);
                        adapter.notifyDataSetChanged();

                    }
                    if (booking.getTime().equals("17:00 PM")) {
                        spinnerArray.remove(9);
                        adapter.notifyDataSetChanged();

                    }
                    if (booking.getTime().equals("18:00 PM")) {
                        spinnerArray.remove(10);
                        adapter.notifyDataSetChanged();

                    }

                    if (booking.getTime().equals("19:00 PM")) {
                        spinnerArray.remove(11);
                        adapter.notifyDataSetChanged();

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
