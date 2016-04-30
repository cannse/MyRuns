package com.example.edreichua.myruns2;

/**
 * Created by edreichua on 4/22/16.
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.widget.EditText;

public class MyRunsDialogFragment extends DialogFragment {

    private static final String DIALOG_ID_KEY = "which_dialog";

    // List of dialog IDs
    public static final int PHOTO_PICKER_ID = 0;
    public static final int DURATION_PICKER_ID = 1;
    public static final int DISTANCE_PICKER_ID = 2;
    public static final int CALORIES_PICKER_ID = 3;
    public static final int HEARTRATE_PICKER_ID = 4;
    public static final int COMMENT_PICKER_ID = 5;

    // List of options for photo picker dialog
    public static final int SELECT_FROM_CAMERA = 0;
    public static final int SELECT_FROM_GALLERY = 1;

    /**
     * Create a new instance of a dialog fragment
     * @param dialog_id
     * @return
     */
    public static MyRunsDialogFragment newInstance(int dialog_id) {

        MyRunsDialogFragment dialogFrag = new MyRunsDialogFragment();
        Bundle inputBundle = new Bundle();
        inputBundle.putInt(DIALOG_ID_KEY, dialog_id);
        dialogFrag.setArguments(inputBundle);

        return dialogFrag;
    }

    /**
     * Create dialog fragment
     * @param savedInstanceState
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Activity parent = getActivity();
        int dialog_id = getArguments().getInt(DIALOG_ID_KEY);

        // Create dialog builder to choose between camera and gallery
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(parent);

        // Create OnClickListener based on the type of dialog fragment
        switch (dialog_id) {

            case PHOTO_PICKER_ID:
                buildPhotoPickerDialog(dialogBuilder,parent);
                return dialogBuilder.create();

            case DURATION_PICKER_ID:
                buildEditPickerDialog(dialogBuilder, parent, getString(R.string.ui_manual_duration), dialog_id);
                return dialogBuilder.create();

            case DISTANCE_PICKER_ID:
                buildEditPickerDialog(dialogBuilder, parent, getString(R.string.ui_manual_distance),dialog_id);
                return dialogBuilder.create();

            case CALORIES_PICKER_ID:
                buildEditPickerDialog(dialogBuilder, parent, getString(R.string.ui_manual_calories),dialog_id);
                return dialogBuilder.create();

            case HEARTRATE_PICKER_ID:
                buildEditPickerDialog(dialogBuilder, parent, getString(R.string.ui_manual_heartrate),dialog_id);
                return dialogBuilder.create();

            case COMMENT_PICKER_ID:
                buildEditPickerDialog(dialogBuilder, parent, getString(R.string.ui_manual_comment), dialog_id);
                return dialogBuilder.create();

            default:
                return null;
        }
    }


    /////////////////////// Helper Functions for building dialogs ///////////////////////

    /**
     * build the photo picker dialog
     * @param dialogBuilder
     * @param parent
     */
    private void buildPhotoPickerDialog(AlertDialog.Builder dialogBuilder, final Activity parent){
        dialogBuilder.setTitle(R.string.ui_photo_picker);

        // Create listener
        DialogInterface.OnClickListener photoPickerlistener =
                new DialogInterface.OnClickListener() {

                    @Override // Override the onClick method
                    public void onClick(DialogInterface dialog, int item) {
                        ((UserProfileActivity) parent).selectPhotoPickerItem(item);
                    }
                };

        // Create the dialog
        dialogBuilder.setItems(R.array.ui_photo_picker_list, photoPickerlistener);
    }

    /**
     * build the edit number or text picker dialog
     * @param dialogBuilder
     * @param parent
     */
    private void buildEditPickerDialog(final AlertDialog.Builder dialogBuilder, final Activity parent,
                                       final String title, int id){
        dialogBuilder.setTitle(title);

        // Use an EditText view to get user input
        final EditText input = new EditText(parent);

        if(id == DISTANCE_PICKER_ID) {
            input.setInputType(InputType.TYPE_CLASS_NUMBER |
                    InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        }else if (id == COMMENT_PICKER_ID) {
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setHint(R.string.ui_manual_comment_hint);
            input.setLines(4);
        }else{
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        dialogBuilder.setView(input);

        dialogBuilder.setPositiveButton(R.string.ui_ok_button, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int btnid) {
                String value = input.getText().toString();

                switch (title) {
                    case "Duration":
                        int valueInSeconds = Integer.parseInt(value)*60;
                        ((ManualEntryActivity) parent).entry.
                                setmDuration(!value.equals("") ? valueInSeconds : 0);
                        break;
                    case "Distance":
                        double val = Double.parseDouble(value);

                        // Find unit
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String unitPref = pref.getString(getString(R.string.unit_preference),
                                getString(R.string.unit_km));

                        if (unitPref.equals("Miles")) {
                            val *= ManualEntryActivity.MILES2KM;
                        }
                        ((ManualEntryActivity) parent).entry.
                                setmDistance(!value.equals("") ? val : 0);
                        break;
                    case "Calories":
                        ((ManualEntryActivity) parent).entry.
                                setmCalorie(!value.equals("") ? Integer.parseInt(value) : 0);
                        break;
                    case "Heart Rate":
                        ((ManualEntryActivity) parent).entry.
                                setmHeartRate(!value.equals("") ? Integer.parseInt(value) : 0);
                        break;
                    case "Comment":
                        ((ManualEntryActivity) parent).entry.setmComment(value);
                        break;
                }
                return;
            }
        });

        dialogBuilder.setNegativeButton(R.string.ui_cancel_button, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int btnid) {
                return;
            }
        });
    }
}