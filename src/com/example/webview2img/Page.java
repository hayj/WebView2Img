package com.example.webview2img;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebView.PictureListener;
import android.webkit.WebViewClient;

public class Page
{
	public interface PageListener
	{
		public void onGenerated(Page page);
	}

	private class GenerationQueue
	{
		private LinkedList<Page> queue = new LinkedList<Page>();
		private boolean started = false;

		public void add(Page page)
		{
			Log.e("", "_2");
			boolean start = false;
			if(this.queue.isEmpty())
				start = true;
			this.queue.addLast(page);
			if(start)
				page.start();
		}

		public void next()
		{
			Log.e("", "_2.2");
			// Remove the previous page :
			this.queue.removeFirst();
			// If we have an other page to start :
			if(!this.queue.isEmpty())
			{
				// Peek (not remove) and start the first element :
				this.queue.peek().start();
			}
		}
	}

	private class GenerationProcess
	{
		private class WebViewVisibility implements Runnable
		{
			private boolean visible;

			public WebViewVisibility(boolean visible)
			{
				super();
				this.visible = visible;
			}

			@Override
			public void run()
			{
				if(visible)
				{
					Page.this.webView.setVisibility(View.VISIBLE);
					GenerationProcess.this.nextState();
					Log.e("", "2");
				}
				else
					Page.this.webView.setVisibility(View.INVISIBLE);
			}
		}

		private class WebViewClientListener extends WebViewClient
		{
			public void onPageFinished(WebView view, String url)
			{
				GenerationProcess.this.pageFinished = true;
			}
		}

		private class WebViewPictureListener implements PictureListener
		{
			@Override
			@Deprecated
			public void onNewPicture(WebView view, android.graphics.Picture pic)
			{
				if(GenerationProcess.this.pageFinished && !GenerationProcess.this.newPicture)
				{
					GenerationProcess.this.newPicture = true;
					GenerationProcess.this.nextState();
					Log.e("", "3");
				}
			}
		}

		private class WebViewLoader implements Runnable
		{
			@Override
			public void run()
			{
				Page.this.webView
						.loadData(
								"<html><body><p>:::::EEEEEEEEEEE:::::</p><p style=\"width: 20px;height: 20px; background-color: blue;\"></p></body></html>",
								"text/html", "UTF-8");
			}
		}

		private class BitmapGenerator extends Thread
		{
			@Override
			public void run()
			{
				Page.this.bitmap = null;
				Canvas c = null;
				Page.this.bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
				c = new Canvas(Page.this.bitmap);
				Page.this.webView.layout(Page.this.webView.getLeft(), Page.this.webView.getTop(),
						Page.this.webView.getRight(), Page.this.webView.getBottom());
				c.drawColor(Color.WHITE);
				Page.this.webView.draw(c);
				GenerationProcess.this.nextState();
				Log.e("", "4");
			}
		}

		int state = 0;
		private boolean pageFinished = false;
		private boolean newPicture = false;

		public GenerationProcess()
		{
			this.nextState();
			Log.e("", "1");
		}

		private void nextState()
		{
			switch(this.state)
			{
				case 0:
					//Page.this.webView.post(new WebViewVisibility(true));
					Page.this.webView2ImageView.runOnUiThread(new WebViewVisibility(true));
					break;
				case 1:
					Page.this.webView.setWebViewClient(new WebViewClientListener());
					Page.this.webView.setPictureListener(new WebViewPictureListener());
					Page.this.webView2ImageView.runOnUiThread(new WebViewLoader());
					break;
				case 2:
					new BitmapGenerator().start();
					break;
				case 3:
					// Attention : ici le 1 du suivant comence avant le 4
					Page.this.listener.onGenerated(Page.this);
					Page.this.webView2ImageView.runOnUiThread(new WebViewVisibility(false));
					Page.generationQueue.next();
					break;
			}
			this.state++;
		}

	}

	static GenerationQueue generationQueue = null;
	private WebView webView;
	private PageListener listener;
	private Bitmap bitmap;
	private WebView2ImageView webView2ImageView;
	private Activity activity;

	public Page(WebView webView, String fileName)
	{
		super();
		if(Page.generationQueue == null)
		{
			Log.e("", "Alloc of generationQueue");
			Page.generationQueue = new GenerationQueue();
		}
		
		// if(i == 0)
		// {
		// //Le probleme c'est qu'il ne relance pas ça en mode normal (mais ou i en mode debug) :
		// Log.e("", "AAAA");
		// Page.generationQueue = new GenerationQueue();
		// i++;
		// }
		this.webView = webView;
		this.activity = (Activity) this.webView.getContext();
	}

	public void start()
	{
		Log.e("", "_4");
		new GenerationProcess();
	}

	public void setListener(PageListener listener)
	{
		this.listener = listener;
	}

	public void generateBitmap(PageListener listener)
	{
		this.listener = listener;
		Page.generationQueue.add(this);
	}

	public Bitmap getBitmap()
	{
		if(this.bitmap != null)
			return this.bitmap;

		// TODO : return a bitmap which contains a "not found" :
		return null;
	}
}
