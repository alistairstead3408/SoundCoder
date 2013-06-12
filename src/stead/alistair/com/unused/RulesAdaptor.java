package stead.alistair.com.unused;


import java.util.ArrayList;

import stead.alistair.com.soundcoder.R;



import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;



public class RulesAdaptor extends ListActivity {


	
	private String TAG = "RulesActivity";
	
	ColorItemAdapter m_adapter;
	private long[] blobData = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rulesactivity);
        
        blobData = this.getIntent().getBundleExtra("blobData").getLongArray("bundleBlobData");
        Log.e(TAG, "" + blobData[0]);
        
        int[] blobs = new int [(blobData.length -2) / 2];
        String[] items = new String[((blobData.length -2) / 2) + 1];
        items[0] = "New Rule Set...";
        for(int i = 0; i < blobs.length; i++){
        	items[i+1] = "blob " + i;
        }
        
        
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items));
        ArrayList<ColorItem> colorItems = new ArrayList<ColorItem>();
        for(int i = 2; i < blobData.length; i+=2){
        	colorItems.add(new ColorItem((int)blobData[i], (int)blobData[i+1], 50));
        }
        
        this.m_adapter = new ColorItemAdapter(this, R.layout.row, colorItems);
        setListAdapter(this.m_adapter);

    }
    
    private class ColorItemAdapter extends ArrayAdapter<ColorItem> {

        private ArrayList<ColorItem> colorItems;

        public ColorItemAdapter(Context context, int textViewResourceId, ArrayList<ColorItem> items) {
                super(context, textViewResourceId, items);
                this.colorItems = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.row, null);
                }
                if (colorItems != null) {
                        TextView tt = (TextView) v.findViewById(R.id.toptext);
                        ImageView iv = (ImageView) v.findViewById(R.id.icon);
                        iv.setBackgroundColor(colorItems.get(0).getHSV());
                        if (tt != null) {
                              tt.setText("HSV: "+colorItems.toString());                          
                        }
                }
                return v;
        }
}
    
    
    
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.rulesmenu, menu);

		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().equals(this.getString(R.string.menu1))) {
			final AlertDialog.Builder alert = new AlertDialog.Builder(this);
			final EditText input = new EditText(this);
			alert.setView(input);
			alert.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int whichButton) {
					//list.add(value);
					
				}
			});

			alert.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.cancel();
				}
			});
			alert.show();

			return true;
		}
		else
		{
			Log.e(TAG, (String) item.getTitle());
			return false;
		}
		
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// TODO Auto-generated method stub
		super.onOptionsMenuClosed(menu);
	}
    
    
    
	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

    
}


