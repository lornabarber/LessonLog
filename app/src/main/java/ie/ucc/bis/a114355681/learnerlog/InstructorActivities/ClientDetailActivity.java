package ie.ucc.bis.a114355681.learnerlog.InstructorActivities;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.apache.http.protocol.HTTP;

import ie.ucc.bis.a114355681.learnerlog.LoginActivities.MainActivity;
import ie.ucc.bis.a114355681.learnerlog.R;
import ie.ucc.bis.a114355681.learnerlog.datamodel.Booking;

public class ClientDetailActivity extends AppCompatActivity {

    //To distinguish which activity class has logged the information in logcat
    private static final String TAG = "ClientDetailActivity";

    //recycler view to hold lesson history
    private RecyclerView mLessonRecord;

    private String userID;

    ImageButton imgText, imgPhone, imgEmail;

    //declare text views that will be used to hold student details
    private TextView txtName, txtAddress, txtNumber, txtEmail;

    //Declare firebase objects
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef, mUser, userDetails, userLessons;

    Query lessonQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_detail);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //initialize text views
        txtName = (TextView) findViewById(R.id.txtName);
        txtAddress = (TextView) findViewById(R.id.txtAddress);
        txtNumber = (TextView)findViewById(R.id.txtNumber);
        txtEmail = (TextView)findViewById(R.id.txtEmail);

        imgText = (ImageButton) findViewById(R.id.imgText);
        imgPhone = (ImageButton) findViewById(R.id.imgPhone);
        imgEmail = (ImageButton) findViewById(R.id.imgEmail);

        //add the students contact details to the text views
        String user_id = getIntent().getExtras().getString("user_id");
        userDetails = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        userDetails.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                txtName.setText(name);

                String address  = dataSnapshot.child("address").getValue().toString();
                txtAddress.setText(address);

                String phone = dataSnapshot.child("phone").getValue().toString();
                txtNumber.setText(phone);

                String email = dataSnapshot.child("email").getValue().toString();
                txtEmail.setText(email);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //set the layout to hold the details in the lesson record list
        mLessonRecord = (RecyclerView)findViewById(R.id.lesson_record);
        mLessonRecord.setHasFixedSize(true);
        mLessonRecord.setLayoutManager(new LinearLayoutManager(this));

        //retrieve the bookings related to the current user
        userLessons = FirebaseDatabase.getInstance().getReference().child("bookings");
        lessonQuery = userLessons.orderByChild("uid").equalTo(user_id);

        //declare the database reference object. This is what we use to access the database.
        //NOTE: Unless you are signed in, this will not be useable.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

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

        imgPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + txtNumber.getText()));

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        imgText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType(HTTP.PLAIN_TEXT_TYPE);
                intent.setData(Uri.parse("smsto: " + txtNumber.getText()));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        imgEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
            intent.putExtra(Intent.EXTRA_EMAIL, txtEmail.getText());

            ComponentName emailApp = intent.resolveActivity(getPackageManager());
            ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
            boolean hasEmailApp = emailApp != null && !emailApp.equals(unsupportedAction);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(intent, "Choose an application to send your mail with"));
            }
            }
        });
    }

    //create a LessonRecordViewHolder for the lesson record recycler view
    public static class LessonRecordViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public LessonRecordViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        //set lesson date using the booking model class
        public void setDate(String date){
            TextView txtDate = (TextView) mView.findViewById(R.id.txtDate);
            txtDate.setText(date);
        }

        //set students lessonNum using the booking model class
        public void setLessonNum(String lessonNum) {
            TextView txtLessonNum = (TextView) mView.findViewById(R.id.txtLessonNum);
            txtLessonNum.setText(lessonNum);
        }

        //set the lesson status using booking model class
        public void setStatus(String status){
            TextView txtStatus = (TextView) mView.findViewById(R.id.txtStatus);
            txtStatus.setText(status);
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
                Toast.makeText(ClientDetailActivity.this, "Signing out...", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                startActivity(new Intent(ClientDetailActivity.this, MainActivity.class));
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

        /*creates a new FirebaseRecyclerAdapter object called firebaseRecyclerAdapter
        Uses the booking class, lesson_record_row layout file (containing the recycler view layout),
        the LessonRecordViewHolder class and the lessonQuery as parameters */

        final FirebaseRecyclerAdapter<Booking, ClientDetailActivity.LessonRecordViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Booking, ClientDetailActivity.LessonRecordViewHolder>(
                Booking.class, R.layout.lesson_record_row, ClientDetailActivity.LessonRecordViewHolder.class, lessonQuery) {

            @Override
            //populate the viewHolder with lesson details from the Bookings model
            protected void populateViewHolder(ClientDetailActivity.LessonRecordViewHolder viewHolder, Booking model, int position){

                //holds the booking key associated with the selected booking in the recycler view
                final String booking_key = getRef(position).getKey();

                //set the viewHolder with name, lesson number, date, time and location
                viewHolder.setLessonNum("Lesson " + model.getLessonNum());
                viewHolder.setDate(model.getDate());
                viewHolder.setStatus(model.getStatus());

            }
        };

        //fills the request list with data in firebaseRecyclerAdapter
        mLessonRecord.setAdapter(firebaseRecyclerAdapter);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
