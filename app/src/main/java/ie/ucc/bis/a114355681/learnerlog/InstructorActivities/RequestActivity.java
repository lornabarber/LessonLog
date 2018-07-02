package ie.ucc.bis.a114355681.learnerlog.InstructorActivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import ie.ucc.bis.a114355681.learnerlog.LoginActivities.MainActivity;
import ie.ucc.bis.a114355681.learnerlog.R;
import ie.ucc.bis.a114355681.learnerlog.datamodel.Booking;
import ie.ucc.bis.a114355681.learnerlog.HelperClasses.BottomNavigationViewHelper;

public class RequestActivity extends AppCompatActivity {

    //To distinguish which activity class has logged the information in logcat
    private static final String TAG = "RequestActivity";

    //recycler view to hold the request list
    private RecyclerView mRequestList;

    TextView txtNoRequests;

    //hold the current user id
    private String userID, sender_user_id, reciever_user_id;

    //declare firebase objects
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;

    //will be used to query the database
    private Query mStatusQuery, mUserQuery;

    //string that holds the lesson status to be retrieved
    private String status = "requested";

    //reference to booking table in the database to retrieve the booking status
    DatabaseReference mDatabaseStatus, mDatabaseBooking, mUser, mPaidLessons, notificationReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //removes animation from bottom navigation bar
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView_Bar);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        //highlights the selected menu item
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    //current activity
                    case R.id.ic_requests:
                        break;

                    //schedule activity
                    case R.id.ic_schedule:
                        startActivity(new Intent(RequestActivity.this, InstructorScheduleActivity.class));
                        break;

                    //student activity
                    case R.id.ic_students:
                        startActivity(new Intent(RequestActivity.this, ClientListActivity.class));
                        break;

                    //payment activity
                    case R.id.ic_payments:
                        startActivity(new Intent(RequestActivity.this, PaymentsReceivedActivity.class));
                }
                return true;
            }
        });

        //declare the database reference object. This is what we use to access the database.
        //NOTE: Unless you are signed in, this will not be useable.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        sender_user_id = mAuth.getCurrentUser().getUid();

        notificationReference = FirebaseDatabase.getInstance().getReference().child("Notifications");

        //set the layout to hold the details in the request list
        mRequestList = (RecyclerView)findViewById(R.id.request_list);
        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(this));

        //Query the database to get the bookings with a "requested" status
        mDatabaseStatus = FirebaseDatabase.getInstance().getReference().child("bookings");
        mStatusQuery = mDatabaseStatus.orderByChild("status").equalTo(status);

        //reference to the bookings table
        mDatabaseBooking = FirebaseDatabase.getInstance().getReference().child("bookings");


        //checks the currently logged in user
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

        txtNoRequests = (TextView) findViewById(R.id.txtNoRequests);
    }

    //create a RequestViewHolder for the requests recycler view
    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        View mView;

        //declare accept and reject buttons
        Button btnAccept, btnReject;

        public RequestViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            //initialize buttons
            btnAccept = (Button) mView.findViewById(R.id.btnAccept);
            btnReject = (Button) mView.findViewById(R.id.btnReject);
        }

        //set students name using the booking model class
        public void setName(String name){
            TextView txtUser = (TextView) mView.findViewById(R.id.txtUser);
            txtUser.setText(name);
        }

        //set students lessonNum using the booking model class
        public void setLessonType(String lessonType) {
            TextView txtLessonNo = (TextView) mView.findViewById(R.id.txtLessonNo);
            txtLessonNo.setText(lessonType);
        }

        //set lesson date using the booking model class
        public void setDate(String date){
            TextView txtLessonDate = (TextView) mView.findViewById(R.id.txtLessonDate);
            txtLessonDate.setText(date);
        }

        //set lesson time using the booking model class
        public void setTime(String time){
            TextView txtLessonTime = (TextView) mView.findViewById(R.id.txtLessonTime);
            txtLessonTime.setText(time);
        }

        //set location using the booking model class
        public void setLocation(String location){
            TextView txtLocation = (TextView) mView.findViewById(R.id.txtLocation);
            txtLocation.setText(location);
        }
    }


    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

       /*creates a new FirebaseRecyclerAdapter object called firebaseRecyclerAdapter
        Uses the booking class, request_row layout file (containing the recycler view layout),
        the RequestViewHolder class and the mStatusQuery as parameters */

        mStatusQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    txtNoRequests.setVisibility(TextView.INVISIBLE);
                    mRequestList.setVisibility(RecyclerView.VISIBLE);

                    final FirebaseRecyclerAdapter<Booking, RequestViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Booking, RequestViewHolder>(
                            Booking.class, R.layout.request_row, RequestViewHolder.class, mStatusQuery) {

                        @Override
                        //populate the viewHolder with lesson details from the Bookings model
                        protected void populateViewHolder(RequestViewHolder viewHolder, Booking model, int position){

                            //set the viewHolder with name, lesson number, date, time and location
                            viewHolder.setName(model.getName());
                            viewHolder.setLessonType(model.getLessonType());
                            viewHolder.setDate(model.getDate());
                            viewHolder.setTime(model.getTime());
                            viewHolder.setLocation(model.getLocation());

                            //holds the booking key associated with the selected booking in the recycler view
                            final String booking_key = getRef(position).getKey();

                            //onClickListener for accept button, will update the booking status to "confirmed"
                            viewHolder.btnAccept.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View v) {
                                    mDatabaseBooking.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            reciever_user_id = dataSnapshot.child(booking_key).child("uid").getValue().toString();
                                            mDatabaseBooking.child(booking_key).child("status").setValue("confirmed").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        //create a hash map to hold notification data
                                                        HashMap<String, String> notificationsData = new HashMap<String, String>();
                                                        notificationsData.put("from", sender_user_id);
                                                        notificationsData.put("type", "confirmation");

                                                        //create a new notification in the database with a unique key
                                                        notificationReference.child(reciever_user_id).push().setValue(notificationsData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }
                            });

                            //onClickListener for reject button, will update the booking status to "rejected"
                            viewHolder.btnReject.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {

                                    //add the ValueEventListener to the mDatabaseBooking reference declared in onCreate
                                    mDatabaseBooking.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            //set the status for the selected booking to declined
                                            mDatabaseBooking.child(booking_key).child("status").setValue("declined");
                                            reciever_user_id = dataSnapshot.child(booking_key).child("uid").getValue().toString();
                                            final String studentID = dataSnapshot.child(booking_key).child("uid").getValue().toString();
                                            //retrieves the students lessons number from users table
                                            mPaidLessons = FirebaseDatabase.getInstance().getReference().child("users").child(studentID);
                                            mPaidLessons.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    //+1 to the lesson number so student isn't charged for rejected lesson
                                                    Long lessons = (Long) dataSnapshot.child("lessons").getValue();
                                                    mPaidLessons.child("lessons").setValue(lessons + 1);
                                                    Long lessonNum = (Long) dataSnapshot.child("lessonNum").getValue();
                                                    mPaidLessons.child("lessonNum").setValue(lessonNum - 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                //create a hash map to hold notification data
                                                                HashMap<String, String> notificationsData = new HashMap<String, String>();
                                                                notificationsData.put("from", sender_user_id);
                                                                notificationsData.put("type", "decline");

                                                                //create a new notification in the database with a unique key
                                                                notificationReference.child(reciever_user_id).push().setValue(notificationsData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {

                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                            mDatabaseBooking.removeEventListener(this);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }
                            });
                        }
                    };

                    //fills the request list with data in firebaseRecyclerAdapter
                    mRequestList.setAdapter(firebaseRecyclerAdapter);
                } else {
                    txtNoRequests.setVisibility(TextView.VISIBLE);
                    mRequestList.setVisibility(RecyclerView.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                Toast.makeText(RequestActivity.this, "Signing out...", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                startActivity(new Intent(RequestActivity.this, MainActivity.class));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
