package itu.dk.masterthesis.smartdoor_remote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
	static final int DIALOG_INFINITE_PROGRESS = 0;
	public static final String EXTRA_SEARCHTEXT = "extraSearch";
	public static final int GET_PICTURE = 13;
	public static final String STATUS = "status";
	public static final String PICTURE = "picture";
	public static final String SELECTED_STATUS = "status";
	private EditText statusTxt;
	private Button statusButton;
	private Button pictureButton;
	private Button selectB;
	private EditText pictureTxt;
	private ImageView pictureView;
	public static Bitmap bitmap = null;
	public Context context;
	DBadapter adapter;
	Cursor status;
	BitmapFactory.Options options = new BitmapFactory.Options();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		statusTxt = (EditText) findViewById(R.id.statusTxt);
		pictureTxt = (EditText) findViewById(R.id.pictureTxt);
		statusButton = (Button) findViewById(R.id.statusB);
		selectB = (Button) findViewById(R.id.selectB);
		pictureButton = (Button) findViewById(R.id.pictureB);
		pictureView = (ImageView) findViewById(R.id.pictureView);
		pictureView.setDrawingCacheEnabled(true);
		
		adapter = new DBadapter(this);
		adapter.open();
		
		adapter.syncDefaultsFromServer();
		
		context = this;
		selectB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final CharSequence[] items = new CharSequence[adapter.getNumberOfStatics()];
				status = adapter.getStatic(adapter.getNumberOfStatics()+"");
				if(status.getCount() > 0) {
					int i = 0;
					if (status.moveToFirst()){
						do {
						   String text = status.getString(status.getColumnIndex("status"));
						   items[i] = text;
						   i++;
						   }
						while(status.moveToNext());
					}
					status.close();
				}
				//final CharSequence[] items = {"Status 1", "Status 2","Status 3", "Status 4", "Status 5"};
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("Select Status");
				builder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						statusTxt.setText(items[item]);
						byte[] pic = adapter.getStaticPic(items[item]+"");
						bitmap = BitmapFactory.decodeByteArray(pic, 0, pic.length, options);
						Picture p = new Picture(bitmap);
						pictureView.setImageBitmap(p.getPicture());
						}
				});
				builder.show();
				System.gc();
			}
		});
		statusButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (statusTxt.getText().length() > 0) {
					if (bitmap != null) {
						UpdateStatusAT us = new UpdateStatusAT();
						us.execute();
					} else {
						displayToast("Include a picture.");
					}
				} else {
					displayToast("Enter new status.");
				}
			}
		});
		pictureButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (pictureTxt.getText().length() > 0) {
					ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
					NetworkInfo mWifi = connManager
							.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
					NetworkInfo mData = connManager
							.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
					if (mWifi.isConnected() || mData.isConnected()) {
						updatePicture();
					} else {
						displayToast("You need internet acces to be turned on.");
					}
				} else {
					displayToast("Enter text to search for a picture.");
				}
			}
		});
	}
	
	class UpdateStatusAT extends AsyncTask<Void, Integer, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				Picture p = new Picture(bitmap);
				adapter.saveStatus(p.getByteArray(), statusTxt.getText().toString());
			} finally {
				// dismiss the dialog
				dismissDialog(DIALOG_INFINITE_PROGRESS);
			}
			return null;
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void onPreExecute() {
			// while search, show a dialog with infinite progress
			showDialog(DIALOG_INFINITE_PROGRESS);
			// disable load more button
		}

		@Override
		protected void onPostExecute(Void result) {
			statusTxt.setText("");
			pictureTxt.setText("");
			pictureView.setImageBitmap(null);
			bitmap = null;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (DIALOG_INFINITE_PROGRESS == id) {
			return ProgressDialog.show(this, "",
					"Updating status, please wait...", true);
		}
		return super.onCreateDialog(id);
	}

	private void updatePicture() {
		// create an intent to start the ViewThumbnailsActivity
		Intent startThumbnailsActIntent = new Intent(this,
				ViewThumbnailsActivity.class);
		// put in the intent the search string too
		startThumbnailsActIntent.putExtra(EXTRA_SEARCHTEXT, pictureTxt
				.getText().toString());
		// fire the intent
		startActivityForResult(startThumbnailsActIntent, GET_PICTURE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == GET_PICTURE) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				byte[] byteArray = data.getByteArrayExtra("selectedPicture");
				Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0,
						byteArray.length);
				bitmap = bmp;
				pictureView.setImageBitmap(bmp);
			}
		}
	}

	private void displayToast(String txt) {
		Toast.makeText(this, txt, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(STATUS, statusTxt.getText().toString());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		statusTxt.setText(savedInstanceState.getString(STATUS));
		if (bitmap != null) {
			pictureView.setImageBitmap(bitmap);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}