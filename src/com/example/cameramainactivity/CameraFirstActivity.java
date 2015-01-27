package com.example.cameramainactivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.googlecode.tesseract.android.TessBaseAPI;

public class CameraFirstActivity extends Activity implements Runnable
{
	public static final String FILE_PATH = Environment.getExternalStorageDirectory().toString() + "/MyOcr/";
	private static final String TAKEN_PIC = "photo";
	protected Button _button;
	protected ImageView _image;
	protected TextView _field;
	protected String _path =FILE_PATH + "/myPicture.jpg";
	private int color=R.color.black;
	protected boolean _takenpic;
	Bitmap bitmap;
	protected String _pathOpencv = FILE_PATH+"/myPicture2.jpg";
	//initializes the openCV native library
	static {
		if (!OpenCVLoader.initDebug()) {
			// Handle initialization error
			Log.e("lib", "Cannot connect to OpenCV Manager");
		}
	}
	private BaseLoaderCallback  mOpenCVCallBack = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				Mat Image = Highgui.imread(Environment.getExternalStorageDirectory()+"/myPicture.jpg");
				if (Image == null) {

					Log.e(this.toString(),"Fatal error: can't open /myPicture.jpg!");  
				}
			} break;
			default:
			{
				super.onManagerConnected(status);
			} break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		copyTrainedData();
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_camera_first);
		mOpenCVCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);//added to avoid the open CV manager app installation
		//		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, this, mOpenCVCallBack))
		//		{
		//			Log.e("TEST", "Cannot connect to OpenCV Manager");
		//		}
		_image = (ImageView) findViewById(R.id.image);
		_field = (TextView) findViewById(R.id.field);
		_button = (Button) findViewById(R.id.button);
		_button.setOnClickListener(new ButtonClickHandler());

	}



	public class ButtonClickHandler implements View.OnClickListener 
	{
		public void onClick(View view){




			File file = new File(_path);
			Uri uriOutputFile = Uri.fromFile(file);

			final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			//final Intent intent = new Intent(CameraFirstActivity.this, CameraMainActivity.class);
			//resolver Uri to be used to store the requested image.
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uriOutputFile);
			startActivityForResult(intent, 0);


		}

	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {



		if (resultCode == -1) {
			//new Thread(CameraFirstActivity.this).start();
			onTakenImage();
		} else {

		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(CameraFirstActivity.TAKEN_PIC, _takenpic);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {

		if (savedInstanceState.getBoolean(CameraFirstActivity.TAKEN_PIC)) {
			onTakenImage();
		}
	}

	private void  writetofile()
	{
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(FILE_PATH + "/myPicture3.jpeg");
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bitmap is your Bitmap instance

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void gaussianFilter() {
		Log.e( "OpenCV", "Noises in image removal method");
		Mat source = Highgui.imread(FILE_PATH + "/myPicture3.jpeg",Highgui.CV_LOAD_IMAGE_GRAYSCALE );//reading the image from path and storing the grayscale image matrix in source
		Mat destination = new Mat(source.rows(),source.cols(),source.type());
		//Imgproc.Canny(source, destination, 10, 100);
		Imgproc.GaussianBlur(source, destination,new Size(3,3), 0);//it blurs an image
		Imgproc.threshold(destination, destination, 0, 255, Imgproc.THRESH_BINARY+Imgproc.THRESH_OTSU);//Thresholding enables to achieve image segmentation
		Highgui.imwrite(_pathOpencv, destination);
		Log.e( "OpenCV", "Image processing through openCV Done");

	}
	protected void onTakenImage() {
		_takenpic = true;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize =4 ;

		bitmap = BitmapFactory.decodeFile(FILE_PATH + "/myPicture.jpg", options);
		_image.setImageBitmap(bitmap);
		try
		{
			ColorDrawable drawable = (ColorDrawable) _image.getBackground();
			color=	drawable.getColor();
		} catch(Exception e)
		{
			e.printStackTrace();
		}

		try {
			ExifInterface intex = new ExifInterface(FILE_PATH + "/myPicture.jpg");
			int exifOrientation = intex.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			int angle = 0;
			switch (exifOrientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				angle = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				angle = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				angle = 270;
				break;
			}
			if (angle != 0) {
				int width = bitmap.getWidth();
				int height = bitmap.getHeight();
				Matrix mtx = new Matrix();
				mtx.preRotate(angle);//rotating the image to extract text in a proper way
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, mtx, false);//returns immutable bitmap
			}

			// Convert to ARGB_8888, required by tessrect API
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
			
			writetofile();
			
			gaussianFilter();//processing the image using the methods of  openCV native library to feedback the tesserect engine
			
			BitmapFactory.Options option = new BitmapFactory.Options();
			options.inSampleSize =4;

			bitmap = BitmapFactory.decodeFile(_pathOpencv, option);
			Log.v(this.toString(),"bitmap from First activity ="+bitmap.toString());
		} catch (IOException e) {
			Log.e("", "Couldn't correct orientation: " + e.toString());
		}
		//Displaying ProcessDialog

		final ProgressDialog ringProgressDialog = ProgressDialog.show(CameraFirstActivity.this, "Please wait " +
				"...", "Reading text ...", true);
		ringProgressDialog.setCancelable(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					TessBaseAPI tessAPI = new TessBaseAPI();
					tessAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK);
					new ProcessOCR(CameraFirstActivity.this, tessAPI).execute();
					Thread.sleep(5000);
				} catch (Exception e) {
				}
				ringProgressDialog.dismiss();
			}
		}).start();




	}

	private static boolean assetFolderCopy(AssetManager assetManager,String fromAssetPath, String toPath) {
		try {
			String[] list_files = assetManager.list(fromAssetPath);
			new File(toPath).mkdirs();//make the necessary directory in the given path
			boolean b = true;
			for (String file : list_files)
				if (file.contains("."))
					b &= copyFolder(assetManager, 
							fromAssetPath + "/" + file,
							toPath + "/" + file);
				else 
					b &= assetFolderCopy(assetManager, 
							fromAssetPath + "/" + file,
							toPath + "/" + file);
			return b;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	private void copyTrainedData() {
		SharedPreferences settings = this.getSharedPreferences("CameraActivity", 0);
		boolean onetime = settings.getBoolean("onetime", true);
		if (onetime) { 
			SharedPreferences.Editor _sharedPref = settings.edit();
			_sharedPref.putBoolean("onetime", false);
			_sharedPref.commit();
			assetFolderCopy(getAssets(), "tessdata", FILE_PATH+"/tessdata");//copies the trained eng file from assets folder into your file path + tessdata folder

		}
	}
	private static boolean copyFolder(AssetManager assetManager,String fromAssetPath, String toPath) {
		InputStream inputstream = null;
		OutputStream outputstream = null;
		try {
			inputstream = assetManager.open(fromAssetPath);
			new File(toPath).createNewFile();
			outputstream = new FileOutputStream(toPath);
			copyFile(inputstream, outputstream);
			inputstream.close();
			inputstream = null;
			outputstream.flush();
			outputstream.close();
			outputstream = null;
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	private static void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}

	public int getColor() {

		return color;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}



	@Override
	public void run() {

		//


	}


}
