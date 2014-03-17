package com.example.webview2img;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebView.PictureListener;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.webview2img.StorageUtils.StorageInfo;

public class MainActivity extends Activity
{

	/*
	 * private class HelloWebViewClient extends WebViewClient { }
	 */

	private WebView wv;
	private boolean ready;
	private View v;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.wv = (WebView) findViewById(R.id.webView1);
		this.v = findViewById(R.id.linear2);
		// this.v.setVisibility(View.INVISIBLE);
		//wv.setBackground(background);
		this.wv.setBackgroundColor(0x00000000);
//		this.wv.setBackgroundColor(0x00FFFFFF);
		wv.setVisibility(View.VISIBLE);
		this.wv.loadData(
				"<html><body style=\"background-color: rgba(255, 255, 255, 0);\"><p>YAAAAAA</p><p style=\"width: 20px;height: 20px; background-color: blue;\"></p></body></html>",
				"text/html", "UTF-8");

		
		MainActivity.deletePNG(this);
		// v.loadUrl("http://www.google.fr");
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public MainActivity getThis()
	{
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onStart()
	{
		super.onStart();

		// if(this.v == null)
		// {
		// disp(this, "v est null");
		// }
		// else
		// {
		// deletePNG(this);
		// Bitmap bitmap = loadBitmapFromView(this.v);
		// storeBitmap(this, bitmap);
		// }
		this.ready = false;
		
		try
		{
			this.wv.setWebViewClient(new WebViewClient()
			{
				public void onPageFinished(WebView view, String url)
				{
					// ready.set(true);
					ready = true;
//					if(getThis().wv == null)
//					{
//						MainActivity.disp(getThis(), "wv est null");
//					}
//					else
//					{
//						Bitmap bitmap = MainActivity.loadBitmapFromView(getThis().wv);
//						MainActivity.storeBitmap(getThis(), bitmap);
//					}
				}
			});
			wv.setPictureListener(new PictureListener()
			{
				@Override
				@Deprecated
				public void onNewPicture(WebView view, Picture picture)
				{
					if(ready)
					{
						ready = false;
						// TODO
						if(getThis().wv == null)
						{
							MainActivity.disp(getThis(), "wv est null");
						}
						else
						{
							Bitmap bitmap = MainActivity.loadBitmapFromView(getThis().wv);
							MainActivity.storeBitmap(getThis(), bitmap);
						}
					}
				}
			});
		}
		catch(Exception e)
		{
			Log.e("MYAPP", "exception: " + e.getMessage());
			Log.e("MYAPP", "exception: " + e.toString());
		}
	}



	private static void disp(Context context, String text)
	{
		Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_LONG).show();
	}

	// http://stackoverflow.com/questions/2801116/converting-a-view-to-bitmap-without-displaying-it-in-android
	public static Bitmap loadBitmapFromView(View v)
	{
		// int width = wv.getMeasuredWidth();
		// int height = wv.getMeasuredHeight();
		// if(width == 0)
		// width = 500;
		// if(height == 0)
		// height = 500;

		// Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		// Canvas canvas = new Canvas(bitmap);
		// canvas.drawColor(Color.RED);
		// Paint pencil = new Paint();
		// pencil.setTextSize(40);
		// pencil.setColor(Color.CYAN);
		// canvas.drawCircle(150, 150, 10, pencil);

		// Picture p = wv.capturePicture();
		// p.draw(canvas);

		// canvas.drawBitmap(bitmap, 0, 0, pencil);
		// wv.draw(canvas);

		// wv.measure(
		// MeasureSpec.makeMeasureSpec(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED),
		// MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		// wv.layout(0, 0, width, height);
		// wv.setDrawingCacheEnabled(true);
		// wv.buildDrawingCache();

		Bitmap b = null;
		Canvas c = null;
		((WebView)v).setBackgroundColor(0xFFFFFFFF);
		v.setVisibility(View.VISIBLE);
		if(v.getMeasuredHeight() <= 0)
		{
			int wrap = ViewGroup.LayoutParams.WRAP_CONTENT;
			v.measure(wrap, wrap);
			// v.measure(0, 0); v.getMeasuredWidth(); v.getMeasuredHeight();
			int width = v.getMeasuredWidth();
			if(width <= 0 || width > 10000)
				width = 200;
			int height = v.getMeasuredHeight();
			if(height <= 0 || height > 10000)
				height = 200;
			b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

			c = new Canvas(b);
			
			v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
			// v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
		}
		else
		{
			// b = Bitmap.createBitmap(v.getLayoutParams().width, v.getLayoutParams().height, Bitmap.Config.ARGB_8888);
			//b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
			b = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
			c = new Canvas(b);
			v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
		}
		c.drawColor(Color.WHITE);
		v.setVisibility(View.INVISIBLE);
		v.draw(c);
		return b;
	}

	public static void storeBitmap(Context context, Bitmap bitmap)
	{
		try
		{

			// Si le fichier est lisible et qu'on peut écrire dedans
			if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
					&& !Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState()))
			{

				String dir = null;
				List<StorageInfo> list = StorageUtils.getStorageList();
				if(list.size() >= 2)
				{
					StorageInfo extDir = list.get(1);
					if(extDir.path.contains("ext") && !extDir.readonly)
						dir = extDir.path;
				}
				else
					dir = Environment.getExternalStorageDirectory().getPath();
				dir += "/Android/data/" + context.getPackageName() + "/files";

				// for(StorageInfo si : list)
				// {
				// Log.e("", si.getDisplayName());
				// Log.e("", si.toString());
				// Log.e("", si.path);
				// }

				// On crée un fichier qui correspond à l'emplacement extérieur
				File file = new File(dir);
				for(File child : file.listFiles())
				{
					child.delete();
				}
				file.mkdirs();
				file = new File(file, "test" + (new Date()).getTime() + ".png");
				file.createNewFile();
				Log.e("", file.getAbsolutePath());

				FileOutputStream out = new FileOutputStream(file);
				boolean result = bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
				if(out != null)
					out.close();
				Toast.makeText(context.getApplicationContext(), "Storage result : " + result, Toast.LENGTH_LONG).show();
			}
			else
				Log.e("Storage error", "");
		}
		catch(Exception e)
		{
			Log.e("MYAPP", "exception: " + e.getMessage());
			Log.e("MYAPP", "exception: " + e.toString());
		}
	}

	public static void deletePNG(Context context)
	{
		// Si le fichier est lisible et qu'on peut écrire dedans
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				&& !Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState()))
		{

			String dir = null;
			List<StorageInfo> list = StorageUtils.getStorageList();
			if(list.size() >= 2)
			{
				StorageInfo extDir = list.get(1);
				if(extDir.path.contains("ext") && !extDir.readonly)
					dir = extDir.path;
			}
			else
				dir = Environment.getExternalStorageDirectory().getPath();
			dir += "/Android/data/" + context.getPackageName() + "/files";

			// for(StorageInfo si : list)
			// {
			// Log.e("", si.getDisplayName());
			// Log.e("", si.toString());
			// Log.e("", si.path);
			// }

			// On crée un fichier qui correspond à l'emplacement extérieur
			File file = new File(dir);
			for(File child : file.listFiles())
			{
				child.delete();
				child = null;
			}
			file = null;
		}
	}
}
