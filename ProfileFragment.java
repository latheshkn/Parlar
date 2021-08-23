package com.designurway.idlidosa.ui.home_page.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.designurway.idlidosa.R;
import com.designurway.idlidosa.a.activity.RegisterActivity;
import com.designurway.idlidosa.a.model.ErrorMessageModel;
import com.designurway.idlidosa.a.model.ProfileDataModel;
import com.designurway.idlidosa.a.model.ProfileModel;
import com.designurway.idlidosa.a.model.StatusAndMessageModel;
import com.designurway.idlidosa.a.retrofit.BaseClient;
import com.designurway.idlidosa.a.retrofit.RetrofitApi;
import com.designurway.idlidosa.a.utils.AndroidUtils;
import com.designurway.idlidosa.a.utils.PreferenceManager;
import com.designurway.idlidosa.a.utils.UtilConstant;
import com.designurway.idlidosa.databinding.FragmentProfileBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.codec.Base64;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;
    private static final String TAG = "ProfileFragment";
    EditText emailFieldEt,phoneNumFieldEt,nameFieldEt,addressFieldEt;
    Button saveBtn;
    NavDirections action;
    CircleImageView ivCamera,personPicImgv;
    Context context;
    File file;
    String path;
    Uri imageUri;
    Bitmap bitmap;
    String email,phoneNum,address,name,pincode;
    ProgressBar progressProfile;
    ConstraintLayout CostraintProfile;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        context = binding.profileAddressEt.getContext();
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameFieldEt = binding.profileNameEt;
        emailFieldEt = binding.profileEmailEt;
        phoneNumFieldEt = binding.profilePhoneEt;
        addressFieldEt = binding.profileAddressEt;
        personPicImgv = binding.profileImg;
        ivCamera = binding.otpImg;
        saveBtn = binding.saveProfileBtn;
        progressProfile = binding.progressProfile;
        CostraintProfile = binding.CostraintProfile;

        phoneNumFieldEt.setText(PreferenceManager.getCustomerPhone());

        setProfileDetails();


        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 102);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                phoneNumFieldEt.setEnabled(false);
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
//                Toast.makeText(getContext(), "Save Bttn", Toast.LENGTH_SHORT).show();
                name = nameFieldEt.getText().toString().trim();
                email = emailFieldEt.getText().toString().trim();
                address = addressFieldEt.getText().toString().trim();
                phoneNum = phoneNumFieldEt.getText().toString().trim();

                if (name.isEmpty()){
                    nameFieldEt.setFocusable(true);
                    nameFieldEt.setError("reqruire");
                    return;
                }
                if (!email.matches(emailPattern) ||email.isEmpty()){

                    emailFieldEt.setFocusable(true);
                    emailFieldEt.setError("Enter valid email");
                    return;
                }
                  if (address.isEmpty()){
                      addressFieldEt.setFocusable(true);
                      addressFieldEt.setError("reqruire");
                    return;
                }
                 if (phoneNum.isEmpty()){
                     phoneNumFieldEt.setFocusable(true);
                     phoneNumFieldEt.setError("reqruire");
                    return;
                }

                if (bitmap!=null){
                    saveProfileImage(bitmap);
                    updateProfile(name,email,phoneNum,address);
                }else {
                    updateProfile(name,email,phoneNum,address);
                }
            }
        });

    }

    private void setProfileDetails() {

        RetrofitApi retrofitApi = BaseClient.getClient().create(RetrofitApi.class);
        Call<ProfileDataModel> call = retrofitApi.getProfileDetails(PreferenceManager.getCustomerPhone());
        call.enqueue(new Callback<ProfileDataModel>() {
            @Override
            public void onResponse(Call<ProfileDataModel> call, Response<ProfileDataModel> response) {
                if (response.isSuccessful()) {
                    progressProfile.setVisibility(View.INVISIBLE);
                    CostraintProfile.setVisibility(View.VISIBLE);
                    ProfileDataModel dataModel = response.body();
                    ProfileModel model = dataModel.getData();
                     email = model.getEmail();
                    emailFieldEt.setText(email);
                     phoneNum = model.getPhone();
                    phoneNumFieldEt.setText(phoneNum);
                     address = model.getHomeAddress();
                    addressFieldEt.setText(address);
                     name = model.getName();
                    nameFieldEt.setText(name);

                    pincode = model.getPin_code();
                    if (model.getProfileImage().isEmpty()) {
//                        Picasso.with(getContext()).load("http://192.168.4.168/API/idli_dosa/profile/user_profile.png").into(profileImageCiv);
                    } else {
                        progressProfile.setVisibility(View.INVISIBLE);
                        CostraintProfile.setVisibility(View.VISIBLE);
                        Picasso.get().load(model.getProfileImage()).into(personPicImgv);
                    }
                } else {
                    progressProfile.setVisibility(View.INVISIBLE);
                    CostraintProfile.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "no data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileDataModel> call, Throwable t) {
                Log.d(TAG, "onFailure" + t.getMessage());

            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 102 && resultCode == RESULT_OK) {

            path = data.getData().toString();
            imageUri = data.getData();

            file = new File(path);

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),imageUri);
                personPicImgv.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }


    private void saveProfileImage(Bitmap bitmap) {

        Bitmap image = bitmap;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] imageinbyte = byteArrayOutputStream.toByteArray();
        String imageInString = Base64.encodeBytes(imageinbyte, android.util.Base64.DEFAULT);

        Log.d("imageInString", imageInString);

        if (!imageInString.isEmpty()) {
            RetrofitApi retrofitApi = BaseClient.getClient().create(RetrofitApi.class);
            Call<ErrorMessageModel> call = retrofitApi.postImage(PreferenceManager.getCustomerPhone(),
                    AndroidUtils.randomName(6), imageInString);

            call.enqueue(new Callback<ErrorMessageModel>() {
                @Override
                public void onResponse(Call<ErrorMessageModel> call, Response<ErrorMessageModel> response) {
                    if (response.isSuccessful()) {
//
//                        action = ProfileFragmentDirections.actionProfileFragment4ToHomeFragment();
//                        Navigation.findNavController(getView()).navigate(action);


                    } else {
                        Toast.makeText(context, "fail", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ErrorMessageModel> call, Throwable t) {

                    Toast.makeText(context, "On Failure "+t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateProfile(String name, String email, String phone, String address) {
        RetrofitApi retrofitApi = BaseClient.getClient().create(RetrofitApi.class);
        Call<ProfileModel> call = retrofitApi.postProfile(name, email, phone, address,pincode,PreferenceManager.getCustomeReferenceCode());
        call.enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(Call<ProfileModel> call, Response<ProfileModel> response) {
                if (response.isSuccessful()) {
                    ProfileModel model = response.body();
                    String name = model.getName();
                    String email = model.getEmail();
                    String phone = model.getPhone();
                    String id = model.getId();
                    String code = model.getReferralCode();
                    String pwd = model.getPassword();
                    String refCode = model.getReferredFrom();
                    Log.d(TAG, "referred" + refCode);

                    PreferenceManager.saveCustomer(id, name, email, phone, pwd, code);
                    insertRefCode();

                        action = ProfileFragmentDirections.actionProfileFragment4ToHomeFragment();
                        Navigation.findNavController(getView()).navigate(action);



                } else {

                    Log.d(TAG, "no Data");
                }
            }

            @Override
            public void onFailure(Call<ProfileModel> call, Throwable t) {
                Log.d(TAG, "onFailure" + t.getMessage());

            }
        });
    }

    public void insertRefCode() {
        RetrofitApi retrofitApi = BaseClient.getClient().create(RetrofitApi.class);
        Call<StatusAndMessageModel> call = retrofitApi.updateReferralCode(PreferenceManager.getReferred_from(),
                PreferenceManager.getCustomeReferenceCode());
        Log.d("referredfrom",PreferenceManager.getReferred_from());
        call.enqueue(new Callback<StatusAndMessageModel>() {
            @Override
            public void onResponse(Call<StatusAndMessageModel> call,
                                   Response<StatusAndMessageModel> response) {
                if (response.isSuccessful()) {
//                    Toast.makeText(RegisterActivity.this, "Success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Failed",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StatusAndMessageModel> call, Throwable t) {
                Log.d(TAG, "onFailure" + t.getMessage());

            }
        });
    }



}