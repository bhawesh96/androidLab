package com.example.rakshit.glintlogicinternship;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity
{
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        if (auth.getCurrentUser()==null)
            getSupportFragmentManager().beginTransaction().add(R.id.container, new LoginFragment()).commit();
        else
        {
            progressBar.setVisibility(View.VISIBLE);
            adminStatus(auth, ref);
        }
    }

    public void adminStatus(FirebaseAuth auth, DatabaseReference ref)
    {
        ref.child(auth.getCurrentUser().getUid()).child("admin").addListenerForSingleValueEvent(
                new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        progressBar.setVisibility(View.GONE);
                        if (dataSnapshot.getValue().toString().equals("true"))
                            Utils.setAdmin(true);
                        else
                            Utils.setAdmin(false);
                        if (!Utils.isAdmin())
                        {
                            finish();
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                        else
                            getSupportFragmentManager().beginTransaction().replace(R.id.container, new ListFragment()).commit();
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
