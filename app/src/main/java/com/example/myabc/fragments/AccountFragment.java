package com.example.myabc.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.monscanner.ShapeValidationFragment;
import com.example.myabc.LoginActivity;
import com.example.myabc.MainActivity;
import com.example.myabc.R;
import com.example.myabc.SignupActivity;
import com.example.myabc.UserDetail;
import com.google.android.gms.vision.text.Line;
import com.google.firebase.auth.FirebaseAuth;


public class AccountFragment extends Fragment {
    EditText email,password;
    Button login;
    TextView signup,skips;
    FirebaseAuth auth;
    ImageView open,close;
    String TAG="LOGIN-->";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        auth=FirebaseAuth.getInstance();

        View root=inflater.inflate(R.layout.fragment_test,container,false);
        final Button login=root.findViewById(R.id.login_button);
        email=root.findViewById(R.id.login_email);


        password=root.findViewById(R.id.login_password);
        signup=root.findViewById(R.id.signup_text);



        open=root.findViewById(R.id.open_eye);
        close=root.findViewById(R.id.close_eye);
        if(auth.getCurrentUser()!=null)
        {
            Intent intent=new Intent(getActivity(), UserDetail.class);
            startActivity(intent);
            Toast.makeText(getContext(), "Already Login", Toast.LENGTH_SHORT).show();

        }


        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getActivity(),SignupActivity.class);
                startActivity(i);


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

        return root;
    }
}