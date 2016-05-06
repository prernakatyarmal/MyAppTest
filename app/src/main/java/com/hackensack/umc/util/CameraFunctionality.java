package com.hackensack.umc.util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.hackensack.umc.com.hackensack.umc.camera.PictureDemo;

import org.apache.http.client.protocol.ClientContextConfigurer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;

import static com.hackensack.umc.util.Base64Converter.decodeUri;

/**
 * Created by prerana_katyarmal on 11/24/2015.
 */
public class CameraFunctionality {


    private static String TAG = "CameraFunctionality";


    public static Uri dispatchTakePictureIntent(int cameraFacing, Context context) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraFacing == 1) {
            intent.putExtra("android.intent.extras.CAMERA_FACING", Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else {
            intent.putExtra("android.intent.extras.CAMERA_FACING", Camera.CameraInfo.CAMERA_FACING_BACK);
        }

        ((Activity) context).startActivityForResult(intent, Constant.FRAGMENT_CONST_REQUEST_IMAGE_CAPTURE);
        // Intent intent=new Intent(context, CustomCameraActivity.class);
        //   context.startActivity(intent);

      /*  Uri imageUri = null;

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra("android.intent.extras.CAMERA_FACING", Camera.CameraInfo.CAMERA_FACING_FRONT);
        File photo;
        try {
            // place where to store camera taken picture
            photo = createTemporaryFile(Math.random()+"picture", ".jpg");
            photo.delete();
        } catch (Exception e) {
            Log.v(TAG, "Can't create file to take picture!");
            //Toast.makeText(getActivity(), "Please check SD card! Image shot is impossible!", 10000);
            return imageUri;
        }
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        //start camera intent
        ((Activity)context).startActivityForResult(intent, Constant.FRAGMENT_CONST_REQUEST_IMAGE_CAPTURE);
        return imageUri;*/
        return null;

    }

    public static Uri dispatchTakePictureIntent(Context context, int cameraFacing, int selectedImageView) {
        Uri imageUri = null;

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        // Intent intent = new Intent(context, PictureDemo.class);
        if (cameraFacing == 1) {
            intent.putExtra("android.intent.extras.CAMERA_FACING", Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else {
            intent.putExtra("android.intent.extras.CAMERA_FACING", Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        File photo;
        try {
            // place where to store camera taken picture
            photo = CameraFunctionality.createTemporaryFile("picture_" + selectedImageView, ".png");
            // photo.delete();
        } catch (Exception e) {
            Log.v(TAG, "Can't create file to take picture!");
            //Toast.makeText(getActivity(), "Please check SD card! Image shot is impossible!", 10000);
            return imageUri;
        }
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        //start camera intent
        ((Activity) context).startActivityForResult(intent, Constant.FRAGMENT_CONST_REQUEST_IMAGE_CAPTURE);
        return imageUri;
    }

    public static File createTemporaryFile(String part, String ext) throws Exception {
        File tempDir = Environment.getExternalStorageDirectory();
        tempDir = new File(tempDir.getAbsolutePath() + "/.temp/");
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }
        File file = new File(tempDir.getAbsolutePath() + part + ext);
        if (file.exists()) {
            file.delete();
        }
        return new File(tempDir.getAbsolutePath() + part + ext);
    }

    public static void deleteDirectoryOfImages() {
        File path = Environment.getExternalStorageDirectory();
        path = new File(path.getAbsolutePath() + "/hackensack/");
        deleteRecursive(path);
    }


    public static boolean deleteDirectory(File path) {

        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        boolean isDeleted = fileOrDirectory.delete();
    }

    public static Bitmap setBitmapContast(Bitmap bitmap) {
        return changeBitmapContrastBrightness(bitmap, 20, 5);
    }

    public static Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }

    public static Bitmap checkImageOrientation(Bitmap bitmap, String photoPath, Context context) {
        ExifInterface ei = null;
        Bitmap rotatedBitmap = null;
        Cursor cursor = context.getContentResolver().query(Uri.parse(photoPath), null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        String path = cursor.getString(idx);
        try {
            ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            Log.v("CameraFunctionality", "orientation is::" + String.valueOf(orientation));
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    Log.v("CameraFunctionality", "ORIENTATION_ROTATE_90");
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    Log.v("CameraFunctionality", "ORIENTATION_ROTATE_180");
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    Log.v("CameraFunctionality", "ORIENTATION_ROTATE_270");
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;
                default:
                    Log.v("CameraFunctionality", "default orientation is::" + String.valueOf(orientation));
                    rotatedBitmap = bitmap;
                    break;
                // etc.
            }
        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;
        }

        return rotatedBitmap;

    }

    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static void setPhotoToImageView(String urlString, ImageView mImageView, Context context) {


        try {
            Bitmap bitmap = decodeUri(urlString, context);
            // bitmap = CameraFunctionality.checkImageOrientation(bitmap, urlString.toString(), context);
            mImageView.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void setOrieantationOfBitmap(String urlString, Context context) {


        try {
            Bitmap bitmap = decodeUri(urlString, context);
            bitmap = CameraFunctionality.checkImageOrientation(bitmap, urlString.toString(), context);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void setPhotoToImageViewWithoutOrientation(String urlString, ImageView mImageView, Context context) {


        try {
            Bitmap bitmap = decodeUri(urlString, context);
            mImageView.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void setBitMapToImageView(String uri, Bitmap bitmap, ImageView mImageView, Context context) {


        //  try {
        // bitmap = CameraFunctionality.rotateBitmap(bitmap,uri);
        mImageView.setImageBitmap(bitmap);
        /*} catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
    }

    public static Bitmap getBitmapFromFile(String urlString, Context context) {
        Bitmap bitmap = null;
        try {
            bitmap = decodeUri(urlString, context);
            //  bitmap = CameraFunctionality.checkImageOrientation(bitmap, urlString.toString(), context);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static void deleteImageAfterUploading(String imagePath, Context context) {
        try {

            context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Images.Media.DATA
                            + "='"
                            + Uri.parse(imagePath)
                            + "'", null);

        } catch (Exception e) {
            e.printStackTrace();

        }

        /*// to notify change
        getActivity().getContentResolver().notifyChange(
                Uri.parse("file://" + newfilee.getPath()),null);*/
    }

    public static void deleteImage(String imageFilePath, Context context) {
        String file_dj_path = imageFilePath;
        File fdelete = new File(file_dj_path);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                Log.e("-->", "file Deleted :" + file_dj_path);
                callBroadCast(context);
            } else {
                Log.e("-->", "file not Deleted :" + file_dj_path);
            }
        }
    }

    public static void callBroadCast(Context context) {
        if (Build.VERSION.SDK_INT >= 14) {
            Log.e("-->", " >= 14");
            MediaScannerConnection.scanFile(context, new String[]{Environment.getExternalStorageDirectory().toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                /*
                 *   (non-Javadoc)
                 * @see android.media.MediaScannerConnection.OnScanCompletedListener#onScanCompleted(java.lang.String, android.net.Uri)
                 */
                public void onScanCompleted(String path, Uri uri) {
                    Log.e("ExternalStorage", "Scanned " + path + ":");
                    Log.e("ExternalStorage", "-> uri=" + uri);
                }
            });
        } else {
            Log.e("-->", " < 14");
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }

    public static String getRealPathFromURI(Uri uri, Context context) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void hideImageFromGallery(String imagePath, Context context) {
        File file = new File(getRealPathFromURI(Uri.parse(imagePath), context));
        String subString = imagePath + ".nomedia";
        File reNamedFile = new File(subString);
        boolean reNamed = file.renameTo(reNamedFile);
        if (reNamed) {
            boolean delete = file.delete();
        }
        Log.v(TAG, reNamedFile.getAbsolutePath());
    }

    public static String getNmaeWithoutNomedia(String imagePath) {
        File file = new File(imagePath);
        String subString = imagePath.substring(0, imagePath.lastIndexOf("."));
        File reNamedFile = new File(subString);
        boolean reNamed = file.renameTo(reNamedFile);
        if (reNamed) {
            boolean delete = file.delete();
        }
        Log.v("Camera", file.getAbsolutePath());
        return reNamedFile.getAbsolutePath();
    }


    public static String storeImagesInFolder(String imagePath, Context context) {
        Bitmap bitmap = null;
        final String appDirectoryName = "test";
        final File imageRoot = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), appDirectoryName);
        imageRoot.mkdirs();
        File file = new File(imageRoot, "temp"); // the File to save to
        try {
            FileOutputStream fOut = new FileOutputStream(file);

            bitmap = decodeUri(imagePath, context);
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
            fOut.flush();
            fOut.close();

            //deleteImageAfterUploading(imagePath,context);
            Log.v(TAG, file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file.getAbsolutePath();
// open OutputStream and write the file
    }

    public static Uri saveCropedImage(Bitmap bitmap, Context mContext, int selectedImageView) {
        File imageFile = null;
        Uri uri = null;
        String state = Environment.getExternalStorageState();
        File folder = null;
        if (state.contains(Environment.MEDIA_MOUNTED)) {
            folder = new File(Environment
                    .getExternalStorageDirectory() + "/.Demo");
        } else {
            folder = new File(Environment
                    .getExternalStorageDirectory() + "/.Demo");
        }

        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        try {
            if (success) {
                imageFile = new File(folder.getAbsolutePath()
                        + File.separator
                        + "insuranceCard_"
                        + "Image.png");
                if (imageFile.exists()) {
                    boolean b = imageFile.delete();
                }

                imageFile.createNewFile();

            } else {
                Toast.makeText(mContext, "Image Not saved",
                        Toast.LENGTH_SHORT).show();
                return uri;
            }

            ByteArrayOutputStream ostream = new ByteArrayOutputStream();

            // save image into gallery
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);

            FileOutputStream fout = new FileOutputStream(imageFile);
            fout.write(ostream.toByteArray());
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN,
                System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.MediaColumns.DATA,
                imageFile.getAbsolutePath());

        uri = mContext.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        return uri;
    }

    public static Uri saveCropedImage_1(Bitmap bitmap, Context mContext, int seelectedImageView) {
        Uri imageUri = null;

        File photo = null;
        try {
            // place where to store camera taken picture


            photo = CameraFunctionality.createTemporaryFile("picture_cropped" + seelectedImageView, ".png");
            // photo.delete();


            ByteArrayOutputStream ostream = new ByteArrayOutputStream();

            // save image into gallery
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);

            FileOutputStream fout = new FileOutputStream(photo);
            fout.write(ostream.toByteArray());
            fout.flush();
            fout.close();
        } catch (Exception e) {
            Log.v(TAG, "Can't create file to take picture!");
            //Toast.makeText(getActivity(), "Please check SD card! Image shot is impossible!", 10000);

        }
        imageUri = Uri.fromFile(photo);
        /*ContentValues values = new ContentValues();


        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.MediaColumns.DATA,
                photo.getAbsolutePath());

        Uri uri = mContext.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);*/
        //start camera intent
        return imageUri;
    }

    public static String writeBase64(int selectedImageView, String fcontent) {

        //  String fpath = "/sdcard/"+fname+".txt";

        File file = null;
        try {
            file = createTemporaryFile("Base64_" + selectedImageView, ".txt");
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(fcontent);
            bw.flush();
            bw.close();

            Log.d("Suceess", "Sucess");
            return file.getAbsolutePath();


        } catch (Exception e) {
            e.printStackTrace();
        }

        // If file does not exists, then create it

        return null;

    }

    public static Intent createCameCropInatent(Uri picUri) {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");


        cropIntent.setDataAndType(picUri, "image");


        //cropIntent.setData(picUri);
        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("aspectX", 0);
        cropIntent.putExtra("aspectY", 0);
        cropIntent.putExtra("return-data", false);

        return cropIntent;
   /* int thumbnailwidth = 640, thumbnailheight = 320;
    Intent intent = new Intent("com android. camera.action.CROP");
    intent.setType("image");
    intent.setData(picUri);
    intent.putExtra("corp","true");
    intent.putExtra("outputX",thumbnailwidth);
    intent.putExtra("outputY",thumbnailheight);
    intent.putExtra("aspectX",thumbnailwidth);
    intent.putExtra("aspectY",thumbnailheight);
    intent.putExtra("return-data",false);
    intent.putExtra("scale",true);
    return intent;*/

    }

    public static Intent createDispatchPictureIntent(Context context, int selectedImageView) {

        Uri imageUri = null;

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");


        File photo;
        try {
            photo = CameraFunctionality.createTemporaryFile("picture_temp" + selectedImageView, ".png");
            photo.delete();
        } catch (Exception e) {
            return null;
        }
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        //start camera intent
            /*Util.saveSelectedImageUriPath(context, imageUri);
            Util.saveSelectedImageView(context, selectedImageView);*/
        return intent;
        //  return getPhotoFromCamera();

    }

    public static String readBasee64File(String fpath) {

        BufferedReader br = null;
        String response = null;

        try {

            StringBuffer output = new StringBuffer();

            br = new BufferedReader(new FileReader(fpath));
            String line = "";
            while ((line = br.readLine()) != null) {
                output.append(line + "n");
            }
            response = output.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return null;

        }
        return response;

    }

    public static byte[] compressImageBitMap(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        return bytes;
    }

    public static Bitmap unCompressedImage(byte[] bytes) {
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bmp;
    }

    public static Uri getImageFromCamera(Context context) {
        // Describe the columns you'd like to have returned. Selecting from the Thumbnails location gives you both the Thumbnail Image ID, as well as the original image ID
        String[] projection = {
                MediaStore.Images.Thumbnails._ID,  // The columns we want
                MediaStore.Images.Thumbnails.IMAGE_ID,
                MediaStore.Images.Thumbnails.KIND,
                MediaStore.Images.Thumbnails.DATA};
        String selection = MediaStore.Images.Thumbnails.KIND + "=" + // Select only mini's
                MediaStore.Images.Thumbnails.MINI_KIND;

        String sort = MediaStore.Images.Thumbnails._ID + " DESC";

//At the moment, this is a bit of a hack, as I'm returning ALL images, and just taking the latest one. There is a better way to narrow this down I think with a WHERE clause which is currently the selection variable
        Cursor myCursor = ((Activity) context).managedQuery(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, selection, null, sort);

        long imageId = 0l;
        long thumbnailImageId = 0l;
        String thumbnailPath = "";

        try {
            myCursor.moveToFirst();
            imageId = myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID));
            thumbnailImageId = myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID));
            thumbnailPath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
        } finally {
            myCursor.close();
        }

        //Create new Cursor to obtain the file Path for the large image

        String[] largeFileProjection = {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA
        };

        String largeFileSort = MediaStore.Images.ImageColumns._ID + " DESC";
        myCursor = ((Activity) context).managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, largeFileProjection, null, null, largeFileSort);
        String largeImagePath = "";

        try {
            myCursor.moveToFirst();

//This will actually give yo uthe file path location of the image.
            largeImagePath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
        } finally {
            myCursor.close();
        }
        // These are the two URI's you'll be interested in. They give you a handle to the actual images
        Uri uriLargeImage = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(imageId));
        Uri uriThumbnailImage = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, String.valueOf(thumbnailImageId));

