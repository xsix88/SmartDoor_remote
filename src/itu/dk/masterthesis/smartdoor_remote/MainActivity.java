package itu.dk.masterthesis.smartdoor_remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String EXTRA_SEARCHTEXT = "extraSearch";
	public static final int GET_PICTURE = 13;
	public static final String STATUS = "status";
	public static final String PICTURE = "picture";

	private EditText statusTxt;
	private Button statusButton;
	private Button pictureButton;
	private EditText pictureTxt;
	private ImageView pictureView;

	public static Bitmap bitmap = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		statusTxt = (EditText) findViewById(R.id.statusTxt);
		pictureTxt = (EditText) findViewById(R.id.pictureTxt);
		statusButton = (Button) findViewById(R.id.statusB);
		pictureButton = (Button) findViewById(R.id.pictureB);
		pictureView = (ImageView) findViewById(R.id.pictureView);
		pictureView.setDrawingCacheEnabled(true);

		statusButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (statusTxt.getText().length() > 0) {
					UpdateStatus us = new UpdateStatus();
					us.start();
					statusTxt.setText("");
					pictureTxt.setText("");
					pictureView.setImageBitmap(null);
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

	class UpdateStatus extends Thread {
		public void run() {
			// TODO Auto-generated method stub
			int serverPort = 7896;
			String status = statusTxt.getText().toString();
			Socket socket;
			try {
				InetAddress serverAddress = InetAddress.getByName("localhost");
				socket = new Socket(serverAddress, serverPort);
				OutputStream os = socket.getOutputStream();
				// could have gotten an InputStream as well
				DataOutputStream dos = new DataOutputStream(os);
				dos.writeUTF(status);
				if (bitmap != null) {
					Picture p = new Picture(bitmap);
					byte[] picture = p.getByteArray();
					dos.write(picture);
				}
				dos.flush();
				InputStream is = socket.getInputStream();
				DataInputStream dis = new DataInputStream(is);
				displayToast(dis.readUTF());
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
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
