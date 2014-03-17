package com.example.webview2img;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.ImageView;

import com.example.webview2img.Page.PageListener;

/**
 * Example activity which load R.layout.image_view and create new pages
 * @author hayj
 */
public class WebView2ImageView extends Activity implements PageListener
{
	/**
	 * The first ImageView display page1.html
	 */
	private ImageView image1;
	/**
	 * page2.html
	 */
	private ImageView image2;
	/**
	 * A WebView must be create to perform the generation
	 */
	private WebView webView;
	private Page page1;
	private Page page2;

	/**
	 * This is the callback function acording to a specified Page
	 */
	@Override
	public void onGenerated(final Page page)
	{
		Bitmap b = page.getBitmap();
		if(page == WebView2ImageView.this.page1)
			WebView2ImageView.this.image1.setImageBitmap(b);
		else
			WebView2ImageView.this.image2.setImageBitmap(b);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_view);
		this.image1 = (ImageView) findViewById(R.id.image1);
		this.image2 = (ImageView) findViewById(R.id.image2);
		this.webView = (WebView) findViewById(R.id.webView1);
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		// You can be in the UI Thread :
		this.page1 = new Page(this.webView, "page1.html");
		this.page1.generateBitmap(this);
		this.page2 = new Page(this.webView, "page2.html");
		this.page2.generateBitmap(this);

		// Or in an other Thread :
		(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Thread.sleep(2000);
				}
				catch(InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				WebView2ImageView.this.page1 = new Page(WebView2ImageView.this.webView, "page2.html");
				WebView2ImageView.this.page1.generateBitmap(WebView2ImageView.this);
				WebView2ImageView.this.page2 = new Page(WebView2ImageView.this.webView, "page1.html");
				WebView2ImageView.this.page2.generateBitmap(WebView2ImageView.this);

			}
		})).start();
	}
}