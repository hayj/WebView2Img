package com.example.webview2img;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.webview2img.Page.PageListener;

public class WebView2ImageView extends Activity implements PageListener
{
	private ImageView image1;
	private ImageView image2;
	private WebView webView;
	private Page page1;
	private Page page2;
	private View rootView;

	private class ThreadGeneration extends Thread
	{

	}

	@Override
	public void onGenerated(final Page page)
	{
		this.rootView.post(new Runnable()
		{
			@Override
			public void run()
			{
				Bitmap b = page.getBitmap();
				if(page == WebView2ImageView.this.page1)
					WebView2ImageView.this.image1.setImageBitmap(b);
				else
					WebView2ImageView.this.image2.setImageBitmap(b);
			}
		});

		// Bitmap b = page.getBitmap();
		// if(page == this.page1)
		// this.image1.setImageBitmap(b);
		// else
		// this.image2.setImageBitmap(b);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_view);
		this.rootView = (View) findViewById(R.id.linear1);
		this.image1 = (ImageView) findViewById(R.id.image1);
		this.image2 = (ImageView) findViewById(R.id.image2);
		this.webView = (WebView) findViewById(R.id.webView1);
		Page.generationQueue = null;
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		// You can be in the UI Thread :
//		 this.page1 = new Page(WebView2ImageView.this, this.webView, "page1.html");
//		 this.page1.generateBitmap(this);
//		 this.page2 = new Page(WebView2ImageView.this, this.webView, "page2.html");
//		 this.page2.generateBitmap(this);

		// Or in an other Thread :
		(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
//				try
//				{
//					Thread.sleep(1000);
//				}
//				catch(InterruptedException e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
				Log.e("", "_1");
				WebView2ImageView.this.page1 = new Page(WebView2ImageView.this.webView, "page1.html");
				WebView2ImageView.this.page1.generateBitmap(WebView2ImageView.this);
				WebView2ImageView.this.page2 = new Page(WebView2ImageView.this.webView, "page2.html");
				WebView2ImageView.this.page2.generateBitmap(WebView2ImageView.this);
				
			}
		})).start();
	}
}