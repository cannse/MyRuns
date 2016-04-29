package com.example.edreichua.myruns2;

/**
 * Created by edreichua on 4/22/16.
 */

import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.app.Activity;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class UserProfileActivity extends Activity{

    private Uri mImageUri;
    private ImageView mImageView;
    public static final int REQUEST_ID_FROM_CAMERA = 0;
    public static final int REQUEST_ID_FROM_GALLERY = 1;
    private static final String IMAGE_UNSPECIFIED = "image/*";
    public static final String PREFS = "Profile_Info";
    protected boolean isFromCamera, isPhotoTaken;


    /////////////////////// Override core functionality ///////////////////////

    /**
     * Handle creating of activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Retrieve the profile photo information
        mImageView = (ImageView) findViewById(R.id.profile_image);
        loadPhoto(getString(R.string.profile_pict_filename));

        // Find tempPhoto path
        File tempPhoto = new File(this.getFilesDir().getAbsolutePath()+
                "/"+getString(R.string.temp_filename));

        if (savedInstanceState != null && tempPhoto.exists() && savedInstanceState.getBoolean("photoTaken")) {
            loadPhoto(getString(R.string.temp_filename));
            isPhotoTaken =  savedInstanceState.getBoolean("photoTaken");
        }

        // Retrieve profile information
        loadUserInfo();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the image capture uri before the activity goes into background
        outState.putBoolean("photoTaken", isPhotoTaken);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Return to activity
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case REQUEST_ID_FROM_CAMERA:

                // Start to crop image
                startCroppingPhoto(mImageUri);
                break;

            case REQUEST_ID_FROM_GALLERY:

                // Start to crop image
                this.mImageUri = data.getData();
                startCroppingPhoto(mImageUri);
                break;

            case Crop.REQUEST_CROP:

                // Update image view with the cropped photo
                updateCroppedPhoto(resultCode, data);

                // Delete temp image after finishing cropping
                if (isFromCamera) {
                    File tempPhoto = new File(mImageUri.getPath());

                    if (tempPhoto.exists())
                        tempPhoto.delete();
                }
                break;
        }
    }

    protected void onDestroy(){
        File tempPhoto = new File(this.getFilesDir().getAbsolutePath()+
                "/"+getString(R.string.temp_filename));

        if (tempPhoto.exists() && isFinishing()) {
            tempPhoto.delete();

            Log.d("Delete tempPhoto path", this.getFilesDir().getAbsolutePath() +
                    "/" + getString(R.string.temp_filename));
        }
        super.onDestroy();
    }

    /////////////////////// Handle Selection of buttons ///////////////////////

    /**
     * Handle the selection of the save button
     * @param v
     */
    public void selectSave(View v) {

        // Save picture
        savePhoto(getString(R.string.profile_pict_filename));

        // Save remaining entry into shared preference
        saveUserInfo();

        // Inform user that the profile information is saved
        Toast.makeText(getApplicationContext(), getString(R.string.ui_toast_save),
                Toast.LENGTH_SHORT).show();

        // Close the activity
        finish();
    }

    /**
     * Handle the selection of the cancel button
     * @param v
     */
    public void selectCancel(View v) {
        // Close the activity
        finish();
    }

    /**
     * Handle the selection of the change button
     * @param v
     */
    public void selectChange(View v) {

        toPhotoPicker(MyRunsDialogFragment.PHOTO_PICKER_ID);
    }


    /////////////////////// Photo picker functionality ///////////////////////

    /**
     * Jump to Photo Picker dialog window
     * @param id
     */
    public void toPhotoPicker(int id) {
        DialogFragment photoPickerWindow = MyRunsDialogFragment.newInstance(id);
        photoPickerWindow.show(getFragmentManager(), getString(R.string.photo_picker_tag));
    }

    /**
     * Handle the selection of item in the Photo Picker dialog
     * @param whichItem
     */
    public void selectPhotoPickerItem(int whichItem) {
        Intent intent;

        mImageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "temp_"
                + String.valueOf(System.currentTimeMillis()) + ".jpg"));

        switch (whichItem) {
            case MyRunsDialogFragment.SELECT_FROM_CAMERA:

                // Take photo using camera
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // Save photo temporarily
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                intent.putExtra("return-data", true);

                try {
                    // Trigger the cropping activity
                    startActivityForResult(intent, REQUEST_ID_FROM_CAMERA);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }

                isFromCamera = true;
                break;

            case MyRunsDialogFragment.SELECT_FROM_GALLERY:

                // Take photo from gallery
                intent = new Intent(Intent.ACTION_PICK);
                intent.setType(IMAGE_UNSPECIFIED);

                // Save photo temporarily
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                intent.putExtra("return-data", true);

                try {
                    // Trigger the cropping activity
                    startActivityForResult(intent, REQUEST_ID_FROM_GALLERY);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                break;

            default:
                return;
        }

    }


    /////////////////////// Helper Functions ///////////////////////

    /**
     * Load profile photo from internal storage
     */
    private void loadPhoto(String path) {

        try {
            // Retrieve photo from internal storage
            FileInputStream rFile = openFileInput(path);
            Bitmap bmap = BitmapFactory.decodeStream(rFile);
            mImageView.setImageBitmap(bmap);
            rFile.close();

        } catch (IOException e) {
            // Set to default profile pict is nothing is stored
            mImageView.setImageResource(R.drawable.default_profile);
        }
    }

    /**
     * Save profile photo into internal storage
     */
    private void savePhoto(String path) {

        mImageView.buildDrawingCache();
        Bitmap bmap = mImageView.getDrawingCache();
        try {
            // Save profile photo into internal storage
            FileOutputStream wFile = openFileOutput(
                    path, MODE_PRIVATE);
            bmap.compress(Bitmap.CompressFormat.PNG, 100, wFile);
            wFile.flush();
            wFile.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Load profile information from shared preference
     */
    private void loadUserInfo(){

        // Retrieve information from shared preference
        SharedPreferences prefs = getSharedPreferences(PREFS, 0);

        EditText textName = (EditText) findViewById(R.id.edit_name);
        textName.setText(prefs.getString("savedName", ""));

        EditText textEmail = (EditText) findViewById(R.id.edit_email);
        textEmail.setText(prefs.getString("savedEmail", ""));

        EditText textPhone = (EditText) findViewById(R.id.edit_phone);
        textPhone.setText(prefs.getString("savedPhone", ""));

        EditText textClass = (EditText) findViewById(R.id.edit_class);
        textClass.setText(prefs.getString("savedClass", ""));

        EditText textMajor = (EditText) findViewById(R.id.edit_major);
        textMajor.setText(prefs.getString("savedMajor", ""));

        RadioButton isFemale = (RadioButton) findViewById(R.id.radioButton_female);
        isFemale.setChecked(prefs.getBoolean("savedFemale", false));

        RadioButton isMale = (RadioButton) findViewById(R.id.radioButton_male);
        isMale.setChecked(prefs.getBoolean("savedMale", false));
    }

    /**
     * Save profile information into shared preference
     */
    private void saveUserInfo(){

        // Save remaining entry into shared preference
        SharedPreferences prefs = getSharedPreferences(PREFS, 0);
        final SharedPreferences.Editor edit = prefs.edit();

        EditText textName = (EditText) findViewById(R.id.edit_name);
        edit.putString("savedName", textName.getText().toString());

        EditText textEmail = (EditText) findViewById(R.id.edit_email);
        edit.putString("savedEmail", textEmail.getText().toString());

        EditText textPhone = (EditText) findViewById(R.id.edit_phone);
        edit.putString("savedPhone", textPhone.getText().toString());

        EditText textClass = (EditText) findViewById(R.id.edit_class);
        edit.putString("savedClass", textClass.getText().toString());

        EditText textMajor = (EditText) findViewById(R.id.edit_major);
        edit.putString("savedMajor", textMajor.getText().toString());

        RadioButton isFemale = (RadioButton) findViewById(R.id.radioButton_female);
        edit.putBoolean("savedFemale", isFemale.isChecked());

        RadioButton isMale = (RadioButton) findViewById(R.id.radioButton_male);
        edit.putBoolean("savedMale", isMale.isChecked());

        // Commit change into shared preference
        edit.commit();
    }

    /**
     * Start cropping photo, either from camera or gallery
     * @param source
     */
    private void startCroppingPhoto(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped_photo"));
        Crop.of(source, destination).asSquare().start(this);
    }

    /**
     * Update image view with the cropped photo
     * @param resultCode
     * @param result
     */
    private void updateCroppedPhoto(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {

            mImageView.setImageURI(null);
            mImageView.setImageURI(Crop.getOutput(result));
            savePhoto(getString(R.string.temp_filename));
            isPhotoTaken = true;

        } else if (resultCode == Crop.RESULT_ERROR) {

            Toast.makeText(this, getString(R.string.ui_toast_error), Toast.LENGTH_SHORT).show();
        }
    }
}
