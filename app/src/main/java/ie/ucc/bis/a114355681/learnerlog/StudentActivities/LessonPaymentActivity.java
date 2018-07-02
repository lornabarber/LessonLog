package ie.ucc.bis.a114355681.learnerlog.StudentActivities;

import android.app.Activity;
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
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import ie.ucc.bis.a114355681.learnerlog.LoginActivities.MainActivity;
import ie.ucc.bis.a114355681.learnerlog.HelperClasses.BottomNavigationViewHelper;
import ie.ucc.bis.a114355681.learnerlog.R;
import ie.ucc.bis.a114355681.learnerlog.PaypalConfig;

public class LessonPaymentActivity extends AppCompatActivity {

    // //To distinguish which activity class has logged the information in logcat
    private static final String TAG = "LessonPaymentActivity";

    //declare screen elements
    Button btnContinue;
    RadioButton rdoOption1, rdoOption2;

    //Declare firebase objects
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private FirebaseUser mCurrentUser;

    //reference to firebase
    DatabaseReference paymentRef, newPayment;

    //will hold the current user id
    private String userID;

    private double lessonPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //removes animation from bottom navigation bar
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView_Bar);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        //highlights the selected menu item
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

        //sets the listener for the bottom nav bar
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    //displays the home activity
                    case R.id.ic_lessons:
                        startActivity(new Intent(LessonPaymentActivity.this, ScheduleActivity.class));
                        break;

                    //displays booking activity
                    case R.id.ic_book:
                        startActivity(new Intent(LessonPaymentActivity.this, BookingActivity.class));
                        break;

                    //current activity
                    case R.id.ic_payments:
                        break;

                    //displays the settings activity
                    case R.id.ic_settings:
                        startActivity(new Intent(LessonPaymentActivity.this, EnterDetails.class));
                        break;
                }
                return true;
            }
        });

        //start the paypal service
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        //initialise screen elements
        btnContinue = (Button) findViewById(R.id.btnContinue);
        rdoOption1 = (RadioButton) findViewById(R.id.rdoOption1);
        rdoOption2 = (RadioButton) findViewById(R.id.rdoOption2);

        //declare the database reference object. This is what we use to access the database.
        //NOTE: Unless you are signed in, this will not be useable.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        paymentRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        newPayment = FirebaseDatabase.getInstance().getReference().child("payments");

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

        //onClickListener for btnContinue
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if option 1 is selected, the price is €325 and the processPayment method is called
                if (rdoOption1.isChecked()){
                    lessonPrice = 325;
                    processPayment();
                //if option 2 is selected, the price is €325 and the processPayment method is called
                } else if (rdoOption2.isChecked()) {
                    lessonPrice = 35;
                    processPayment();
                }
                //no option is selected so method isn't called
                else {
                    Toast.makeText(LessonPaymentActivity.this, "Please select an option", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private int PAYPAL_REQUEST_CODE = 1;
    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(PaypalConfig.PAYPAL_CLIENT_ID);

    protected void processPayment() {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(lessonPrice), "USD", "Lesson Payment",
        PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, com.paypal.android.sdk.payments.PaymentActivity.class);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        startActivityForResult(intent, PAYPAL_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PAYPAL_REQUEST_CODE) {

            if(resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(confirm.toJSONObject().toString());
                        String paymentResponse = jsonObj.getJSONObject("response").getString("state");

                        if(paymentResponse.equals("approved")){ //if payment is successful
                            Toast.makeText(getApplicationContext(), "Payment successful", Toast.LENGTH_LONG).show();
                            paymentRef.child("paid").setValue(true); //show that the user paid in the database
                            btnContinue.setEnabled(false); //disable continue button
                            updateInfo(); //call method to add lessons to database
                            addPayment();//add the payment to the database
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                //unsuccessful payment message
                Toast.makeText(getApplicationContext(), "Payment unsuccessful", Toast.LENGTH_LONG).show();
            }
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
                Toast.makeText(LessonPaymentActivity.this, "Signing out...", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                startActivity(new Intent(LessonPaymentActivity.this, MainActivity.class));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    //stop the PaypalService
    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    //update the users payment info based on the option selected
      protected void updateInfo(){
        paymentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if option 1 is selected the user recieves 12 lessons and this is saved to the database
                if (rdoOption1.isChecked()) {
                    paymentRef.child("lessons").setValue(12);
                    }
                //if option 2 is selected the user recieves 1 lesson and this is saved to the database
                else if (rdoOption2.isChecked()) {
                        paymentRef.child("lessons").setValue(1);
                    }
                }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //this will add a new payment record to the database
    protected  void addPayment() {
          paymentRef.addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                  //retrieves students name and date of payment
                  String name = dataSnapshot.child("name").getValue().toString();

                  //gets todays date
                  Date cDate = new Date();
                  String date = new SimpleDateFormat("dd/MM/yyy").format(cDate);

                  //creates a unique payment key
                  final DatabaseReference addPayment = newPayment.push();

                  //adds the student name, the date and the payment amount
                  addPayment.child("name").setValue(name);
                  addPayment.child("date").setValue(date);
                  addPayment.child("time").setValue(ServerValue.TIMESTAMP);
                  if (rdoOption1.isChecked()) {
                      addPayment.child("amount").setValue("€" + 325.00); //12 lesson option selected
                  } else if (rdoOption2.isChecked()){
                      addPayment.child("amount").setValue("€" + 35.00); //single lesson option selected
                  }
              }

              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
          });


    }
}
