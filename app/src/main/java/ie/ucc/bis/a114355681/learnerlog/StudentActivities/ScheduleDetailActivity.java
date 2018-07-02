package ie.ucc.bis.a114355681.learnerlog.StudentActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import ie.ucc.bis.a114355681.learnerlog.LoginActivities.MainActivity;
import ie.ucc.bis.a114355681.learnerlog.R;

public class ScheduleDetailActivity extends AppCompatActivity {

    //To distinguish which activity class has logged the information in logcat
    private static final String TAG = "ScheduleDetailActivity";

    private String sender_user_id, reciever_user_id;

    //declare screen elements
    private TextView txtLessonNo, txtDate, txtTime, txtLocation, txtStatus;
    private Button btnCancel, btnBook;

    //Declare firebase objects
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef, bookingRef, mUserRef, notificationReference;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_detail);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //initialize screen elements
        txtLessonNo = (TextView) findViewById(R.id.txtLessonNo);
        txtDate = (TextView) findViewById(R.id.txtDate);
        txtTime = (TextView)findViewById(R.id.txtTime);
        txtLocation = (TextView)findViewById(R.id.txtLocation);
        txtStatus = (TextView)findViewById(R.id.txtStatus);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnBook = (Button)findViewById(R.id.btnBook);

        //initialize notification reference
        notificationReference = FirebaseDatabase.getInstance().getReference().child("Notifications");

        //this gets the booking key from the previous activity and finds related details in the booking table
        String booking_key = getIntent().getExtras().getString("booking_key");

        bookingRef = FirebaseDatabase.getInstance().getReference().child("bookings").child(booking_key);

        bookingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //set the text views with values from database
                String date = dataSnapshot.child("date").getValue().toString();
                String time = dataSnapshot.child("time").getValue().toString();
                String location = dataSnapshot.child("location").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String lessonType = dataSnapshot.child("lessonType").getValue().toString();
                String lessonNum = dataSnapshot.child("lessonNum").getValue().toString();
                txtDate.setText(date);
                txtTime.setText(time);
                txtLocation.setText(location);
                txtStatus.setText("This lesson has been " + status + ".");
                txtLessonNo.setText("Lesson " + lessonNum + " - " + lessonType);

                //disables and greys out the cancel button if the status is either declined, cancelled or completed
                String dateTime = dataSnapshot.child("date").getValue().toString();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyy");
                try {
                    Date strDate = sdf.parse(dateTime);
                    if ((status.equals("requested")) && (System.currentTimeMillis() > strDate.getTime())){
                        btnCancel.setAlpha(.5f);
                        btnCancel.setEnabled(false);
                    }
                } catch (ParseException e){
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (status.equals("declined") || status.equals("cancelled") || status.equals("completed")) {
                    btnCancel.setAlpha(.5f);
                    btnCancel.setEnabled(false);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //updates the status to completed if the confirmed lesson date has passed
        bookingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String dateTime = dataSnapshot.child("date").getValue().toString();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyy");
                try {
                    Date strDate = sdf.parse(dateTime);
                    String status = dataSnapshot.child("status").getValue().toString();
                    if ((status.equals("confirmed")) && (System.currentTimeMillis() > strDate.getTime())){
                            bookingRef.child("status").setValue("completed");
                    }
                } catch (ParseException e){
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //declare the database reference object. This is what we use to access the database.
        //NOTE: Unless you are signed in, this will not be useable.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mCurrentUser = mAuth.getCurrentUser();

        sender_user_id = mAuth.getCurrentUser().getUid();
        reciever_user_id = "gAwViFMz48Qsz6hVI07z3iHibtv1";

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

        //onClickListener for btnBook
        //this will use an intent to display the booking activity
        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScheduleDetailActivity.this, BookingActivity.class));
            }
        });

        //OnClickListener for btnCancel
        //will handle the cancelling of a lesson
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String dateTime = dataSnapshot.child("date").getValue().toString();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyy");
                        try {
                            //parses date from database
                            Date strDate = sdf.parse(dateTime);
                            //gets the difference between the current time and the date of the booking
                            Long difference = strDate.getTime() - System.currentTimeMillis();
                            //if the booking is greater than 48 hours, the student will not lose out on their lesson
                            if ((difference / (1000 * 60 * 60)) > 48) {
                                //an alert dialog to ask the user if they are sure they want to cancel the lesson
                                AlertDialog.Builder builder;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    builder = new AlertDialog.Builder(ScheduleDetailActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                                } else {
                                    builder = new AlertDialog.Builder(ScheduleDetailActivity.this);
                                }
                                //title of alert dialog
                                builder.setTitle("Cancel Lesson")
                                        //alert dialog message to be displayed
                                        .setMessage("Are you sure you want to cancel this lesson?")
                                        //option 1 is yes; the user wants to cancel the lesson
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // continue with cancellation
                                                bookingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        //set the status to cancelled
                                                        bookingRef.child("status").setValue("cancelled");
                                                        final String studentID = dataSnapshot.child("uid").getValue().toString();
                                                        mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(studentID);
                                                        mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                Long lessons = (Long) dataSnapshot.child("lessons").getValue();
                                                                mUserRef.child("lessons").setValue(lessons + 1);
                                                                Long lessonNum = (Long) dataSnapshot.child("lessonNum").getValue();
                                                                mUserRef.child("lessonNum").setValue(lessonNum - 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            notifyInstructor();
                                                                        }
                                                                    }
                                                                });
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        })
                                        //option 2 is no; the user does not want to cancel the lesson
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // do nothing
                                            }
                                        })
                                        //sets the warning icon in the alert dialog
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                                //if the booking is less than 48 hours, the student will lose their lesson
                            } else if ((difference / (1000 * 60 * 60)) < 48){
                                //an alert dialog to ask the user if they are sure they want to cancel the lesson
                                AlertDialog.Builder builder;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    builder = new AlertDialog.Builder(ScheduleDetailActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                                } else {
                                    builder = new AlertDialog.Builder(ScheduleDetailActivity.this);
                                }
                                //title of alert dialog
                                builder.setTitle("Cancel Lesson")
                                        //alert dialog message to be displayed
                                        .setMessage("Are you sure you want to cancel this lesson? Without 48 hours notice, this lesson will be charged at full price.")
                                        //option 1 is yes; the user wants to cancel the lesson
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // continue with cancellation
                                                bookingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        //set the status to cancelled
                                                        bookingRef.child("status").setValue("cancelled");
                                                        final String studentID = dataSnapshot.child("uid").getValue().toString();
                                                        mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(studentID);
                                                        mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                //updates the students lesson number
                                                                Long lessonNum = (Long) dataSnapshot.child("lessonNum").getValue();
                                                                mUserRef.child("lessonNum").setValue(lessonNum - 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            notifyInstructor();
                                                                        }
                                                                    }
                                                                });
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        })
                                        //option 2 is no; the user does not want to cancel the lesson
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // do nothing
                                            }
                                        })
                                        //sets the warning icon in the alert dialog
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                            }

                        } catch (ParseException e){
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
    }

    private void notifyInstructor() {
        //create a hash map to hold notification data
        HashMap<String, String> notificationsData = new HashMap<String, String>();
        notificationsData.put("from", sender_user_id);
        notificationsData.put("type", "cancellation");

        //create a new notification in the database with a unique key
        notificationReference.child(reciever_user_id).push().setValue(notificationsData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                return;
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
                Toast.makeText(ScheduleDetailActivity.this, "Signing out...", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                startActivity(new Intent(ScheduleDetailActivity.this, MainActivity.class));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
