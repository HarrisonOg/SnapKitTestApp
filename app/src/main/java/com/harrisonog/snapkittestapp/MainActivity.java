package com.harrisonog.snapkittestapp;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.snapchat.kit.sdk.SnapCreative;
import com.snapchat.kit.sdk.creative.api.SnapCreativeKitApi;
import com.snapchat.kit.sdk.creative.exceptions.SnapMediaSizeException;
import com.snapchat.kit.sdk.creative.media.SnapMediaFactory;
import com.snapchat.kit.sdk.creative.media.SnapPhotoFile;
import com.snapchat.kit.sdk.creative.models.SnapPhotoContent;

import java.io.File;
import java.util.List;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    SnapCreativeKitApi snapCreativeKitApi;
    boolean photoPicker;

    private static final int WRITE_STORAGE_PERM = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EasyImage.configuration(this)
                .setAllowMultiplePickInGallery(false);

        snapCreativeKitApi = SnapCreative.getApi(this);

        photoPicker = false;
        Button sendButton = findViewById(R.id.sendPhotoButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPhotoSend();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(photoPicker) {
            EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
                @Override
                public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                    photoPicker = false;
                    //Some error handling
                }

                @Override
                public void onImagesPicked(List<File> imagesFiles, EasyImage.ImageSource source, int type) {
                    //Handle the images
                    photoPicker = false;
                    onPhotosReturned(imagesFiles);
                }
            });
        }
    }

    private void onPhotosReturned(List<File> imageFiles) {
        Log.d("photo Returned", "Photo returned");
        SnapMediaFactory snapMediaFactory = SnapCreative.getMediaFactory(this);
        if(imageFiles.size() == 1) {
            SnapPhotoFile photoFile;
            try {
                photoFile = snapMediaFactory.getSnapPhotoFromFile(imageFiles.get(0));
            } catch (SnapMediaSizeException e) {
                e.printStackTrace();
                return;
            }
            SnapPhotoContent snapPhotoContent = new SnapPhotoContent(photoFile);
            snapPhotoContent.setCaptionText("Sent from My Android App");
            snapPhotoContent.setAttachmentUrl("www.google.com");

            snapCreativeKitApi.send(snapPhotoContent);
            Log.d("Snapchat", "Photo sent to snapchat");
        }
    }

    private void startPhotoSend() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(EasyPermissions.hasPermissions(this, perms)) {
            //has permissions
            EasyImage.openGallery(MainActivity.this, 0);
            photoPicker = true;
        } else {
            //does not have permissions
            EasyPermissions.requestPermissions(this, getString(R.string.app_permission_text), WRITE_STORAGE_PERM, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
