package com.example.eveant.event.eventDetails;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;

import android.view.View;
import android.widget.ImageView;

import com.example.eveant.R;
import com.example.eveant.event.Event;
import com.example.eveant.event.agenda.Activity;
import com.example.eveant.event.eventDetails.utils.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

class PdfExporter {

    private final Context ctx;
    private final View root;

    PdfExporter(Context ctx, View root) {
        this.ctx = ctx; this.root = root;
    }

    void export(Event event, List<Activity> agenda) {
        Bitmap cover = tryGetHeaderBitmap();
        byte[] bytes = PdfUtils.buildEventPdfBytes(event, cover, agenda, ctx, root);
        if (bytes == null || bytes.length == 0) {
            Ui.toast(ctx, "Failed to build PDF");
            return;
        }
        String filename = Str.safeFileName((event.getName()==null||event.getName().isEmpty())? "event" : event.getName()) + ".pdf";
        try {
            String path = savePdfToDownloads(bytes, filename);
            Ui.toast(ctx, "Saved: " + path);
        } catch (Exception ex) {
            Ui.toast(ctx, "Save failed: " + ex.getMessage());
        }
    }

    private Bitmap tryGetHeaderBitmap() {
        ImageView iv = root.findViewById(R.id.ivPhoto);
        try {
            if (iv != null && iv.getDrawable() instanceof BitmapDrawable) {
                return ((BitmapDrawable) iv.getDrawable()).getBitmap();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String savePdfToDownloads(byte[] data, String displayName) throws Exception {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            ContentResolver cr = ctx.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, displayName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Downloads.IS_PENDING, 1);
            Uri uri = cr.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) throw new IllegalStateException("Insert failed");
            try (OutputStream os = cr.openOutputStream(uri)) {
                if (os == null) throw new IllegalStateException("Cannot open stream");
                os.write(data);
            }
            values.clear();
            values.put(MediaStore.Downloads.IS_PENDING, 0);
            cr.update(uri, values, null, null);
            return "Downloads/" + displayName;
        } else {
            File dir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
            if (!dir.exists()) dir.mkdirs();
            File out = new File(dir, displayName);
            try (FileOutputStream fos = new FileOutputStream(out)) { fos.write(data); }
            ctx.sendBroadcast(new android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(out)));
            return out.getAbsolutePath();
        }
    }
}
