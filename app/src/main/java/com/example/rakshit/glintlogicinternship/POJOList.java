package com.example.rakshit.glintlogicinternship;

public class POJOList
{
    String name;
    String UID;

    public POJOList(String UID, String name)
    {
        this.name = name;
        this.UID = UID;
    }

    public String getName()
    {
        return name;
    }

    public String getUID()
    {
        return UID;
    }
}
