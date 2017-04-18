package com.leenita.sindbad.fragments;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.leenita.sindbad.R;
import com.leenita.sindbad.SindbadApp;

/**
 * displays the QR code of the purchased offer
 */
public class DiagQrCode extends DialogFragment {
    View view;
    ImageView ivCode;
    TextView tvDone;

    String codeContent, title;
    private final static int BLACK = 0xFF000000;
    private final static int WHITE = 0xFFFFFFFF;
    private final static int WIDTH = SindbadApp.getPXSize(250);

    public static DiagQrCode newInstance(String qrCodeContent, String title){
        DiagQrCode diag = new DiagQrCode();
        Bundle extras = new Bundle();
        if(qrCodeContent != null) {
            extras.putString("content", qrCodeContent);
            extras.putString("title", title);
        }
        diag.setArguments(extras);
        return diag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        title = getArguments().getString("title");

        view = getActivity().getLayoutInflater().inflate(R.layout.diag_qr_code, null);
        Dialog diag = new Dialog(getActivity());
        diag.setContentView(view);
        diag.setTitle((title!= null && !title.isEmpty())?title:getString(R.string.place_holder));
        init();
        return diag;
    }

    private void init() {
        ivCode = (ImageView) view.findViewById(R.id.ivCode);
        tvDone = (TextView) view.findViewById(R.id.tvDone);

        codeContent = getArguments().getString("content");
        generateQrCode(codeContent);
        tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });


    }

    private void generateQrCode(String codeContent) {
        Bitmap bitmap = null;
        try {
            bitmap = encodeAsBitmap(codeContent);
            if(bitmap != null)
                ivCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

    }

    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, WIDTH, WIDTH, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }
}
