// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tsu,
// "The Art of War"

package com.hackensack.umc.cropper;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.Window;


import com.hackensack.umc.R;
import com.hackensack.umc.com.hackensack.umc.camera.CameraPreview;
import com.hackensack.umc.cropper.util.ImageViewUtil;
import com.hackensack.umc.util.Util;

import java.lang.ref.WeakReference;

/**
 * Task to crop bitmap asynchronously from the UI thread.
 */
public class BitmapCroppingWorkerTask extends AsyncTask<Void, Void, BitmapCroppingWorkerTask.Result> {

    //region: Fields and Consts

    /**
     * Use a WeakReference to ensure the ImageView can be garbage collected
     */
    private final WeakReference<CameraPreview> mCropImageViewReference;

    /**
     * the bitmap to crop
     */
    private final Bitmap mBitmap;

    /**
     * The Android URI of the image to load
     */
    private final Uri mUri;
    private final Dialog mProgressDialog;
    private  View mDialogView;

    /**
     * The context of the crop image view widget used for loading of bitmap by Android URI
     */
    private Context mContext;

    /**
     * Required cropping rectangle
     */
    private final Rect mRect;

    /**
     * The shape to crop the image
     */
    private final CropImageView.CropShape mCropShape;

    /**
     * Degrees the image was rotated after loading
     */
    private final int mDegreesRotated;

    /**
     * required width of the cropping image
     */
    private final int mReqWidth;

    /**
     * required height of the cropping image
     */
    private final int mReqHeight;
    //endregion

    public BitmapCroppingWorkerTask(CameraPreview cropImageView, Bitmap bitmap, Rect rect, CropImageView.CropShape cropShape) {
        mCropImageViewReference = new WeakReference<>(cropImageView);
        mContext = cropImageView.getContext();
        mBitmap = bitmap;
        mRect = rect;
        mCropShape = cropShape;
        mUri = null;
        mDegreesRotated = 0;
        mReqWidth = 0;
        mReqHeight = 0;

        mProgressDialog = new Dialog(mContext, R.style.AppTheme_PopupOverlay);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(mContext.getResources().getColor(R.color.semi_tranparent_white)));
        mDialogView =  ((Activity) mContext).getLayoutInflater().inflate(R.layout.humc_loading, null);
        mProgressDialog.setContentView(mDialogView);
    }

    public BitmapCroppingWorkerTask(CameraPreview cropImageView, Uri uri, Rect rect, CropImageView.CropShape cropShape, int degreesRotated, int reqWidth, int reqHeight) {
        mCropImageViewReference = new WeakReference<>(cropImageView);
        mContext = cropImageView.getContext();
        mUri = uri;
        mRect = rect;
        mCropShape = cropShape;
        mDegreesRotated = degreesRotated;
        mReqWidth = reqWidth;
        mReqHeight = reqHeight;
        mBitmap = null;
        mProgressDialog = null;
    }

    /**
     * The Android URI that this task is currently loading.
     */
    public Uri getUri() {
        return mUri;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
       // mProgressDialog.show();
    }

    /**
     * Crop image in background.
     *
     * @param params ignored
     * @return the decoded bitmap data
     */


    @Override
    protected Result doInBackground(Void... params) {
        try {
            if (!isCancelled()) {

                Bitmap bitmap = null;
                if (mUri != null) {
                    bitmap = ImageViewUtil.cropBitmap(
                            mContext,
                            mUri,
                            mRect,
                            mDegreesRotated,
                            mReqWidth,
                            mReqHeight);
                } else if (mBitmap != null) {
                    bitmap = ImageViewUtil.cropBitmap(mBitmap, mRect);
                }
                if (bitmap != null && mCropShape == CropImageView.CropShape.OVAL) {
                    bitmap = ImageViewUtil.toOvalBitmap(bitmap);
                }
                return new Result(bitmap);
            }
            return null;
        } catch (Exception e) {
            return new Result(e);
        }
    }

    /**
     * Once complete, see if ImageView is still around and set bitmap.
     *
     * @param result the result of bitmap cropping
     */
    @Override
    protected void onPostExecute(Result result) {
        if (result != null) {
            boolean completeCalled = false;
            if (!isCancelled()) {
                CameraPreview cropImageView = mCropImageViewReference.get();
                if (cropImageView != null) {
                    completeCalled = true;
                    cropImageView.onGetImageCroppingAsyncComplete(result);
                }
            }
            if (!completeCalled && result.bitmap != null) {
                // fast release of unused bitmap
                result.bitmap.recycle();
            }
        }
        mProgressDialog.dismiss();
    }

    //region: Inner class: Result

    /**
     * The result of BitmapCroppingWorkerTaskOld async loading.
     */
    public static final class Result {

        /**
         * The cropped bitmap
         */
        public final Bitmap bitmap;

        /**
         * The error that occurred during async bitmap cropping.
         */
        public final Exception error;

        Result(Bitmap bitmap) {
            this.bitmap = bitmap;
            this.error = null;
        }

        Result(Exception error) {
            this.bitmap = null;
            this.error = error;
        }
    }

    //endregion
}
