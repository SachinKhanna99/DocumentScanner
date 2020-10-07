package com.example.documentscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {
TextView login,error;
Button signup;
EditText signup_name,signup_password,signup_email,phonenumber;

FirebaseAuth auth;
ImageView open,close;
String TAG="Singup-->";

DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        login=findViewById(R.id.login_text);


        auth=FirebaseAuth.getInstance();
        reference= FirebaseDatabase.getInstance().getReference();
        

        signup=findViewById(R.id.signup_button);
        signup_name=findViewById(R.id.name);
        signup_password=findViewById(R.id.signup_password);
        signup_email=findViewById(R.id.signup_email);
        phonenumber=findViewById(R.id.signup_phone);

        open=findViewById(R.id.open_eye);
        close=findViewById(R.id.close_eye);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(SignupActivity.this,LoginActitvity.class);
                startActivity(i);

            }
        });

       close.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               //HIDE PASSWORD
               signup_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

               open.setVisibility(View.VISIBLE);
               close.setVisibility(View.INVISIBLE);
           }
       });
open.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        //Show Password
        signup_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        close.setVisibility(View.VISIBLE);
        open.setVisibility(View.INVISIBLE);
    }
});




        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             String n=signup_name.getText().toString().trim();
             String em=signup_email.getText().toString().trim();
             String ps=signup_password.getText().toString().trim();

             String ph=phonenumber.getText().toString();

             if(TextUtils.isEmpty(n))
             {
                 signup_name.requestFocus();
                 Toast.makeText(SignupActivity.this, "Please Enter Your Name", Toast.LENGTH_SHORT).show();
             }
             else if(n.length()<=2)
             {
                 signup_name.requestFocus();
                 Toast.makeText(SignupActivity.this, "Please enter name with more than 2 character ", Toast.LENGTH_SHORT).show();
             }
               else if(TextUtils.isEmpty(em))
                {
                   signup_email.requestFocus();
                    Toast.makeText(SignupActivity.this, "Please Enter Your Email Id", Toast.LENGTH_SHORT).show();
                }
             else if(TextUtils.isEmpty(ps))
             {
                 signup_password.requestFocus();

                 Toast.makeText(SignupActivity.this, "Please Enter Your Email Id", Toast.LENGTH_SHORT).show();
             }
             else{
                 Signup(n,em,ps,ph);
             }
            }
        });
    }

    private void Signup(final String n, final String em, final String ps, final String ph) {

       auth.createUserWithEmailAndPassword(em,ps).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
           @Override
           public void onComplete(@NonNull Task<AuthResult> task) {
               if(task.isSuccessful())
               {

                   SaveData(n,em,ph);

               }

               else if(!task.isSuccessful())
               {
                   try {
                       throw task.getException();
                   } catch(FirebaseAuthWeakPasswordException e) {
                       signup_password.setError("Weak Password");
                       signup_password.requestFocus();
                   } catch(FirebaseAuthInvalidCredentialsException e) {
                       signup_email.setError("Invalid Email ID");
                       signup_email.requestFocus();
                   } catch(FirebaseAuthUserCollisionException e) {
                       signup_email.setError("User Already Exist ");
                       signup_email.requestFocus();
                   } catch(Exception e) {
                       Log.e(TAG, e.getMessage());
                   }
               }
           }
       }) ;
    }

    private void SaveData(String n, String em,String ph) {
        boolean premium = false;
        HashMap<String,Object> map=new HashMap<>();
        
        map.put("name",n);
        map.put("email",em);
        map.put("phone",ph);
        map.put("premium",premium);
        
        reference.child("Users").push().updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Intent i=new Intent(SignupActivity.this,MainActivity.class);
                   i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                   startActivity(i);
                   finish();
                   Toast.makeText(SignupActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show(); 
                }
                else 
                {
                    Toast.makeText(SignupActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
    }
}
