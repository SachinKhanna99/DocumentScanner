package com.example.myabc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myabc.model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class UserDetail extends AppCompatActivity {
TextView name,status,email;
String TAG="User Details";
DatabaseReference reference;
Button payment,signout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        name=findViewById(R.id.user_name);
        email=findViewById(R.id.user_email);
        status=findViewById(R.id.user_status);
        signout=findViewById(R.id.signout);
        String user=FirebaseAuth.getInstance().getCurrentUser().getUid();
        reference= FirebaseDatabase.getInstance().getReference().child("Users").child(user);
        Users users;
        payment=findViewById(R.id.payment);
        Log.e(TAG, "onCreate: "+reference );

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();

                Intent i=new Intent(UserDetail.this,HomePageActitvity.class);
                startActivity(i);
            }
        });

        email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
       // name.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        Log.e(TAG, "onCreate: "+user );

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reference.keepSynced(true);
                for(DataSnapshot snapshot1:snapshot.getChildren())
                {
                    Users users1=new Users(snapshot1.child("name").getValue(String.class),snapshot1.child("email").getValue(String.class),snapshot1.child("premium").getValue(String.class));
                    Log.e(TAG, "onDataChange: "+users1.getName() );
                    Log.e(TAG, "onDataChange: "+users1.getPremium() );
                    name.setText(users1.getName());

                    String p=users1.getPremium();
                    if(p.equals("false"))
                    {
                        status.setText("You are not a premium user");
                        payment.setVisibility(View.VISIBLE);
                        payment.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent=new Intent(UserDetail.this,PaymentActivity.class);
                                intent.putExtra(user,"uid");
                                startActivity(intent);
                            }
                        });
                    }
                    else if(p.equals("true"))
                    {
                        status.setText("You are a Premium User  ");
                        status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.crown, 0);
                        payment.setVisibility(View.INVISIBLE);

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent(UserDetail.this,HomePageActitvity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

//   if(Objects.equals(users1.getPremium(), "false"))
////                    {
////                        status.setText("Buy premium");
////                        payment.setVisibility(View.VISIBLE);
////                    }
////                    else {
////                        status.setText("Premium User");
////                        status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.camera, 0, 0, 0);
////                        payment.setVisibility(View.INVISIBLE);
////                    }