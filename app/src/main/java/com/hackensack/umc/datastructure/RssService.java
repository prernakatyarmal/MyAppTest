package com.hackensack.umc.datastructure;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.hackensack.umc.util.Constant;

public class RssService extends IntentService {
	private static String RSS_LINK = Constant.url;
	
    public static final String ITEMS = "items";
    public static final String RECEIVER = "receiver";
     
    public RssService() {
        super("RssService");
    }
 
    @Override
    protected void onHandleIntent(Intent intent) {
        List<RssItem> rssItems = null;
        try {
            XmlParser parser = new XmlParser();
            rssItems = parser.parse(getInputStream(Constant.url));
        } catch (XmlPullParserException e) {
            Log.w(e.getMessage(), e);
        } catch (IOException e) {
            Log.w(e.getMessage(), e);
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable(ITEMS, (Serializable) rssItems);
        ResultReceiver receiver = intent.getParcelableExtra(RECEIVER);
        receiver.send(0, bundle);
    }
 
    public InputStream getInputStream(String link) {
        try {
            URL url = new URL(link);
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            return null;
        }
    }
}