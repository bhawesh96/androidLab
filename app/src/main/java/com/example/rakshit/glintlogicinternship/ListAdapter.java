package com.example.rakshit.glintlogicinternship;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends ArrayAdapter<POJOList>
{
    ArrayList<POJOList> agents = new ArrayList<>();
    Context _context;

    public ListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<POJOList> objects)
    {
        super(context, 0, objects);
        this.agents = objects;
        this._context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View v = LayoutInflater.from(_context).inflate(android.R.layout.simple_list_item_1, parent, false);
        ((TextView)v.findViewById(android.R.id.text1)).setText(agents.get(position).getName());
        return v;
    }

    @Nullable
    @Override
    public POJOList getItem(int position)
    {
        return agents.get(position);
    }

    @Override
    public int getCount()
    {
        return this.agents.size();
    }
}
