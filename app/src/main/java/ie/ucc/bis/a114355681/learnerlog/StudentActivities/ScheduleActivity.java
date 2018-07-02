package ie.ucc.bis.a114355681.learnerlog.StudentActivities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.*;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import ie.ucc.bis.a114355681.learnerlog.LoginActivities.MainActivity;
import ie.ucc.bis.a114355681.learnerlog.HelperClasses.BottomNavigationViewHelper;
import ie.ucc.bis.a114355681.learnerlog.R;
import ie.ucc.bis.a114355681.learnerlog.datamodel.Booking;

public class ScheduleActivity extends AppCompatActivity {

    //To distinguish which activity class has logged the information in logcat
    private static final String TAG = "ScheduleActivity";

    private RecyclerView mScheduleList;

    TextView txtNoBookings;

    //will hold the current user id
    private String userID;

    //declare firebase objects
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;

    //query to retrieve current user ID
    private Query mCurrentUserQuery;

    //reference to current user in the database
    DatabaseReference mDatabaseCurrentUser;

    @Override 
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule); //set the layout

        android.support.v7.widget.Toolbar myToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //removes animation from bottom navigation bar
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView_Bar);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        //highlights the selected menu item
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);

        //sets the listener for the bottom nav bar
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    //displays the home activity
                    case R.id.ic_lessons:
                        break;

                    //displays the booking activity
                    case R.id.ic_book:
                        startActivity(new Intent(ScheduleActivity.this, BookingActivity.class));
                        break;

                    //displays the payments activity
                    case R.id.ic_payments:
                        startActivity(new Intent(ScheduleActivity.this, LessonPaymentActivity.class));
                        break;

                    //displays the settings activity
                    case R.id.ic_settings:
                        startActivity(new Intent(ScheduleActivity.this, LessonPaymentActivity.class));
                        break;
                }
                return true;
            }
        });

        //set the layout of the recycler view which will hold the booking details
        mScheduleList = (RecyclerView)findViewById(R.id.requested_bookings);
        mScheduleList.setHasFixedSize(true);
        mScheduleList.setLayoutManager(new LinearLayoutManager(this));

        //declare the database reference object. This is what we use to access the database.
        //NOTE: Unless you are signed in, this will not be useable.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        //create a query to the bookings table to find the bookings related to the logged in user
        mDatabaseCurrentUser = FirebaseDatabase.getInstance().getReference().child("bookings");
        mCurrentUserQuery = mDatabaseCurrentUser.orderByChild("uid").equalTo(userID);

        txtNoBookings = (TextView) findViewById(R.id.txtNoBookings);

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
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ScheduleViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setStatus(String status) {
            TextView etLessonStatus = (TextView) mView.findViewById(R.id.etLessonStatus);
            etLessonStatus.setText(status);
        }

        public void setLessonNum(String lessonNum) {
            TextView etLessonNo = (TextView) mView.findViewById(R.id.etLessonNo);
            etLessonNo.setText(lessonNum);
        }

        public void setDate(String date) {
            TextView etLessonDate = (TextView) mView.findViewById(R.id.etLessonDate);
            etLessonDate.setText(date);
        }

        public void setTime(String time) {
            TextView etLessonTime = (TextView) mView.findViewById(R.id.etLessonTime);
            etLessonTime.setText(time);
        }

        public void setLessonType(String type) {
            TextView etLessonType = (TextView) mView.findViewById(R.id.etLessonType);
            etLessonType.setText(type);
        }
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
                Toast.makeText(ScheduleActivity.this, "Signing out...", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                startActivity(new Intent(ScheduleActivity.this, MainActivity.class));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mCurrentUserQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    txtNoBookings.setVisibility(TextView.INVISIBLE);
                    mScheduleList.setVisibility(RecyclerView.VISIBLE);

                    FirebaseRecyclerAdapter<Booking, ScheduleActivity.ScheduleViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Booking, ScheduleActivity.ScheduleViewHolder>(
                            Booking.class, R.layout.schedule_list_layout, ScheduleActivity.ScheduleViewHolder.class, mCurrentUserQuery) {
                        @Override
                        protected void populateViewHolder(ScheduleActivity.ScheduleViewHolder viewHolder, Booking model, int position) {

                            //retrieve booking key
                            final String booking_key = getRef(position).getKey();

                            //add details to recycler view
                            viewHolder.setStatus(model.getStatus());
                            viewHolder.setLessonNum("Lesson " + model.getLessonNum());
                            viewHolder.setDate(model.getDate());
                            viewHolder.setTime(model.getTime());
                            viewHolder.setLessonType(model.getLessonType());

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent singleLessonIntent = new Intent(ScheduleActivity.this, ScheduleDetailActivity.class);
                                    singleLessonIntent.putExtra("booking_key", booking_key);
                                    startActivity(singleLessonIntent);
                                }
                            });
                        }
                    };
                    mScheduleList.setAdapter(firebaseRecyclerAdapter);

                } else {
                    txtNoBookings.setVisibility(TextView.VISIBLE);
                    mScheduleList.setVisibility(RecyclerView.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
