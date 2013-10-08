package itu.dk.masterthesis.smartdoor_remote;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

public class DBadapter {
	DBhelper helper;
	static SQLiteDatabase db;
	static Context context;
	static int count;
	static String status;
	static byte[] picture;
	static long datetime;
	
	public DBadapter(Context context) {
		helper = new DBhelper(context);
		DBadapter.context = context;
	}
	
	public void open() {
		db = helper.getWritableDatabase();
	}
	
	public void close() {
		db.close();
	}
	
	public void syncDefaultsFromServer() {
		int cmd = 1;
		new AsyncRest(context, cmd).execute("http://jsnas.dyndns.org/SmartDoorRestAPI/defaults/index.php?owner_id=1");
	}
	
	public String checkForNewDefaults() {
		int cmd = 7;
		AsyncTask<String, Void, String> results = new AsyncRest(context, cmd).execute("http://jsnas.dyndns.org/SmartDoorRestAPI/defaults/index.php?owner_id=1&new=1");
		try {
			return results.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void saveStaticNoRest(byte[] picture, String status) {
		ContentValues values = new ContentValues();
		values.put("pic", picture);
		values.put("status", status);
        db.insert("statics", null, values);
	}
	
	public Cursor getStatic(String amount) {
		return db.rawQuery("SELECT * FROM statics ORDER BY _id DESC LIMIT ?", new String[] {amount});
	}
	
	public int getNumberOfStatics() {
		Cursor nCount = db.rawQuery("SELECT count(*) FROM statics", new String[]{});
		nCount.moveToFirst();
		int count = nCount.getInt(0);
		nCount.close();
		return count;
	}
	
	public void saveStatus(byte[] picture, String status) {
		Date myDate = new Date();
	    long timeMilliseconds = myDate.getTime();
        count = (int)(Math.random()*100000);
		DBadapter.picture = picture;
		DBadapter.status = status;
		DBadapter.datetime = timeMilliseconds;
        int cmd = 2;
		new AsyncRest(context, cmd).execute("http://jsnas.dyndns.org/SmartDoorRestAPI/status/index.php");
	}
	
	public byte[] getStaticPic(String status) {
		Cursor nCount = db.rawQuery("SELECT pic FROM statics WHERE status=?", new String[]{status});
		nCount.moveToFirst();
		byte[] pic = nCount.getBlob(0);
		nCount.close();
		return pic;
	}
	public static void clearStatics() {
		db.execSQL("DELETE FROM statics");
	}
}

class AsyncRest extends AsyncTask<String, Void, String> {
	Context mContext;
	int cmd;
	byte[] byteArray;
	String status;
	private Exception exception;
	AsyncRest(Context context, int cmd) {
		this.mContext = context;
		this.cmd = cmd;
	}
    
    @Override
    protected String doInBackground(String... urls) {
    	try {
            URL url= new URL(urls[0]);
            RestHandler rest = new RestHandler();
            switch(cmd) {
            	case 1:
            		/*if(idnumber.length() > 0) {
	            		if(Integer.parseInt(idnumber.getText().toString()) > 0) {
	            			JSONArray json = rest.doGet(url+"?id="+idnumber.getText());
	            			JSONObject c = json.getJSONObject(0);
	            			Log.i("Smartdoor json", c.getString("status"));
	            			byteArray = Base64.decode(c.getString("picture"), 0);
	            			return "ID: " + c.getString("id") +" OwnerID: "+ c.getString("owner_id") +" Status: "+ c.getString("status");
	            		}
            		} else {*/
            		//URL url = new URL("http://jsnas.dyndns.org/SmartDoorRestAPI/defaults/index.php?owner_id=1");
        			JSONArray json = rest.doGet(url+"");
        			DBadapter.clearStatics();
        			for(int i = 0; i < json.length(); i++){
        		        JSONObject c = json.getJSONObject(i);
        		        byteArray = Base64.decode(c.getString("picture"), 0);
        		        status = c.getString("status");
        		        DBadapter.saveStaticNoRest(byteArray, status);
            		}
        			break;
            	case 2:
            		JSONObject json1 = new JSONObject();
            		json1.put("id", DBadapter.count);
            		json1.put("owner_id", 1);
            		json1.put("status", DBadapter.status);
            		json1.put("picture", Base64.encodeToString(DBadapter.picture, Base64.DEFAULT));
            		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            		String dateString = fmt.format(DBadapter.datetime);
            		json1.put("datetime", dateString);
            		return rest.doPost(url+"", json1).toString();
            	case 7:
            		JSONArray json3 = rest.doGet(url+"");
        			JSONObject c = json3.getJSONObject(0);
        			//byteArray = Base64.decode(c.getString("picture"), 0);
    		        //status = c.getString("status");
    		        return c.getString("picture") + "BREAK" + c.getString("status");
            }
        } catch (Exception e) {
            this.exception = e;
        }
        return null;
    }
    
    @Override
    protected void onPostExecute(String result) {
    	if(exception != null) {
    		Log.i("Smartdoor", "AsyncTask Exception: "+exception);
    	}
    	//Log.i("Smartdoor", "Async Result: " + result);
    	/*if(byteArray != null) {
	     	Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
	    	ImageView status_pic = (ImageView) ((Activity) mContext).findViewById(R.id.status_pic);
			status_pic.setImageBitmap(bmp);
    	}
    	TextView result_text = (TextView) ((Activity) mContext).findViewById(R.id.result);
    	result_text.setText(result);*/
    }
}
