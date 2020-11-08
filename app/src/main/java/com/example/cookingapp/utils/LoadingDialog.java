package com.example.cookingapp.utils;

import android.app.Activity;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;

import com.example.cookingapp.R;

public class LoadingDialog {
    private Activity activity;
    private AlertDialog alertDialog;

    public LoadingDialog(Activity activity) {
        this.activity = activity;
    }
    public void startLoadindDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_progress,null));
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.show();
    }
   public void dissLoadingDialog(){
        alertDialog.dismiss();
    }
}
