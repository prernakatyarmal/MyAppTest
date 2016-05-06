package com.hackensack.umc.fargment_manual_crop;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hackensack.umc.R;
import com.hackensack.umc.activity.ActivityCropImage;
import com.hackensack.umc.activity.RegistrationDetailsActivity;
import com.hackensack.umc.util.Base64Converter;
import com.hackensack.umc.util.CameraFunctionality;
import com.hackensack.umc.util.Constant;
import com.hackensack.umc.util.Util;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.hackensack.umc.fragment.ProfileInsuranceInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link com.hackensack.umc.fragment.ProfileInsuranceInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


public class ProfileIdInfoFragment extends Fragment implements View.OnClickListener, com.hackensack.umc.fragment.ProfileInsuranceInfoFragment.OnFragmentInteractionListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "ProfileIdFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private Button btnProceed;
    private ImageView imgIcFront;
    private ImageView imgIcBack;
    private int selectedImageView;
    private boolean idInsuranceFargmentAddeed;
    private Uri pathDlFront;
    private Uri pathDlBack;
    private Button btnManul;
    private TextView txtIdFrontMessage;
    private TextView txtIdBackMessage;
    private Uri imageUri;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileInsuranceInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static com.hackensack.umc.fragment.ProfileInsuranceInfoFragment newInstance(String param1, String param2) {
        com.hackensack.umc.fragment.ProfileInsuranceInfoFragment fragment = new com.hackensack.umc.fragment.ProfileInsuranceInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileIdInfoFragment() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_id_info, container, false);
        inflateView(view);
        return view;
    }

    private void inflateView(View view) {
        btnProceed = (Button) view.findViewById(R.id.proceed_btn_id);
        btnProceed.setOnClickListener(this);
        imgIcFront = (ImageView) view.findViewById(R.id.dl_front);
        imgIcBack = (ImageView) view.findViewById(R.id.dl_back);
        imgIcFront.setOnClickListener(this);
        imgIcBack.setOnClickListener(this);
        btnManul = ((Button) view.findViewById(R.id.manual_btn));
        txtIdFrontMessage = (TextView) view.findViewById(R.id.txtIdFrontMessage);
        txtIdBackMessage = (TextView) view.findViewById(R.id.txtIdBackMessage);

        btnManul.setOnClickListener(this);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onImagesSelected(Uri uri, int selectedImageView, Bitmap croppedImage, String base64FilePath) {
        if (mListener != null) {
            mListener.onIdFragmentInteraction(uri, selectedImageView, croppedImage, base64FilePath);
        }
    }

    public void setCroppedImageBitMap(Bitmap bitMap, int selectedImageView) {
        if (mListener != null) {
            mListener.getCroppedImageBitmapForProfile(bitMap, selectedImageView);
        }
    }

    public void onProceedButtonClicked() {
        mListener.onProceedButtonClicked();
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

    @Override
    public void onInsuranceFragmentInteraction(Uri uri, int selectedImageView) {

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
        public void onIdFragmentInteraction(Uri uri, int selectedImageView, Bitmap croppedImage, String base64FilePath);

        public void onProceedButtonClicked();

        public void getCroppedImageBitmapForProfile(Bitmap bitmap, int selectedImageView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.proceed_btn_id:
                // if(!idInsuranceFargmentAddeed) {
                if ((pathDlFront != null && pathDlBack == null) || (pathDlBack != null && pathDlFront == null)) {
                    Util.showAlert(getActivity(), "Please attach Id proof for front and back.", "Alert");
                } else {
                    mListener.onProceedButtonClicked();
                }

                //}
                break;

            case R.id.dl_front:
                selectedImageView = Constant.ID_PROOF_FRONT;
                imageUri = dispatchTakePictureIntent(0);

                //  dispatchGalleryPictureIntent();

                break;
            case R.id.dl_back:
                selectedImageView = Constant.ID_PROOF_BACK;
                imageUri = dispatchTakePictureIntent(0);
             /*  pathIcBack = dispatchTakePictureIntent(0);
                Log.v(TAG, pathIcBack);*/
                //dispatchGalleryPictureIntent();

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
        // Log.v("data", data.toString());
        switch (requestCode) {
            case Constant.FRAGMENT_CONST_REQUEST_IMAGE_CAPTURE:
                if (resultCode == getActivity().RESULT_OK) {
                    try {
                        if (data != null) {
                            //imageUri = data.getData();
                            if (imageUri == null) {
                                /*Bitmap bimapImage= (Bitmap) data.getExtras().get("data");
                                imageUri=getImageUri(getActivity(),bimapImage);*/

                            }
                            Log.v("onActivityResult", imageUri.toString());
                           /* if (selectedImageView == Constant.ID_PROOF_FRONT) {
                               *//* onImagesSelected(imageUri, selectedImageView);
                                pathDlFront = imageUri;
                                CameraFunctionality.setPhotoToImageView(imageUri.toString(), imgIcFront, getActivity());
                                txtIdFrontMessage.setText(R.string.id_front_image_set_message);*//*
                                *//*Intent intent=new Intent(getActivity(), ActivityCropImage.class);
                                intent.setData(imageUri);
                                startActivityForResult(intent, Constant.CROP_IMAGE_ACTIVITY);*//*


                            } else if (selectedImageView == Constant.ID_PROOF_BACK) {
                               *//* CameraFunctionality.setPhotoToImageView(imageUri.toString(), imgIcBack, getActivity());
                                onImagesSelected(imageUri, selectedImageView);
                                pathDlBack = imageUri;
                                txtIdBackMessage.setText(R.string.id_back_image_set_message);*//*
                            }*/

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(getActivity(), ActivityCropImage.class);
                    intent.setData(imageUri);
                    intent.putExtra(Constant.SELECTED_IMAGE_VIEW, selectedImageView);
                    startActivityForResult(intent, Constant.CROP_IMAGE_ACTIVITY);
                    //cropPictureIntent(imageUri);
                }
                break;
            case Constant.CROP_IMAGE_ACTIVITY:
                String base64FilePath;
                if (resultCode == getActivity().RESULT_OK) {
                    try {
                        if (data != null) {
                            Uri imageUri = data.getData();

                            /*byte[] byteArray = data.getByteArrayExtra("byteArray");
                            Bitmap bitmapImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length)*/;

                            Bitmap bitmapImage = data.getParcelableExtra(Constant.CROPPED_IMAGE);
                            //bitmapImage = CameraFunctionality.getBitmapFromFile(imageUri.toString(), ProfileSelfieManualCropActivity.this);
                            // String base64=CameraFunctionality.readBasee64File(data.getStringExtra(Constant.BASE64_FILE_PATH));                                 // String base64 = Base64Converter.createBase64StringFromBitmap(bitmapImage, ProfileSelfieManualCropActivity.this);
                            // String base64=Base64Converter.createBitmapFromFileImage(imageUri);
                            String base64 = Base64Converter.createBase64StringFromBitmap(bitmapImage, getActivity());
                            // Bitmap bitmapImage =data.getParcelableExtra(Constant.BITMAP_IMAGE);*/

                            //if(imageUri==null){
                            /*   bitmapImage= data.getExtras().getParcelable("data");
                            String base64= Base64Converter.createBase64StringFromBitmap(bitmapImage, getActivity());
                            base64FilePath=CameraFunctionality.writeBase64(selectedImageView, base64);*/
                            //}

                            //Bitmap bitmapImage;
                            // bitmapImage = CameraFunctionality.getBitmapFromFile(imageUri.toString(), getActivity());
                            //String base64=Base64Converter.createBitmapFromFileImage(imageUri);
                            //  String base64 = Base64Converter.createBase64StringFromBitmapNew(bitmapImage,getActivity());
                            //String base64=CameraFunctionality.readBasee64File(data.getStringExtra(Constant.BASE64_FILE_PATH));


                            //   Log.v("onActivityResult", imageUri.toString());
                            if (selectedImageView == Constant.ID_PROOF_FRONT) {
                                CameraFunctionality.setPhotoToImageView(imageUri.toString(), imgIcFront, getActivity());
                                //CameraFunctionality.setBitMapToImageView(imageUri.toString(), bitmapImage, imgIcFront, getActivity());
                                onImagesSelected(imageUri, selectedImageView, null, base64);
                                pathDlFront = imageUri;
                                txtIdFrontMessage.setText(R.string.id_front_image_set_message);

                                //   setCroppedImageBitMap(bitmapImage,selectedImageView);
                            } else if (selectedImageView == Constant.ID_PROOF_BACK) {
                                CameraFunctionality.setPhotoToImageView(imageUri.toString(), imgIcBack, getActivity());

                                //  CameraFunctionality.setBitMapToImageView(imageUri.toString(), bitmapImage, imgIcBack, getActivity());
                                onImagesSelected(imageUri, selectedImageView, null, base64);
                                pathDlBack = imageUri;
                                txtIdBackMessage.setText(R.string.id_back_image_set_message);
                                setCroppedImageBitMap(bitmapImage, selectedImageView);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public Uri dispatchTakePictureIntent(int cameraFacing) {
        Uri imageUri = null;

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo;
        try {
            // place where to store camera taken picture
            photo = CameraFunctionality.createTemporaryFile("picture_" + selectedImageView, ".png");
            photo.delete();
        } catch (Exception e) {
            Log.v(TAG, "Can't create file to take picture!");
            //Toast.makeText(getActivity(), "Please check SD card! Image shot is impossible!", 10000);
            return imageUri;
        }
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        //start camera intent
        startActivityForResult(intent, Constant.FRAGMENT_CONST_REQUEST_IMAGE_CAPTURE);
        return imageUri;
    }

    private void cropPictureIntent(Uri picUri) {
        try {

            Intent cropIntent = new Intent("com.android.camera.action.CROP");

            cropIntent.setDataAndType(picUri, "image/*");
            // cropIntent.setData(picUri);
            cropIntent.putExtra("crop", "true");
           /* cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);*/
            cropIntent.putExtra("outputX", 300);
            cropIntent.putExtra("outputY", 300);

            cropIntent.putExtra("return-data", true);
            startActivityForResult(cropIntent, Constant.CROP_IMAGE_ACTIVITY);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            /*Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();*/
        }
    }


}
