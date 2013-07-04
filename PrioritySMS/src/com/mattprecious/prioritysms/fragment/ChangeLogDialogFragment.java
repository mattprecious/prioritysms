package com.mattprecious.prioritysms.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.mattprecious.prioritysms.R;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Scanner;

public class ChangeLogDialogFragment extends SherlockDialogFragment {
    private static final String TAG = ChangeLogDialogFragment.class.getSimpleName();
    private static final String ENCODING = "UTF-8";
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.change_log_title);

        InputStream changelogStream = getResources().openRawResource(R.raw.changelog);
        Scanner s = new Scanner(changelogStream, ENCODING).useDelimiter("\\A");
        String changelogHtml = s.hasNext() ? s.next() : "";

        try {
            changelogHtml = URLEncoder.encode(changelogHtml, ENCODING).replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "failed to encode change log html", e);
        }

        WebView webView = new WebView(getActivity());
        webView.loadData(changelogHtml, "text/html", ENCODING);
        builder.setView(webView);

        builder.setNeutralButton(R.string.change_log_close, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }
}
