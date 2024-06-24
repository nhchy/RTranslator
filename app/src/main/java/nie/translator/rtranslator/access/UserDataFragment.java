/*
 * Copyright 2016 Luca Martino.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copyFile of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nie.translator.rtranslator.access;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.gallery.imageselector.GalleryImageSelector;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

import nie.translator.rtranslator.Global;
import nie.translator.rtranslator.R;
import nie.translator.rtranslator.bluetooth.tools.BluetoothTools;

import android.content.SharedPreferences;
import android.content.Context;
import java.io.File;
import nie.translator.rtranslator.tools.FileTools;

public class UserDataFragment extends Fragment {
    private ImageView imageView;
    private Button buttonConfirm;
    private EditText inputName;
    private TextInputLayout inputNameLayout;
    private CheckBox privacyTerms;
    //private CheckBox ageTerms;
    private AccessActivity activity;
    private Global global;
    private GalleryImageSelector userImageContainer;
    
    public static final String[] DOWNLOAD_NAMES = {
            "NLLB_cache_initializer.onnx",
            "NLLB_decoder.onnx",
            "NLLB_embed_and_lm_head.onnx",
            "NLLB_encoder.onnx",
            "Whisper_cache_initializer.onnx",
            "Whisper_cache_initializer_batch.onnx",
            "Whisper_decoder.onnx",
            "Whisper_detokenizer.onnx",
            "Whisper_encoder.onnx",
            "Whisper_initializer.onnx"
    };

    public UserDataFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageView = view.findViewById(R.id.user_image_initialization);
        buttonConfirm = view.findViewById(R.id.buttonConfirm);
        inputName = view.findViewById(R.id.input_name);
        inputNameLayout = view.findViewById(R.id.input_name_layout);
        //this.ageTerms = view.findViewById(R.id.checkBoxAge);
        this.privacyTerms = view.findViewById(R.id.checkBoxPrivacy);
        this.privacyTerms.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (AccessActivity) requireActivity();
        global = (Global) activity.getApplication();
        userImageContainer = new GalleryImageSelector(imageView, activity, this, R.drawable.user_icon, "com.gallery.RTranslator.2.0.provider");

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputNameLayout.setErrorEnabled(false);
                inputNameLayout.setErrorEnabled(true);
                boolean error = false;
                String name = inputName.getText().toString();
                if (name.length() == 0) {
                    error = true;
                    inputNameLayout.setError(getResources().getString(R.string.error_missing_username));
                } else {
                    //compatibily check for supported characters
                    ArrayList<Character> supportedCharacters = BluetoothTools.getSupportedUTFCharacters(global);
                    boolean equals = true;
                    for (int i = 0; i < name.length() && equals; i++) {
                        if (!supportedCharacters.contains(Character.valueOf(name.charAt(i)))) {
                            equals = false;
                        }
                    }

                    if (!equals) {
                        error = true;
                        inputNameLayout.setError(getResources().getString(R.string.error_wrong_username) + BluetoothTools.getSupportedNameCharactersString(global));
                    }
                }
                /*if (!ageTerms.isChecked() && !error) {
                    error = true;
                    showAgeTermsError();
                }*/
                if (privacyTerms.isChecked() && !error) {
                    //error = true;
                    //showPrivacyTermsError();
                    importLocalData();
                }
                if (!error) {
                    //save name
                    global.setName(inputName.getText().toString());
                    //save image
                    userImageContainer.saveImage();

                    /*//modification of the firstStart
                    global.setFirstStart(false);
                    //start activity
                    Intent intent = new Intent(activity, VoiceTranslationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                    activity.finish();*/
                    activity.startFragment(AccessActivity.DOWNLOAD_FRAGMENT, null);
                }
            }
        });
    }

    private void importLocalData() {
	    moveDataFile();
        //editor = sharedPreferences.edit();
        //editor.putLong("currentDownloadId", -2);
        //editor.apply();
	}
    private void moveDataFile(){
        SharedPreferences sharedPreferences = global.getSharedPreferences("default", Context.MODE_PRIVATE);
//        String lastTransferFailure = sharedPreferences.getString("lastTransferFailure", "");
//        if(lastTransferFailure.length()>0){
            //we find the index of the lastDownloadSuccess
        int nameIndex = -1;
        for (int i = 0; i < DOWNLOAD_NAMES.length; i++) {
	        nameIndex=i;
                //we restart the transfer
                File from = new File(global.getExternalFilesDir(null) + "/" + DOWNLOAD_NAMES[nameIndex]);
                File to = new File(global.getFilesDir() + "/" + DOWNLOAD_NAMES[nameIndex]);
                int finalNameIndex = nameIndex;
                FileTools.moveFile(from, to, new FileTools.MoveFileCallback() {
                    @Override
                    public void onSuccess() {
                        //we save the success of the transfer
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("lastTransferSuccess", DOWNLOAD_NAMES[finalNameIndex]);
                        editor.apply();

                        if (finalNameIndex < (DOWNLOAD_NAMES.length - 1)) {  //if the download done is not the last one
                            //we start the next download
                            //long newDownloadId = downloader.downloadModel(DownloadFragment.DOWNLOAD_URLS[finalNameIndex + 1], DownloadFragment.DOWNLOAD_NAMES[finalNameIndex + 1]);
                            //editor = sharedPreferences.edit();
                            //editor.putLong("currentDownloadId", newDownloadId);
                            //editor.apply();
                        } else {
                            //we notify the completion of the download of all models
                            editor = sharedPreferences.edit();
                            editor.putLong("currentDownloadId", -2);
                            editor.apply();

                            //startRTranslator();
                        }
                    }

                    @Override
                    public void onFailure() {
                        //we save the failure of the transfer
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("lastTransferFailure", DOWNLOAD_NAMES[finalNameIndex]);
                        editor.apply();
                    }
                });
    	}//for
    }

    //private void startRTranslator(){
    //    if (activity != null) {
    //        //modification of the firstStart
    //        global.setFirstStart(false);
    //        //start activity
    //        Intent intent = new Intent(activity, LoadingActivity.class);
    //        intent.putExtra("activity", "download");
    //        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    //        activity.startActivity(intent);
    //        activity.finish();
    //    }
    //}
    	
    private void showAgeTermsError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);
        builder.setMessage(R.string.error_age_unchecked);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.create().show();
    }

    private void showPrivacyTermsError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);
        builder.setMessage(R.string.error_privacy_unchecked);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        userImageContainer.onActivityResult(requestCode, resultCode, data, false);
    }
}
