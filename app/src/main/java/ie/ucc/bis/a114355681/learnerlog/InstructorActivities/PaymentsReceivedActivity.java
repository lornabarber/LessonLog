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
import ie.ucc.bis.a114355681.learnerlog.datamodel.Payment;

public class PaymentsReceivedActivity extends AppCompatActivity {

    private RecyclerView mPaymentList;

    TextView txtName, txtDate, txtAmount;

    //Declare firebase auth and authStateListener objects
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mCurrentUser;

    private DatabaseReference paymentRef;

    Query datequery;

    private static final String TAG = "PaymentReceivedActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments_received);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //removes animation from bottom navigation bar
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView_Bar);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        //highlights the selected menu item
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    //request activity
                    case R.id.ic_requests:
                        startActivity(new Intent(PaymentsReceivedActivity.this, RequestActivity.class));
                        break;

                    //schedule activity
                    case R.id.ic_schedule:
                        startActivity(new Intent(PaymentsReceivedActivity.this, InstructorScheduleActivity.class));
                        break;

                    //student list activity
                    case R.id.ic_students:
                        startActivity(new Intent(PaymentsReceivedActivity.this, ClientListActivity.class));
                        break;

                    //current activity
                    case R.id.ic_payments:
                }
                return true;
            }
        });

        //set the layout of the recycler view which will hold the payment details
        mPaymentList = (RecyclerView)findViewById(R.id.payment_list);
        mPaymentList.setHasFixedSize(true);
        mPaymentList.setLayoutManager(new LinearLayoutManager(this));

        txtName = (TextView)findViewById(R.id.txtName);
        txtDate = (TextView)findViewById(R.id.txtDate);
        txtAmount = (TextView) findViewById(R.id.txtAmount);

        paymentRef = FirebaseDatabase.getInstance().getReference().child("payments");
        datequery = paymentRef.orderByChild("time");

        //initalise firebase auth
        mAuth = FirebaseAuth.getInstance();
        //checks to see if a user is logged in and gets their UserID
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

    public static class PaymentViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public PaymentViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name) {
            TextView txtName = (TextView) mView.findViewById(R.id.txtName);
            txtName.setText(name);
        }
        public void setDate(String date) {
            TextView txtDate = (TextView) mView.findViewById(R.id.txtDate);
            txtDate.setText(date);
        }

        public void setAmount(String amount) {
            TextView txtAmount = (TextView) mView.findViewById(R.id.txtAmount);
            txtAmount.setText(amount);
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
                Toast.makeText(PaymentsReceivedActivity.this, "Signing out...", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                startActivity(new Intent(PaymentsReceivedActivity.this, MainActivity.class));
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

        FirebaseRecyclerAdapter<Payment, PaymentsReceivedActivity.PaymentViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Payment, PaymentsReceivedActivity.PaymentViewHolder>(
                Payment.class, R.layout.payment_row, PaymentsReceivedActivity.PaymentViewHolder.class, datequery) {
            @Override
            protected void populateViewHolder(PaymentsReceivedActivity.PaymentViewHolder viewHolder, Payment model, int position) {

                //retrieve booking key
                final String payment_id = getRef(position).getKey();

                //add details to recycler view
                viewHolder.setName(model.getName());
                viewHolder.setDate(model.getDate());
                viewHolder.setAmount(model.getAmount());

            }
        };
        mPaymentList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
