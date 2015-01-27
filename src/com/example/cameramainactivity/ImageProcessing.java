package com.example.cameramainactivity;

import java.util.List;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

public class ImageProcessing {
	private Paint paint;
	private List<Rect> boundingBoxes;
	private String text;
	private Bitmap bitmap;
	private TessBaseAPI tessAPI;
	private int color;

	public ImageProcessing(TessBaseAPI tessAPI) {
		this.paint = new Paint();
		this.tessAPI=tessAPI;
	}
	public String getText() {
		return text;
	}

	public List<Rect> getBoundingBoxes() {
		return boundingBoxes;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setBoundingBoxes(List<Rect> BoundingBoxes) {
		this.boundingBoxes = BoundingBoxes;
	}

	public void setcolor(int color) {
		this.color=color;
	}

	public Bitmap getBitmap()
	{
		return bitmap;
	}

	public Bitmap processedBitmap(Bitmap b)
	{
		//set the background color taking from original image
		Paint paint = new Paint();
		if(color==-1)
		{
			paint.setColor(b.getPixel(b.getWidth()/2, b.getHeight()/2));
		}
		else
		{
			paint.setColor(color);
		}
		paint.setStyle(Paint.Style.FILL);
		paint.setStrokeWidth(10);
		Bitmap bitmap = Bitmap.createBitmap(b.getWidth(),b.getHeight(),Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawPaint(paint); 

		paint = new Paint(); 
		paint.setStrokeWidth(10);
		paint.setFilterBitmap(true);

		for(int i=0;i<boundingBoxes.size();i++)
		{

			Rect r = boundingBoxes.get(i);//reading the text strip wise
			tessAPI.setRectangle(r);

			paint.setColor(Color.WHITE); 
			paint.setTextSize(60); 
			String recognisedtext= tessAPI.getUTF8Text();
			recognisedtext = recognisedtext.replaceAll("[^a-zA-Z0-9]+", " ");
			recognisedtext=recognisedtext.trim();
			canvas.drawText(recognisedtext, r.left,r.centerY(), paint);
			Log.v("", "drawn text="+recognisedtext);
		}
		return bitmap;
	}

}
