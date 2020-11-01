package com.example.myabc;

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
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {
    EditText email,password;
    Button login;
    TextView signup,skips;
    FirebaseAuth auth;
    ImageView open,close;
    String TAG="LOGIN-->";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signup=findViewById(R.id.signup_text);
       // skips=findViewById(R.id.skip_it);



//        skips.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i=new Intent(LoginActivity.this,HomePageActitvity.class);
//                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(i);
//                finish() ;
//            }
//        });


        auth=FirebaseAuth.getInstance();
        if(auth.getCurrentUser()!=null)
        {
            Intent i=new Intent(LoginActivity.this,MainActivity.class);
            startActivity(i);
        }

        email=findViewById(R.id.login_email);
        password=findViewById(R.id.login_password);
        login=findViewById(R.id.login_button);


        open=findViewById(R.id.open_eye);
        close=findViewById(R.id.close_eye);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(LoginActivity.this,SignupActivity.class);
                startActivity(i);
                finish();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //HIDE PASSWORD
                password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                open.setVisibility(View.VISIBLE);
                close.setVisibility(View.INVISIBLE);
            }
        });
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show Password
                password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                close.setVisibility(View.VISIBLE);
                open.setVisibility(View.INVISIBLE);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String em=email.getText().toString().trim();
                String ps=password.getText().toString().trim();

                if(TextUtils.isEmpty(em))
                {
                    email.requestFocus();
                    // error.setText("Please Enter email id");
                    email.setError("Please Enter Email ID");
                    email.requestFocus();
                    Toast.makeText(LoginActivity.this, "Please Enter email id", Toast.LENGTH_SHORT).show();
                }
                else  if(TextUtils.isEmpty(ps))
                {
                    password.requestFocus();
                    password.setError("Please Enter Password");
                    //  password.setError("Please Enter password");
                    Toast.makeText(LoginActivity.this, "Please Enter password", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Login(em,ps);
                }
            }
        });




    }

    private void Login(final String em, String ps) {

        auth.signInWithEmailAndPassword(em,ps).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Intent i=new Intent(LoginActivity.this,MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                    Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                }
                if(!task.isSuccessful()) {

                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        email.setError("Invalid User. No Account Exist with this email");
                        email.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        Log.d(TAG , "email :" + email);
                        password.setError("Invalid Password");
                        password.requestFocus();
                    } catch (FirebaseNetworkException e) {
                        Toast.makeText(LoginActivity.this, "error_message_failed_sign_in_no_network", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }

                }
            }
        });
    }
}