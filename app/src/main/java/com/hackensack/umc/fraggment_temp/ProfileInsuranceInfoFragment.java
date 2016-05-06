package com.hackensack.umc.fraggment_temp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hackensack.umc.R;
import com.hackensack.umc.activity.RegistrationDetailsActivity;
import com.hackensack.umc.cropimage.CropImage;
import com.hackensack.umc.util.Base64Converter;
import com.hackensack.umc.util.CameraFunctionality;
import com.hackensack.umc.util.Constant;
import com.hackensack.umc.util.Util;

import java.io.File;
import java.io.Serializable;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileInsuranceInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileInsuranceInfoFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "ProfileInsuranceInfoFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private Button btnProceed;
    private ImageView imgIcFront;
    private ImageView imgIcBack;
    private int selectedImageView;
    private boolean idInsuranceFargmentAddeed;
    private Uri pathIcFront;
    private Uri pathIcBack;
    private Button btnManul;
    private TextView txtFontInsuCardMessage;
    private TextView txtbackInsuCardMessage;
    private Uri imageUri;
    private Intent cameraIntent;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileInsuranceInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileInsuranceInfoFragment newInstance(String param1, String param2) {
        ProfileInsuranceInfoFragment fragment = new ProfileInsuranceInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileInsuranceInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_insurance_info, container, false);
        inflateView(view);
        return view;
    }

    private void inflateView(View view) {
        btnProceed = (Button) view.findViewById(R.id.proceed_btn_insu);
        btnProceed.setOnClickListener(this);
        imgIcFront = (ImageView) view.findViewById(R.id.ic_front);
        imgIcBack = (ImageView) view.findViewById(R.id.ic_back);
        imgIcFront.setOnClickListener(this);
        imgIcBack.setOnClickListener(this);
        btnManul = ((Button) view.findViewById(R.id.manual_btn));
        txtFontInsuCardMessage = (TextView) view.findViewById(R.id.txtInsFrontMessage);
        txtbackInsuCardMessage = (TextView) view.findViewById(R.id.txtInsBackMessage);

        btnManul.setOnClickListener(this);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onImagesSelected(Uri uri, int selectedImageView, Bitmap bitmapImage, String base64Path) {
        if (mListener != null) {
            mListener.onInsuranceFragmentInteraction(uri, selectedImageView, bitmapImage, base64Path);
        }
    }

    public void setCroppedImageBitMap(Bitmap bitMap, int selectedImageView) {
        if (mListener != null) {
            mListener.getCroppedImageBitmapForIsurance(bitMap, selectedImageView);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onInsuranceFragmentInteraction(Uri uri, int selectedImageView, Bitmap croppedBitmapImage, String base64Path);

        public void getCroppedImageBitmapForIsurance(Bitmap bitmap, int selectedImageView);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.proceed_btn_insu:
                if ((pathIcFront != null && pathIcBack == null) || (pathIcBack != null && pathIcFront == null)) {
                    Util.showAlert(getActivity(), "Please attach Insurance proof for front and back.", "Alert");
                } else {
                    ProfileIdInfoFragment profileIdInfoFragment = new ProfileIdInfoFragment();
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.add(R.id.fragmentIdContainer, profileIdInfoFragment, "IdFragment");
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
                break;

            case R.id.ic_front:
                selectedImageView = Constant.INSU_PROOF_FRONT;
                imageUri = dispatchTakePictureIntent();
                onImagesSelected(imageUri, selectedImageView, null, null);

                break;
            case R.id.ic_back:
                selectedImageView = Constant.INSU_PROOF_BACK;
                imageUri = dispatchTakePictureIntent();
                onImagesSelected(imageUri, selectedImageView, null, null);

                break;
            case R.id.manual_btn:
                Intent intent = new Intent(getActivity(), RegistrationDetailsActivity.class);
                Bundle b = new Bundle();
                b.putInt(Constant.REGISTRATION_MODE, Constant.MANUAL);
                intent.putExtra(Constant.BUNDLE, b);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(selectedImageView == Constant.ID_PROOF_FRONT || selectedImageView == Constant.ID_PROOF_BACK) {
            Fragment fragment = (Fragment)getChildFragmentManager().findFragmentByTag("IdFragment");
            if (fragment != null) {
                fragment.onActivityResult(requestCode, resultCode, data);
                return ;
            }
        }

        switch (requestCode) {
            case Constant.FRAGMENT_CONST_REQUEST_IMAGE_CAPTURE:
                if (resultCode == getActivity().RESULT_OK) {
                    /*Intent intent = new Intent(getActivity(), ActivityCropImage.class);
                    intent.setData(imageUri);


                   // intent.putExtra(Constant.Cropp_InterFace,  saveCroppedImage);
                    intent.putExtra(Constant.SELECTED_IMAGE_VIEW, selectedImageView);
                    startActivityForResult(intent, Constant.CROP_IMAGE_ACTIVITY);*/
                    // if(data==null){

                    //  }
                    // else{
                 /*   if (imageUri == null) {
                        imageUri=getImageFromCamera();
                    }*/
                    //imageUri=Util.getSelectedImageUriPath(getActivity());
                    //doCrop(imageUri);
                    cropPictureIntent(imageUri);

                    // }
                }
                break;
            default:
                //imageUri=Util.getSelectedImageUriPath(getActivity());
                //selectedImageView=Util.getSelectedImageView(getActivity());
                if (resultCode == getActivity().RESULT_OK) {
                    if(data!=null) {
                        imageUri = getImageUriFromIntent(data);
                        setImageToImageViewAfterCrop(imageUri);
                    }else{
                        setImageToImageViewAfterCrop(imageUri);
                    }
                }
                break;
        }

    }

    private Uri getImageUriFromIntent(Intent data) {
        Uri imageUriFromIntent=null;
        try {
            if (data != null) {
                 imageUriFromIntent = data.getData();
                // String base64FilePath=data.getStringExtra(Constant.BASE64_FILE_PATH);*/
                if (imageUriFromIntent == null) {
                    imageUriFromIntent = data.getExtras().getParcelable("data");
                }

                if(imageUriFromIntent==null){
                    imageUriFromIntent=imageUri;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageUriFromIntent;
    }

    private void setImageToImageViewAfterCrop(Uri imageUri) {
        String base64FilePath;
        try {
            Bitmap bitmapImage = CameraFunctionality.getBitmapFromFile(imageUri.toString(), getActivity());
            bitmapImage=CameraFunctionality.rotateBitmap(bitmapImage, imageUri.toString());
            // bitmapImage = data.getExtras().getParcelable("data");
            //bitmapImage=CameraFunctionality.setBitmapContast(bitmapImage);
            //  bitmapImage=CameraFunctionality.processingBitmap(bitmapImage);
            String base64 = Base64Converter.createBase64StringFromBitmap(bitmapImage, getActivity());
            // base64FilePath = CameraFunctionality.writeBase64(selectedImageView, base64);


            //  Log.v("onActivityResult", imageUri.toString());
            if (selectedImageView == Constant.INSU_PROOF_FRONT) {
                onImagesSelected(imageUri, selectedImageView, bitmapImage, base64);
                CameraFunctionality.setBitMapToImageView(imageUri.toString(), bitmapImage, imgIcFront, getActivity());
                pathIcFront = imageUri;
                txtFontInsuCardMessage.setText(R.string.ic_front_image_set_message);
            } else if (selectedImageView == Constant.INSU_PROOF_BACK) {
                CameraFunctionality.setBitMapToImageView(imageUri.toString(), bitmapImage, imgIcBack, getActivity());
                onImagesSelected(imageUri, selectedImageView, null, base64);
                pathIcBack = imageUri;
                txtbackInsuCardMessage.setText(R.string.ic_back_image_set_message);


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constant.CAPTURE_IMAGE_URI, imageUri.toString());
    }

    public Uri dispatchTakePictureIntent() {

        Uri imageUri = null;
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo;
        try {
            photo = CameraFunctionality.createTemporaryFile("picture_" + selectedImageView, ".png");
            //photo.delete();
        } catch (Exception e) {
            return imageUri;
        }
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
       /* if(Util.isApiVersionJellyBean()) {
            intent.setData(imageUri);
        }*/
        //start camera intent
        startActivityForResult(intent, Constant.FRAGMENT_CONST_REQUEST_IMAGE_CAPTURE);
       /* Util.saveSelectedImageUriPath(getActivity(), imageUri);
        Util.saveSelectedImageView(getActivity(),selectedImageView);*/
        return imageUri;
        //  return getPhotoFromCamera();
    }


    public Uri getImageFromCamera() {
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
        Cursor myCursor = getActivity().managedQuery(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, selection, null, sort);

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
        myCursor = getActivity().managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, largeFileProjection, null, null, largeFileSort);
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

    private void cropPictureIntent(Uri picUri) {
        try {

            Intent cropIntent = CameraFunctionality.createCameCropInatent(picUri);
            Fragment fragment = this;
            fragment.startActivityForResult(cropIntent, Constant.CROP_IMAGE_ACTIVITY);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT);
            toast.show();

            setImageToImageViewAfterCrop(imageUri);

        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!=null){
            imageUri=Uri.parse(savedInstanceState.getString(Constant.CAPTURE_IMAGE_URI));
        }
    }

}
