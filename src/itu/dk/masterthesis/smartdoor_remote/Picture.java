package itu.dk.masterthesis.smartdoor_remote;

import java.io.ByteArrayOutputStream;

import com.gmail.yuyang226.flickr.photos.Photo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Picture {
	
	public Bitmap bitmap;
	public Photo photo;
	
	public Picture (Bitmap b) {
		super();
		this.bitmap = b;
	}
	
	public Bitmap getPicture(){
		return this.bitmap;
	}
	
	public void setPicture(Bitmap b){
		this.bitmap = b;
	}
	
	public byte[] getByteArray(){
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		this.bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		return byteArray;
	}

}
