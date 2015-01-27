package com.example.cameramainactivity;



import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

final class ProcessOCR extends AsyncTask<Void, Void, Boolean> {

	private CameraFirstActivity firstActivity;
	private TessBaseAPI tessAPI;
	private ImageProcessing bitmapProcessing;
	ProcessOCR(CameraFirstActivity firstActivity, TessBaseAPI tessAPI) {
		this.firstActivity = firstActivity;
		this.tessAPI = tessAPI;

	}
	//Bitmap of Processed Image coming from OpenCV native library is feeding to Tessrect API
	@Override
	protected Boolean doInBackground(Void... params) {
		//running tessrect task in background

		Bitmap bmp=firstActivity.getBitmap();
		Log.v(this.toString(),"bitmap from main activity ="+bmp.toString());
		String textRecognized = "";

		try {     
			tessAPI.setDebug(true);
			tessAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
			tessAPI.init(CameraFirstActivity.FILE_PATH, "eng",TessBaseAPI.OEM_TESSERACT_ONLY);
			tessAPI.setImage(bmp);
			String recognizedText = tessAPI.getUTF8Text();
			if(recognizedText!=null)
			{
				textRecognized=recognizedText;
			}
			textRecognized = textRecognized.replaceAll("[^a-zA-Z0-9]+", " ");

			textRecognized = textRecognized.trim();
			if (textRecognized == null) {
				return false;
			}
			bitmapProcessing = new ImageProcessing(tessAPI);
			bitmapProcessing.setcolor(firstActivity.getColor());
			bitmapProcessing.setBoundingBoxes(tessAPI.getStrips().getBoxRects());



		} catch (RuntimeException e) {
			Log.v(this.toString(), textRecognized);
			Log.e("ProcessOcr", "Caught RuntimeException in request to Tesseract");
			e.printStackTrace();
			try {
				tessAPI.clear();
			} catch (NullPointerException e1) {

			}
			return false;
		}

		bitmapProcessing.setBitmap(bmp);
		bitmapProcessing.setText(textRecognized);


		return true;
	}
	//get the final bitmap after drawing text on rectangle
	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		firstActivity._image.setImageBitmap(bitmapProcessing.processedBitmap(bitmapProcessing.getBitmap()));

		if (tessAPI != null) {
			tessAPI.clear();

		}


	}


}
