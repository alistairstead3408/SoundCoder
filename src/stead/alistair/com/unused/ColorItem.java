package stead.alistair.com.unused;

import android.graphics.Color;

public class ColorItem {
	
	private int h, s, v;
	
	public ColorItem(int hIn){
		h = hIn;
		s = -1;
		v = -1;
	}
	
	public ColorItem(int hIn, int sIn){
		h = hIn;
		s = sIn;
		v = -1;
	}
	
	public ColorItem(int hIn, int sIn, int vIn){
		h = hIn;
		s = sIn;
		v = vIn;
	}
	
	public int getRGB(){
		//do some conversion here
		//TODO write rgb conversion
		int r = h;
		int g = s;
		int b = v;
		int color = Color.argb(1, r, g, b);
		return color;
	}
	
	public int getHSV(){
		float [] hsv = new float[3];
		hsv[0] = h;
		hsv[1] = s;
		hsv[2] = v;
		int color = Color.HSVToColor(hsv);
		return color;
	}
	
	public int getHue(){
		return h;
	}
	
	public int getSaturation(){
		return s;
	}
	
	public int getValue(){
		return v;
	}
	
	public String toString(){	
		return "(" + h + ", " + s + ", " + v + ")";
	}

}
