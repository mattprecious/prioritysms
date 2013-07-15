package com.mattprecious.prioritysms.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public final class Helpers {
    private Helpers() {
    }

    public static void openSupportPage(Context context) {
        Uri supportUri = Uri.parse("https://prioritysms.uservoice.com");
        Intent intent = new Intent(Intent.ACTION_VIEW, supportUri);
        context.startActivity(intent);
    }

    public static void openTranslatePage(Context context) {
        Uri supportUri = Uri.parse("https://crowdin.net/project/priority-sms");
        Intent intent = new Intent(Intent.ACTION_VIEW, supportUri);
        context.startActivity(intent);
    }
}
