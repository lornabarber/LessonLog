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
import ie.ucc.bis.a114355681.learnerlog.HelperClasses.BottomNavigationViewHelper;
import ie.ucc.bis.a114355681.learnerlog.datamodel.User;

public class ClientListActivity extends AppCompatActivity {

    //To distinguish which activity class has logged the information in logcat
    private static final String TAG = "ClientListActivity";

    private RecyclerView mClientList;

    TextView txtName;
    ImageButton imgSearch;
    EditText txtSearchName;

    //will hold the current user id
    private String userID;

    String type = "student";

    //Declare firebase objects
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef, mUser, mName;

    Query mUserQuery, mNameQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_list);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //removes animation from bottom navigation bar
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView_Bar);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        //highlights the selected menu item
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    //request activity
                    case R.id.ic_requests:
                        startActivity(new Intent(ClientListActivity.this, RequestActivity.class));
                        break;

                    //schedule activity
                    case R.id.ic_schedule:
                        startActivity(new Intent(ClientListActivity.this, InstructorScheduleActivity.class));
                        break;

                    //current activity
                    case R.id.ic_students:
                        break;

                    //payment activity
                    case R.id.ic_payments:
                        startActivity(new Intent(ClientListActivity.this, PaymentsReceivedActivity.class));

                }
                return true;
            }
        });

        //set the layout of the recycler view which will hold the booking details
        mClientList = (RecyclerView)findViewById(R.id.client_list);
        mClientList.setHasFixedSize(true);
        mClientList.setLayoutManager(new LinearLayoutManager(this));

        //initalizes name text view
        txtName = (TextView)findViewById(R.id.txtName);
        txtSearchName = (EditText) findViewById(R.id.txtSearchName);

        //retrieves the users from the database and orders them by name
        mUser = FirebaseDatabase.getInstance().getReference().child("students");
        mUserQuery = mUser.orderByChild("name");

        imgSearch = (ImageButton) findViewById(R.id.imgSearch);
        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchStudents();
            }
        });


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
    }

    protected void  searchStudents(){
        String name = txtSearchName.getText().toString();

        if (name.matches("")) {
            resetRecyclerView();
        } else if (name.matches("")){
            resetRecyclerView();
        } else {
            mNameQuery = mUser.orderByChild("name").equalTo(name);
            updateRecyclerView();
        }

    }

    protected void resetRecyclerView() {

        FirebaseRecyclerAdapter<User, ClientListActivity.ClientViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, ClientViewHolder>(
                User.class, R.layout.client_row, ClientListActivity.ClientViewHolder.class, mUserQuery) {
            @Override
            protected void populateViewHolder(ClientListActivity.ClientViewHolder viewHolder, User model, int position) {

                //retrieve booking key
                final String user_id = getRef(position).getKey();

                //add details to recycler view
                viewHolder.setName(model.getName());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singleClientIntent = new Intent(ClientListActivity.this, ClientDetailActivity.class);
                        singleClientIntent.putExtra("user_id", user_id);
                        startActivity(singleClientIntent);
                    }
                });
            }
        };
        mClientList.setAdapter(firebaseRecyclerAdapter);
    }

    protected void updateRecyclerView() {

        FirebaseRecyclerAdapter<User, ClientListActivity.ClientViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, ClientViewHolder>(
                User.class, R.layout.client_row, ClientListActivity.ClientViewHolder.class, mNameQuery) {
            @Override
            protected void populateViewHolder(ClientListActivity.ClientViewHolder viewHolder, User model, int position) {

                //retrieve booking key
                final String user_id = getRef(position).getKey();

                //add details to recycler view
                viewHolder.setName(model.getName());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singleClientIntent = new Intent(ClientListActivity.this, ClientDetailActivity.class);
                        singleClientIntent.putExtra("user_id", user_id);
                        startActivity(singleClientIntent);
                    }
                });
            }
        };
        mClientList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ClientViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public ClientViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name) {
            TextView txtName = (TextView) mView.findViewById(R.id.txtName);
            txtName.setText(name);
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
                Toast.makeText(ClientListActivity.this, "Signing out...", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                startActivity(new Intent(ClientListActivity.this, MainActivity.class));
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

        //adds auth listener
        mAuth.addAuthStateListener(mAuthListener);

        FirebaseRecyclerAdapter<User, ClientListActivity.ClientViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, ClientViewHolder>(
                User.class, R.layout.client_row, ClientListActivity.ClientViewHolder.class, mUserQuery) {
            @Override
            protected void populateViewHolder(ClientListActivity.ClientViewHolder viewHolder, User model, int position) {

                //retrieve booking key
                final String user_id = getRef(position).getKey();

                //add details to recycler view
                viewHolder.setName(model.getName());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singleClientIntent = new Intent(ClientListActivity.this, ClientDetailActivity.class);
                        singleClientIntent.putExtra("user_id", user_id);
                        startActivity(singleClientIntent);
                    }
                });
            }
        };
        mClientList.setAdapter(firebaseRecyclerAdapter);
    }


    @Override
    public void onStop() {
        super.onStop();
        //removes authListener
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}