// I've left out the remaining code, as all I do is assign the URI's to my own objects anyways...
        return uriLargeImage;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, String uri) {
        int orientation = 0;
        try {
            ExifInterface exif = new ExifInterface(Uri.parse(uri).getPath());
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getCameraPhotoOrientation(Context context, Uri imageUri, String imagePath) {
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);

            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

            Log.i("RotateImage", "Exif orientation: " + orientation);
            Log.i("RotateImage", "Rotate value: " + rotate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rotate;
    }

    static int[][] knl = {
            {0, -1, 0},
            {-1, 5, -1},
            {0, -1, 0}
    };

    final static int KERNAL_WIDTH = 3;
    final static int KERNAL_HEIGHT = 3;

    public static Bitmap processingBitmap(Bitmap src) {
        Bitmap dest = Bitmap.createBitmap(
                src.getWidth(), src.getHeight(), src.getConfig());

        int bmWidth = src.getWidth();
        int bmHeight = src.getHeight();
        int bmWidth_MINUS_2 = bmWidth - 2;
        int bmHeight_MINUS_2 = bmHeight - 2;

        for (int i = 1; i <= bmWidth_MINUS_2; i++) {
            for (int j = 1; j <= bmHeight_MINUS_2; j++) {

                //get the surround 3*3 pixel of current src[i][j] into a matrix subSrc[][]
                int[][] subSrc = new int[KERNAL_WIDTH][KERNAL_HEIGHT];
                for (int k = 0; k < KERNAL_WIDTH; k++) {
                    for (int l = 0; l < KERNAL_HEIGHT; l++) {
                        subSrc[k][l] = src.getPixel(i - 1 + k, j - 1 + l);
                    }
                }

                //subSum = subSrc[][] * knl[][]
                int subSumA = 0;
                int subSumR = 0;
                int subSumG = 0;
                int subSumB = 0;

                for (int k = 0; k < KERNAL_WIDTH; k++) {
                    for (int l = 0; l < KERNAL_HEIGHT; l++) {
                        subSumA += Color.alpha(subSrc[k][l]) * knl[k][l];
                        subSumR += Color.red(subSrc[k][l]) * knl[k][l];
                        subSumG += Color.green(subSrc[k][l]) * knl[k][l];
                        subSumB += Color.blue(subSrc[k][l]) * knl[k][l];
                    }
                }

                if (subSumA < 0) {
                    subSumA = 0;
                } else if (subSumA > 255) {
                    subSumA = 255;
                }

                if (subSumR < 0) {
                    subSumR = 0;
                } else if (subSumR > 255) {
                    subSumR = 255;
                }

                if (subSumG < 0) {
                    subSumG = 0;
                } else if (subSumG > 255) {
                    subSumG = 255;
                }

                if (subSumB < 0) {
                    subSumB = 0;
                } else if (subSumB > 255) {
                    subSumB = 255;
                }

                dest.setPixel(i, j, Color.argb(
                        subSumA,
                        subSumR,
                        subSumG,
                        subSumB));
            }
        }

        return dest;
    }
    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth)
    {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }
}
