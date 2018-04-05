package com.example.rakshit.glintlogicinternship;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListFragment extends Fragment
{
    View rootView;
    ListView list;
    ListAdapter adapter = null;
    FirebaseAuth auth;
    DatabaseReference reference;
    ArrayList<POJOList> agents = new ArrayList<>();

    public ListFragment()
    {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.list, container, false);
        list = (ListView) rootView.findViewById(R.id.list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent i = new Intent(getActivity(), MapsActivity.class);
                i.putExtra("uid", agents.get(position).getUID());
                startActivity(i);
            }
        });
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        reference.child(auth.getCurrentUser().getUid()).child("agents").addValueEventListener(
                new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        agents.clear();
                        for (DataSnapshot snap:dataSnapshot.getChildren())
                        {
                            agents.add(new POJOList(snap.getKey(), snap.getValue().toString()));
                        }
                        dataChanged(agents);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                        Log.e("Main", "Couldn't fetch agents. Error: " + databaseError.getMessage());
                    }
                }
        );

        return rootView;
    }

    private void dataChanged(ArrayList<POJOList> l)
    {
        if (adapter==null)
        {
            adapter = new ListAdapter(getContext(), 0, l);
            list.setAdapter(adapter);
        }
        else
        {
            adapter.clear();
            adapter.addAll(l);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_maps_agent, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId()==R.id.menu_logout)
        {
            auth.signOut();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new LoginFragment()).commit();
        }
        return true;
    }
}
