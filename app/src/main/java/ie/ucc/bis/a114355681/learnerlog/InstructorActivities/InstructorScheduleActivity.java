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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import ie.ucc.bis.a114355681.learnerlog.LoginActivities.MainActivity;
import ie.ucc.bis.a114355681.learnerlog.R;
import ie.ucc.bis.a114355681.learnerlog.datamodel.Booking;
import ie.ucc.bis.a114355681.learnerlog.HelperClasses.BottomNavigationViewHelper;
import ie.ucc.bis.a114355681.learnerlog.HelperClasses.DateInputMask;

public class InstructorScheduleActivity extends AppCompatActivity {

    //To distinguish which activity class has logged the information in logcat
    private static final String TAG = "InstructorSchedule";

    private RecyclerView mBookingList;

    //hold the current user id
    private String userID, status;

    //declare firebase objects
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;

    //reference to booking table in the database to retrieve the booking status
    DatabaseReference mDatabaseStatus;

    //Status Query
    private Query mStatusQuery, mDateQuery;

    EditText txtSearchDate;
    ImageButton imgSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor_schedule);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //removes animation from bottom navigation bar
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView_Bar);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        //highlights the selected menu item
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){ //request activity
                    case R.id.ic_requests:
                        startActivity(new Intent(InstructorScheduleActivity.this, RequestActivity.class));
                        break;

                    //current activity
                    case R.id.ic_schedule:
                        break;

                    //student activity
                    case R.id.ic_students:
                        startActivity(new Intent(InstructorScheduleActivity.this, ClientListActivity.class));
                        break;

                    //payment activity
                    case R.id.ic_payments:
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

        //set the layout of the recycler view which will hold the booking details
        mBookingList = (RecyclerView)findViewById(R.id.confirmed_bookings);
        mBookingList.setHasFixedSize(true);
        mBookingList.setLayoutManager(new LinearLayoutManager(this));

        //Query to retrieve bookings with a confirmed status from bookings table
        status = "confirmed";
        mDatabaseStatus = FirebaseDatabase.getInstance().getReference().child("bookings");
        mStatusQuery = mDatabaseStatus.orderByChild("status").equalTo(status);

        txtSearchDate = (EditText) findViewById(R.id.txtSearchDate);
        new DateInputMask(txtSearchDate);

        imgSearch = (ImageButton) findViewById(R.id.imgSearch);
        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchLessons();
            }
        });


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

    public static class BookingViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public BookingViewHolder(View itemView) {
        super(itemView);

        mView = itemView;
    }

    public void setName(String name){
        TextView txtUser = (TextView) mView.findViewById(R.id.txtUser);
        txtUser.setText(name);
    }

    public void setLessonType(String lessonType) {
        TextView txtLessonNo = (TextView) mView.findViewById(R.id.txtLessonNo);
        txtLessonNo.setText(lessonType);
    }

    public void setDate(String date){
        TextView txtLessonDate = (TextView) mView.findViewById(R.id.txtLessonDate);
        txtLessonDate.setText(date);
    }

    public void setTime(String time){
        TextView txtLessonTime = (TextView) mView.findViewById(R.id.txtLessonTime);
        txtLessonTime.setText(time);
    }

    public void setLocation(String location){
        TextView txtLocation = (TextView) mView.findViewById(R.id.txtLocation);
        txtLocation.setText(location);
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
                Toast.makeText(InstructorScheduleActivity.this, "Signing out...", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                startActivity(new Intent(InstructorScheduleActivity.this, MainActivity.class));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    protected void searchLessons(){
        String date = txtSearchDate.getText().toString();

        if (date.matches("dd/mm/yyyy")) {
            resetRecyclerView();
        } else if (date.matches("")){
            resetRecyclerView();
        } else {
            mDateQuery = mDatabaseStatus.orderByChild("date").equalTo(date);
            updateRecyclerView();
        }
    }

    protected void updateRecyclerView(){
        FirebaseRecyclerAdapter<Booking, InstructorScheduleActivity.BookingViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Booking, InstructorScheduleActivity.BookingViewHolder>(
                Booking.class, R.layout.schedule_row, InstructorScheduleActivity.BookingViewHolder.class, mDateQuery) {
            @Override
            protected void populateViewHolder(InstructorScheduleActivity.BookingViewHolder viewHolder, Booking model, int position) {

                viewHolder.setName(model.getName());
                viewHolder.setLessonType(model.getLessonType());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());
                viewHolder.setLocation(model.getLocation());

            }
        };
        mBookingList.setAdapter(firebaseRecyclerAdapter);
    }

    protected void resetRecyclerView(){
        FirebaseRecyclerAdapter<Booking, InstructorScheduleActivity.BookingViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Booking, InstructorScheduleActivity.BookingViewHolder>(
                Booking.class, R.layout.schedule_row, InstructorScheduleActivity.BookingViewHolder.class, mStatusQuery) {
            @Override
            protected void populateViewHolder(InstructorScheduleActivity.BookingViewHolder viewHolder, Booking model, int position) {

                viewHolder.setName(model.getName());
                viewHolder.setLessonType(model.getLessonType());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());
                viewHolder.setLocation(model.getLocation());

            }
        };
        mBookingList.setAdapter(firebaseRecyclerAdapter);
    }
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Booking, InstructorScheduleActivity.BookingViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Booking, InstructorScheduleActivity.BookingViewHolder>(
                Booking.class, R.layout.schedule_row, InstructorScheduleActivity.BookingViewHolder.class, mStatusQuery) {
            @Override
            protected void populateViewHolder(InstructorScheduleActivity.BookingViewHolder viewHolder, Booking model, int position) {

                viewHolder.setName(model.getName());
                viewHolder.setLessonType(model.getLessonType());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());
                viewHolder.setLocation(model.getLocation());

            }
        };
            mBookingList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
