package lcukerd.com.stufflist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import java.io.IOException;

public class showList extends AppCompatActivity {

    private LinearLayout linearLayout;
    private View v;
    private CardView cardView;
    private Cursor cursor;
    private String data;
    private DBinteract interact = new DBinteract(this);
    private eventDBcontract dBcontract = new eventDBcontract(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        data = intent.getStringExtra("Event_Name");
    }
    protected void onResume()
    {
        super.onResume();

        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        String order = preferences.getString("pref_item_order",getString(R.string.defaultvai));
        order = updateorder(order);

        setContentView(R.layout.activity_show_list);
        getSupportActionBar().setTitle(data);
        linearLayout = (LinearLayout) findViewById(R.id.linear);

        cursor = interact.readinEvent(data,order);

        while(cursor.moveToNext())                                                                  // To display list
        {
            v =  View.inflate(this,R.layout.temp,null);
            cardView = (CardView) v.findViewById(R.id.cardSample);
            final String ct = cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columntaken));
            final String rt = cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnreturn));
            final String photoURI = cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnFileloc));
            final String name = cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnName));
            final String id = cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnID));

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(),addItem.class);
                    intent.putExtra("eventName",data);
                    intent.putExtra("calledby","list");
                    intent.putExtra("available data",new String[]{ name , ct , rt , photoURI , id });
                    startActivity(intent);
                }
            });
            cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    try {
                        Log.d("Long Click","successful");
                        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                        View layout = inflater.inflate(R.layout.popup,(ViewGroup)findViewById(R.id.pop));
                        PopupWindow pw = new PopupWindow(layout, 400, 200, true);
                        int coord[]= new int[2];
                        v.getLocationOnScreen(coord);
                        pw.showAtLocation(v, Gravity.NO_GRAVITY, 700 ,coord[1]+100);
                        Button del = (Button) layout.findViewById(R.id.del);
                        del.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SQLiteDatabase db = dBcontract.getWritableDatabase();
                                Log.d("delete operaiton",String.valueOf(db.delete(eventDBcontract.ListofItem.tableName,eventDBcontract.ListofItem.columnID+" = "+id,null)));
                                recreate();                                                         //add option to delete files as well
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            });

            final TextView Ename = (TextView) v.findViewById(R.id.textView);
            final CheckBox taken  = (CheckBox) v.findViewById(R.id.taken),returned = (CheckBox) v.findViewById(R.id.returned);
            final ImageView Eimage = (ImageView) v.findViewById(R.id.imageView);

            taken.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ct.equals(String.valueOf(taken.isChecked()))==false)
                    {
                        if (photoURI!=null)
                            interact.save(data,Ename.getText().toString(),taken,returned,Uri.parse(photoURI),"update",id);
                        else
                            interact.save(data,Ename.getText().toString(),taken,returned,null,"update",id);
                    }
                }
            });
            returned.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (rt.equals(String.valueOf(returned.isChecked()))==false)
                    {
                        if (photoURI!=null)
                            interact.save(data,Ename.getText().toString(),taken,returned,Uri.parse(photoURI),"update",id);
                        else
                            interact.save(data,Ename.getText().toString(),taken,returned,null,"update",id);
                    }
                }
            });


            Ename.setText(name);                                                                    //adds data in card

            if (ct.equals(String.valueOf(1)))
                taken.setChecked(true);
            else
                taken.setChecked(false);
            if (rt.equals(String.valueOf(1)))
                returned.setChecked(true);
            else
                returned.setChecked(false);

            if(photoURI!=null) {
                try {

                    Bitmap photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(photoURI));
                    Log.d("Size of image", "width:"+photo.getWidth()+" height:"+photo.getHeight());
                    Eimage.setImageBitmap(photo);      // Image gets cropped look into it
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            linearLayout.addView(v);
        }
    }
    public String updateorder(String order)
    {
        if (order.equals("date_asc_item"))
            order = eventDBcontract.ListofItem.columndatetime+" ASC";
        else if (order.equals("date_desc_item"))
            order = eventDBcontract.ListofItem.columndatetime+" DESC";
        else if (order.equals("name_asc_item"))
            order = eventDBcontract.ListofItem.columnName+" ASC";
        else if (order.equals("name_desc_item"))
            order = eventDBcontract.ListofItem.columnName+" DESC";
        return order;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_add) {
            Intent addItem = new Intent(getApplicationContext(),addItem.class);
            addItem.putExtra("eventName",data);
            addItem.putExtra("calledby","main");
            startActivity(addItem);
            return true;
        }
        else if (id == R.id.action_settings)
        {
            startActivity(new Intent(this,orderitem.class));
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }

}