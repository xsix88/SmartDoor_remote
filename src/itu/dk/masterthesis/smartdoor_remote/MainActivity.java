package itu.dk.masterthesis.smartdoor_remote;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public static final String EXTRA_SEARCHTEXT = "extraSearch";
	public static final int GET_PICTURE = 13;
	
	private EditText statusTxt;
	private Button statusButton;
	private Button pictureButton;
	private EditText pictureTxt;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		statusTxt = (EditText) findViewById(R.id.statusTxt);
		pictureTxt = (EditText) findViewById(R.id.pictureTxt);
		statusButton = (Button) findViewById(R.id.statusB);
		pictureButton = (Button) findViewById(R.id.pictureB);
		
		statusButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (statusTxt.getText().length() > 0) {					
					//updateStatus();
					statusTxt.setText("");
				} else {
					displayToast("Enter new status.");
				}

			}
		});
		
		pictureButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (pictureTxt.getText().length() > 0) {					
					updatePicture();
					statusTxt.setText("");
				} else {
					displayToast("Enter text to search for a picture.");
				}

			}

			
		});		
	}
	
	private void updatePicture() {
		// create an intent to start the ViewThumbnailsActivity
    	Intent startThumbnailsActIntent = new Intent(this, ViewThumbnailsActivity.class);
    	// put in the intent the search string too
    	startThumbnailsActIntent.putExtra(EXTRA_SEARCHTEXT, pictureTxt.getText().toString());		    	
    	// fire the intent
    	startActivityForResult(startThumbnailsActIntent, GET_PICTURE);
	}	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
	    if (requestCode == GET_PICTURE) {
	        // Make sure the request was successful
	        if (resultCode == RESULT_OK) {
	        	//data
	            // The user picked a contact.
	            // The Intent's data Uri identifies which contact was selected.

	            // Do something with the contact here (bigger example below)
	        }
	    }
	}
	
	
	private void displayToast(String txt) {
		Toast.makeText(this, txt, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
