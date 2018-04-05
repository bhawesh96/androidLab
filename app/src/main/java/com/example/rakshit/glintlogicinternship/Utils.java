package com.example.rakshit.glintlogicinternship;

public class Utils
{
    public static boolean admin = false;
    public static boolean returning = false;

    public static boolean isAdmin()
    {
        return admin;
    }

    public static void setAdmin(boolean b)
    {
        admin = b;
    }

    public static boolean isReturning()
    {
        return returning;
    }

    public static void setReturning(boolean b)
    {
        returning = b;
    }
}
