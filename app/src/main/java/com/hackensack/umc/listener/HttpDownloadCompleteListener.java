package com.hackensack.umc.listener;

import com.hackensack.umc.datastructure.HttpResponse;
import com.hackensack.umc.http.HttpDownloader;

/**
 * Created by gaurav_salunkhe on 9/23/2015.
 */
public interface HttpDownloadCompleteListener {


    public void HttpDownloadCompleted(HttpResponse data);

}
