package com.lcukerd.stufflist.database;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CheckBox;

import com.lcukerd.stufflist.startonBoot;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Programmer on 13-03-2017.
 */

public class DBinteract {

    private eventDBcontract dBcontract ;
    private String[] projection = {
            eventDBcontract.ListofItem.columnID,
            eventDBcontract.ListofItem.columnEvent,
            eventDBcontract.ListofItem.columnName,
            eventDBcontract.ListofItem.columntaken,
            eventDBcontract.ListofItem.columnreturn,
            eventDBcontract.ListofItem.columnFileloc,
            eventDBcontract.ListofItem.columndatetime,
            eventDBcontract.ListofItem.columnnotes
    };
    private Context contextp;

    public DBinteract(Context context)
    {
        contextp=context;
        dBcontract = new eventDBcontract(context);
    }

    public ArrayList<String> readfromDB(String order)
    {
        SQLiteDatabase db = dBcontract.getReadableDatabase();
        int n=0;

        Cursor cursor = db.query(eventDBcontract.ListofItem.tableName,projection,null,null,null,null,order);

        while(cursor.moveToNext())
        {
            Log.d("column return",cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnID))+" "+
                    cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnEvent))+" "+
                    cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnName))+" "+
                    cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columntaken))+" "+
                    cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnreturn))+" "+
                    cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnFileloc))+" "+
                    cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columndatetime))+" "+
                    cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnnotes)));
            if (cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnreturn))!=null)
                if (Long.parseLong(cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnreturn)))>System.currentTimeMillis())
                    n=1;
        }
        if (n==0)
        {
            Log.d("Interact","No alarm");
            ComponentName receiver = new ComponentName(contextp, startonBoot.class);
            PackageManager pm = contextp.getPackageManager();
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
        else
        {
            Log.d("Interact","Few alarm");
            ComponentName receiver = new ComponentName(contextp, startonBoot.class);
            PackageManager pm = contextp.getPackageManager();

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }


        cursor = db.query(eventDBcontract.ListofItem.tableName,projection,null,null,eventDBcontract.ListofItem.columnEvent,null,order);

        int i=0;
        ArrayList<String> NameofEvents = new ArrayList<>();
        while(cursor.moveToNext())
        {
            NameofEvents.add(cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnEvent)));
            i++;
        }
        return (NameofEvents);
    }

    public Cursor readinEvent(String event,String order)
    {
        SQLiteDatabase db = dBcontract.getReadableDatabase();
        Cursor cursor = db.query(eventDBcontract.ListofItem.tableName,projection,eventDBcontract.ListofItem.columnEvent+" = '"+event+"'",null,null,null,order);
        return cursor;
    }
    public String readNote(String id)
    {
        SQLiteDatabase db = dBcontract.getReadableDatabase();
        Cursor cursor = db.query(eventDBcontract.ListofItem.tableName,projection,eventDBcontract.ListofItem.columnID+" = "+id,null,null,null,null);
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnnotes));

    }

    public int readstatus(String eventName)
    {
        SQLiteDatabase db = dBcontract.getReadableDatabase();
        Cursor cursor = db.query(eventDBcontract.ListofItem.tableName,projection,eventDBcontract.ListofItem.columnEvent+" = '"+eventName+"'",null,null,null,eventDBcontract.ListofItem.columnName+" ASC");
        while(cursor.moveToNext())
        {
            String data = cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnName));
            if (data.length()>=2)
                if ((data.charAt(0)=='#')&&(data.charAt(1)=='%'))
                {
                    if (System.currentTimeMillis()<Long.parseLong(cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columntaken))))
                        return 0;
                    else if (System.currentTimeMillis()<Long.parseLong(cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnreturn))))
                        return 1;
                    else
                        return 0;
                }
        }
        return 0;
    }

    public long save(String eventName, String itemName, MenuItem taken, MenuItem returned, Uri photoURI , String caller , String id)
    {
        SQLiteDatabase db = dBcontract.getWritableDatabase();
        ContentValues values = new ContentValues();
        long specialid;

        if (Character.isAlphabetic(eventName.charAt(0)))
        {
            eventName = Character.toUpperCase(eventName.charAt(0)) + eventName.substring(1);
        }
        values.put(eventDBcontract.ListofItem.columnEvent,eventName);
        values.put(eventDBcontract.ListofItem.columnName,itemName);
        values.put(eventDBcontract.ListofItem.columndatetime,getmillis());
        if (taken != null) {
            if (taken.isChecked())
                values.put(eventDBcontract.ListofItem.columntaken, "1");
            else
                values.put(eventDBcontract.ListofItem.columntaken, "0");
            if (returned.isChecked())
                values.put(eventDBcontract.ListofItem.columnreturn, "1");
            else
                values.put(eventDBcontract.ListofItem.columnreturn, "0");
        }
        else
        {
            values.put(eventDBcontract.ListofItem.columntaken, "0");
            values.put(eventDBcontract.ListofItem.columnreturn, "0");
        }
        if (photoURI!=null) {
            values.put(eventDBcontract.ListofItem.columnFileloc, photoURI.toString());
            Log.d("File address write", photoURI.toString());
        }
        else
            values.putNull(eventDBcontract.ListofItem.columnFileloc);
        if (caller.equals("main"))
        {
            specialid = db.insert(eventDBcontract.ListofItem.tableName,null,values);
            Log.d("save operation","complete");
            return specialid;
        }
        else
        {
            db.update(eventDBcontract.ListofItem.tableName,values,"id=?",new String[]{id});
            Log.d("update operation","complete");
            return 0;
        }
    }

    public void saveEvent(String eventName , String itemName , long start , long end , String id)
    {
        SQLiteDatabase db = dBcontract.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(eventDBcontract.ListofItem.columnEvent,eventName);
        values.put(eventDBcontract.ListofItem.columnName,"#%"+itemName);
        if (start!=-1)
            values.put(eventDBcontract.ListofItem.columntaken,start);
        if (end!=-1)
            values.put(eventDBcontract.ListofItem.columnreturn,end);

        db.update(eventDBcontract.ListofItem.tableName,values,"id = ?",new String[]{id});
        Log.d("save operation","Event detail complete");
    }
    public void savenote(String id , String note)
    {
        ContentValues values = new ContentValues();
        SQLiteDatabase db = dBcontract.getWritableDatabase();

        values.put(eventDBcontract.ListofItem.columnnotes,note);
        Log.d("Interact",String.valueOf(db.update(eventDBcontract.ListofItem.tableName,values,"id=?",new String[]{id})));
        Log.d("Interact","add note complete " + note);

    }
    private long getmillis()
    {
        Calendar c = Calendar.getInstance();
        return c.getTimeInMillis();
    }

}
