package com.leenita.sindbad.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.leenita.sindbad.R;

public class DiagBuyNoCredits extends DialogFragment{
    View view;

    public enum DIAG_TYPE {NO_CREDITS_OFFER, NO_CREDITS_PRODUCT}
    DIAG_TYPE type;
    String title;

    public static DiagBuyNoCredits newInstance(DIAG_TYPE type, String title){
        if(title == null)
            title = "";

        DiagBuyNoCredits diag = new DiagBuyNoCredits();
        Bundle extras = new Bundle();
            extras.putInt("type", type.ordinal());
            extras.putString("title", title);
        diag.setArguments(extras);
        return diag;
    }

    @NonNull
    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        String itemName = getArguments().getString("title");
        title = String.format(getString(R.string.diag_buy_title), itemName);
        type = DIAG_TYPE.values()[getArguments().getInt("type")];
        String msg;
        if(type == DIAG_TYPE.NO_CREDITS_OFFER)
           msg = getString(R.string.diag_buy_no_credits_offer);
        else
            msg = getString(R.string.diag_buy_no_credits_prod);

        view = getActivity().getLayoutInflater().inflate(R.layout.diag_buy_no_creits, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSellers();
            }
        });

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle((title!= null && !title.isEmpty())?title:getString(R.string.place_holder));
        dialogBuilder.setMessage(msg);
        dialogBuilder.setPositiveButton(getString(R.string.diag_buy_accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showSellers();
            }
        });
        dialogBuilder.setNegativeButton(getString(R.string.diag_buy_decline), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });
        dialogBuilder.setView(view);
        AlertDialog diag = dialogBuilder.create();


        return diag;
    }

    //TODO show sellers on map view
    private void showSellers()
    {

    }
}
