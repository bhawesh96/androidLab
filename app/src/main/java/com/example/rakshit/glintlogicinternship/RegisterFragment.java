package com.example.rakshit.glintlogicinternship;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterFragment extends Fragment
{
    View rootView;
    Button register;
    EditText name;
    EditText email;
    EditText pass;
    TextView error;

    FirebaseAuth auth;
    DatabaseReference reference;

    public static final String TAG = "REGISTER";

    public RegisterFragment()
    {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.register, container, false);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        name = (EditText) rootView.findViewById(R.id.name);
        email = (EditText) rootView.findViewById(R.id.email);
        pass = (EditText) rootView.findViewById(R.id.pass);
        register = (Button) rootView.findViewById(R.id.register);
        error = (TextView) rootView.findViewById(R.id.error);

        initRegister();
        return rootView;
    }

    private void initRegister()
    {
        register.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (pass.getText().toString().length()<6)
                    error.setText("Password has to be atleast 6 characters");
                else if (email.getText().toString().isEmpty())
                    error.setText("Enter a valid email id");
                else
                {
                    error.setText("");
                    auth.createUserWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        writeData();
                                    }
                                    else
                                    {
                                        Toast.makeText(getActivity(), "Registration failed :(", Toast.LENGTH_SHORT).show();
                                        error.setText(task.getException().getMessage());
                                    }
                                }
                            });
                }
            }
        });
    }

    private void writeData()
    {
        reference.child(auth.getCurrentUser().getUid())
                .child("name").setValue(name.getText().toString().trim()).addOnCompleteListener(
                new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            reference.child(auth.getCurrentUser().getUid())
                                    .child("admin").setValue("hmi2HzQcYEZGwZ5IbEzkqdUWMeZ2").addOnCompleteListener(
                                    new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                reference.child("hmi2HzQcYEZGwZ5IbEzkqdUWMeZ2").child("agents")
                                                        .child(auth.getCurrentUser().getUid()).setValue(name.getText().toString().trim())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    Toast.makeText(getActivity(), "Registration successful :)", Toast.LENGTH_SHORT).show();
                                                                    Utils.setAdmin(false);
                                                                    startActivity(new Intent(getActivity(), HomeActivity.class));
                                                                }
                                                                else
                                                                {
                                                                    Toast.makeText(getActivity(), "Registration failed :(", Toast.LENGTH_SHORT).show();
                                                                    error.setText(task.getException().getMessage());
                                                                }
                                                            }
                                                        });

                                            }
                                            else
                                            {

                                            }
                                        }
                                    }
                            );
                        }
                        else
                        {
                            Toast.makeText(getActivity(), "Registration failed :(", Toast.LENGTH_SHORT).show();
                            error.setText(task.getException().getMessage());
                        }
                    }
                }
        );
    }
}
