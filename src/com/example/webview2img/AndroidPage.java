package com.example.webview2img;

import android.webkit.WebView;

/**
 * This class is an example of extend Page. The purpose is to define method which manipulate the dom.
 * @author hayj
 */
public class AndroidPage extends Page
{

	public AndroidPage(WebView webView, String fileName)
	{
		super(webView, fileName);
	}
	
	public void androizer()
	{
		this.setAllAttr("//img", "src", "android.svg");
		this.set("//p", "Android");
	}
}
