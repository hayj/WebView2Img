package com.example.webview2img;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebView.PictureListener;
import android.webkit.WebViewClient;

/**
 * You can extends this class and perform some manipulation on the dom before generate the bitmap
 * 
 * @author hayj
 */
public class Page
{
	/**
	 * This interface define a callback listener that receives some events
	 * 
	 * @author hayj
	 */
	public interface PageListener
	{
		/**
		 * When the bitmap is generated
		 * 
		 * @param page
		 */
		public void onGenerated(Page page);
	}

	/**
	 * Perform an ordered execution of pages
	 * 
	 * @author hayj
	 */
	private class GenerationQueue
	{
		private LinkedList<Page> queue = new LinkedList<Page>();

		public void add(Page page)
		{
			// If the queue is empty, we must start the first entry :
			if(this.queue.isEmpty())
				page.start();
			// Then we add the page to the list :
			this.queue.addLast(page);
		}

		public void next()
		{
			// Remove the previous page :
			this.queue.removeFirst();
			// If we have an other page to start :
			if(!this.queue.isEmpty())
			{
				// Peek (not remove) and start the first element :
				this.queue.peek().start();
			}
			// Else we can clear the queue :
			else
				Page.clearGenerationQueue();
		}
	}

	/**
	 * Perform the generation according to some steps
	 * 
	 * @author hayj
	 */
	private class GenerationProcess
	{
		/**
		 * Set the visibility of the WebView
		 * 
		 * @author hayj
		 */
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
				}
				else
					Page.this.webView.setVisibility(View.INVISIBLE);
			}
		}

		/**
		 * The first callback of the WebView
		 * 
		 * @author hayj
		 */
		private class WebViewClientListener extends WebViewClient
		{

			public void onPageFinished(WebView view, String url)
			{
				// We set a boolean to specify that the page is loaded :
				GenerationProcess.this.pageFinished = true;
			}
		}

		/**
		 * The callback function which indicate that the bitmap can be generated
		 * 
		 * @author hayj
		 */
		private class WebViewPictureListener implements PictureListener
		{

			@Override
			@Deprecated
			public void onNewPicture(WebView view, android.graphics.Picture pic)
			{
				// If the page is finished and we already have exec this method :
				if(GenerationProcess.this.pageFinished && !GenerationProcess.this.newPicture)
				{
					GenerationProcess.this.newPicture = true;
					GenerationProcess.this.nextState();
				}
			}
		}

		/**
		 * Load content in the WebView
		 * 
		 * @author hayj
		 */
		private class WebViewLoader implements Runnable
		{
			@Override
			public void run()
			{
				// Load a string using a baseURL :
				Page.this.webView.loadDataWithBaseURL(Page.ASSET_ROOT, Page.this.getXML(), "text/html", "UTF-8", null);
			}
		}

		/**
		 * Perform the generation of the bitmap according to the webview
		 * 
		 * @author hayj
		 */
		private class BitmapGenerator extends Thread
		{
			@Override
			public void run()
			{
				Page.this.bitmap = null;
				Canvas c = null;
				// Here you can set width and height according to the html content or the Page constructor :
				Page.this.bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
				c = new Canvas(Page.this.bitmap);
				// Set the box of the WebView :
				Page.this.webView.layout(Page.this.webView.getLeft(), Page.this.webView.getTop(),
						Page.this.webView.getRight(), Page.this.webView.getBottom());
				// Clear the bitmap :
				c.drawColor(Color.WHITE);
				// Draw the bitmap :
				Page.this.webView.draw(c);
				// Go to the next step :
				GenerationProcess.this.nextState();
			}
		}

		/**
		 * Call the listener
		 * 
		 * @author hayj
		 */
		private class onGeneratedHandler implements Runnable
		{
			@Override
			public void run()
			{
				Page.this.listener.onGenerated(Page.this);
			}
		}

		int state = -1;
		private boolean pageFinished = false;
		private boolean newPicture = false;

		/**
		 * This constructor perform the first step of the generation process
		 */
		private GenerationProcess()
		{
			this.nextState();
		}

		private void nextState()
		{
			// The current state is inc :
			this.state++;
			switch(this.state)
			{
				case 0:
					// Set the webview visible
					Page.this.activity.runOnUiThread(new WebViewVisibility(true));
					break;
				case 1:
					// Then set callbacks :
					Page.this.webView.setWebViewClient(new WebViewClientListener());
					Page.this.webView.setPictureListener(new WebViewPictureListener());
					// And load content in the UI Thread :
					Page.this.activity.runOnUiThread(new WebViewLoader());
					break;
				case 2:
					// Then perform the generation :
					new BitmapGenerator().start();
					break;
				case 3:
					// Finally, we call the listener to indicate that the generation is finished :
					Page.this.activity.runOnUiThread(new onGeneratedHandler());
					// Set the visibility to INVISIBLE :
					Page.this.activity.runOnUiThread(new WebViewVisibility(false));
					// And go the next task :
					Page.generationQueue.next();
					break;
			}
		}

	}

	/**
	 * A queue containing all tasks
	 */
	private static GenerationQueue generationQueue;
	/**
	 * WebView which perform the layout
	 */
	private WebView webView;
	/**
	 * External callback listener
	 */
	private PageListener listener;
	/**
	 * The generated bitmap
	 */
	private Bitmap bitmap;
	/**
	 * The activity of the WebView
	 */
	private Activity activity;
	private Document document;
	private Element rootNode;
	private static String ASSET_ROOT = "file:///android_asset/";

	/**
	 * Constructor which init the Page. Then you can use methods to change some content and finaly use generateBitmap.
	 * An activity will be extracted from this WebView.
	 * 
	 * @param webView
	 * @param fileName
	 */
	public Page(WebView webView, String fileName)
	{
		super();
		this.webView = webView;
		this.activity = (Activity) this.webView.getContext();
		try
		{
			this.document = (Document) (new SAXBuilder()).build(this.activity.getAssets().open(fileName));
			this.rootNode = this.document.getRootElement();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Start the generation process according to GenerationProcess' steps
	 */
	private void start()
	{
		new GenerationProcess();
	}

	/**
	 * Perform a bitmap generation in a background thread but use the Thread UI according to the WebView. If you call
	 * this method from different new objects, the task will be add to a queue to not overflow the WebView.
	 * 
	 * @param listener
	 */
	public void generateBitmap(PageListener listener)
	{
		// We set the current listener :
		this.listener = listener;
		// If there is no queue, we alloc it :
		if(Page.generationQueue == null)
			Page.generationQueue = new GenerationQueue();
		// We add the current page to the queue (to be generated) :
		Page.generationQueue.add(this);
	}

	/**
	 * Return the bitmap generated. You must use this method inside the callback function onGenerated.
	 * 
	 * @return the bitmap of the Page
	 */
	public Bitmap getBitmap()
	{
		// Return the bitmap if generated :
		if(this.bitmap != null)
			return this.bitmap;

		// TODO : return a bitmap which contains a "not found" or a "not yet generated" :
		return null;
	}

	/**
	 * Clear the static queue. TO REMOVE
	 */
	private static void clearGenerationQueue()
	{
		// This must be done to fix some problems
		// ie persistent static variables in the current Virtual Machine
		Page.generationQueue = null;
	}

	/**
	 * Set all elements to value according to a specified xpath
	 */
	protected boolean setAll(String xpath, String value)
	{
		List<Element> l = getAll(xpath);
		Iterator<Element> it = l.iterator();
		boolean isSet = false;
		while(it.hasNext())
		{
			Element el = it.next();
			el.setText(value);
			isSet = true;
		}
		return isSet;
	}

	/**
	 * Set the specified xpath expr to value
	 */
	protected boolean set(String xpath, String value)
	{
		Element l = get(xpath);
		if(l != null)
		{
			l.setText(value);
			return true;
		}
		return false;
	}

	/**
	 * Get all elements according to a xpath expr
	 */
	protected List<Element> getAll(String xpath)
	{
		XPathFactory factory = XPathFactory.instance();
		XPathExpression<Element> xpathExpr = factory.compile(xpath, Filters.element(), null,
				Namespace.getNamespace("xpns", "http://www.w3.org/2002/xforms"));
		return xpathExpr.evaluate(this.rootNode);
	}

	/**
	 * Get an Element according to a xpath expr
	 */
	protected Element get(String xpath)
	{
		List<Element> l = getAll(xpath);
		if(l != null && l.get(0) != null)
			return l.get(0);
		else
			return null;
	}
	
	/**
	 * Get the String representation of the xml document
	 */
	protected String getXML()
	{
		XMLOutputter out = new XMLOutputter();
		// out.setFormat(Format.getCompactFormat());
		String compactXML = out.outputString(this.rootNode);
		return compactXML;
	}
}
