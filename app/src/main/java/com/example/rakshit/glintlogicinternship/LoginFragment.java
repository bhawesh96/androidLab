package com.example.rakshit.glintlogicinternship;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginFragment extends Fragment
{
    View rootView;
    Button login;
    Button register;
    TextView forgot;
    EditText email;
    EditText pass;
    TextView error;

    FirebaseAuth auth;

    public static final String TAG = "LOGIN";

    public LoginFragment()
    {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.login, container, false);
        auth = FirebaseAuth.getInstance();

        email = (EditText) rootView.findViewById(R.id.email);
        pass = (EditText) rootView.findViewById(R.id.pass);
        login = (Button) rootView.findViewById(R.id.login);
        forgot = (TextView) rootView.findViewById(R.id.forgot);
        register = (Button) rootView.findViewById(R.id.register);
        error = (TextView) rootView.findViewById(R.id.error);

        initLogin();
        initRegister();
        initForgot();
        return rootView;
    }

    private void initLogin()
    {
        login.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (pass.getText().toString().isEmpty())
                    error.setText("Enter a valid password");
                else if (email.getText().toString().isEmpty())
                    error.setText("Enter a valid email id");
                else
                {
                    error.setText("");
                    auth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(getActivity(), "Login successful :)", Toast.LENGTH_SHORT).show();
                                        adminStatus(auth, FirebaseDatabase.getInstance().getReference());
                                    }
                                    else
                                    {
                                        Toast.makeText(getActivity(), "Login failed :(", Toast.LENGTH_SHORT).show();
                                        error.setText(task.getException().getMessage());
                                    }
                                }
                            });
                }
            }
        });
    }

    private void initRegister()
    {
        register.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new RegisterFragment(), RegisterFragment.TAG)
                        .addToBackStack(TAG).commit();
            }
        });
    }

    private void initForgot()
    {
        forgot.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (email.getText().toString().isEmpty())
                    error.setText("Enter a valid email id");
                else
                {
                    error.setText("");
                    auth.sendPasswordResetEmail(email.getText().toString().trim()).addOnCompleteListener(
                            new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                        Toast.makeText(getActivity(), "Reset mail sent", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                }
            }
        });
    }

    public void adminStatus(FirebaseAuth auth, DatabaseReference ref)
    {
        ref.child(auth.getCurrentUser().getUid()).child("admin").addListenerForSingleValueEvent(
                new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.getValue().toString().equals("true"))
                            Utils.setAdmin(true);
                        else
                            Utils.setAdmin(false);

                        if (!Utils.isAdmin())
                        {
                            getActivity().finish();
                            startActivity(new Intent(getActivity(), HomeActivity.class));
                        }
                        else
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, new ListFragment()).commit();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                        Log.e("Main", databaseError.getMessage());
                    }
                }
        );
    }
}
