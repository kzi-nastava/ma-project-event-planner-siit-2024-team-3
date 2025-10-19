package com.example.eveant.event.eventDetails.utils;

import android.content.Context;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;

import androidx.annotation.Nullable;

import com.example.eveant.event.Event;
import com.example.eveant.event.agenda.Activity;

import java.util.*;

public class PdfUtils {

    public static byte[] buildEventPdfBytes(Event e, @Nullable Bitmap coverBmp, List<Activity> agenda, Context ctx, android.view.View root) {
        final int pageWidth  = 612; // 8.5" * 72
        final int pageHeight = 792; // 11"  * 72
        final int margin = 36;      // 0.5"
        final int contentWidth = pageWidth - margin * 2;

        PdfDocument pdf = new PdfDocument();

        // Paints
        Paint titlePaint   = new Paint(Paint.ANTI_ALIAS_FLAG); titlePaint.setColor(Color.BLACK); titlePaint.setTextSize(18f); titlePaint.setFakeBoldText(true);
        Paint sectionPaint = new Paint(titlePaint); sectionPaint.setTextSize(14f);
        Paint labelPaint   = new Paint(Paint.ANTI_ALIAS_FLAG); labelPaint.setColor(Color.rgb(80,80,80)); labelPaint.setTextSize(11f);
        Paint valuePaint   = new Paint(Paint.ANTI_ALIAS_FLAG); valuePaint.setColor(Color.BLACK); valuePaint.setTextSize(12f);
        Paint monoPaint    = new Paint(valuePaint); monoPaint.setTypeface(Typeface.MONOSPACE);
        Paint linePaint    = new Paint(); linePaint.setColor(Color.LTGRAY); linePaint.setStrokeWidth(1f);

        int pageNo = 1;
        PdfDocument.Page currentPage = pdf.startPage(new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNo).create());
        Canvas canvas = currentPage.getCanvas();
        int x = margin;
        int y = margin + dp(ctx, 6);

        // ===== HEADER =====
        y += drawMultilineText(canvas, e.getName() == null ? "Event" : e.getName(), x, y, contentWidth, titlePaint) + dp(ctx, 6);

        if (coverBmp != null && coverBmp.getWidth() > 0 && coverBmp.getHeight() > 0) {
            int w = contentWidth;
            int h = (int) (w * (coverBmp.getHeight() / (float) coverBmp.getWidth()));
            Bitmap scaled = Bitmap.createScaledBitmap(coverBmp, w, Math.max(1, h), true);
            canvas.drawBitmap(scaled, x, y, null);
            y += h + dp(ctx, 10);
        }

        canvas.drawLine(x, y, x + contentWidth, y, linePaint); y += dp(ctx, 12);

        // ===== QUICK INFO =====
        String type = (e.getEventType()!=null && e.getEventType().getName()!=null) ? e.getEventType().getName() : "—";
        String date = "—", time = "—";
        if (e.getDate()!=null && !e.getDate().isEmpty()) {
            int tPos = Math.max(e.getDate().indexOf('T'), e.getDate().indexOf(' '));
            if (tPos > 0) {
                date = e.getDate().substring(0, tPos);
                time = e.getDate().substring(tPos + 1);
                if (time.length() >= 5) time = time.substring(0, 5);
            } else date = e.getDate();
        }
        String guests = (e.getMaxAttendance()==null) ? "—" : String.valueOf(e.getMaxAttendance());
        String status = (e.getStatus()==null) ? "—" : e.getStatus().name();
        String address = "";
        if (e.getAddress()!=null) {
            address = Str.join(", ",
                    Str.nz(e.getAddress().getStreet()),
                    Str.nz(e.getAddress().getHouseNumber()),
                    Str.nz(e.getAddress().getCity()),
                    Str.nz(e.getAddress().getCountry()));
        }

        y = drawLabelValue(canvas, x, y, "Type", type, labelPaint, valuePaint, contentWidth, ctx);
        y = drawLabelValue(canvas, x, y, "Date", date, labelPaint, valuePaint, contentWidth, ctx);
        y = drawLabelValue(canvas, x, y, "Time", time, labelPaint, valuePaint, contentWidth, ctx);
        y = drawLabelValue(canvas, x, y, "Guests", guests, labelPaint, valuePaint, contentWidth, ctx);
        y = drawLabelValue(canvas, x, y, "Status", status, labelPaint, valuePaint, contentWidth, ctx);
        if (!address.isEmpty())
            y = drawLabelValue(canvas, x, y, "Address", address, labelPaint, valuePaint, contentWidth, ctx);

        y += dp(ctx, 8);
        canvas.drawLine(x, y, x + contentWidth, y, linePaint);
        y += dp(ctx, 12);

        // ===== DESCRIPTION =====
        if (e.getDescription()!=null && !e.getDescription().trim().isEmpty()) {
            y += drawMultilineText(canvas, "Description", x, y, contentWidth, sectionPaint) + dp(ctx, 6);
            y += drawMultilineText(canvas, e.getDescription().trim(), x, y, contentWidth, valuePaint) + dp(ctx, 8);
        }

        // ===== AGENDA =====
        if (agenda != null && !agenda.isEmpty()) {
            y += drawMultilineText(canvas, "Agenda", x, y, contentWidth, sectionPaint) + dp(ctx, 6);

            List<Activity> sorted = new ArrayList<>(agenda);
            Collections.sort(sorted, (a, b) -> Long.compare(TimeFmt.parseMinutes(a.startTime), TimeFmt.parseMinutes(b.startTime)));

            for (Activity a : sorted) {
                String hhmm = TimeFmt.formatHHmm(a.startTime);
                String line = String.format(Locale.getDefault(), "%s – %s", hhmm, Str.nz(a.name));

                // page break if needed
                if (y + dp(ctx, 24) > pageHeight - margin) {
                    pdf.finishPage(currentPage);
                    pageNo++;
                    currentPage = pdf.startPage(new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNo).create());
                    canvas = currentPage.getCanvas();
                    y = margin + dp(ctx, 6);
                    drawMultilineText(canvas, "Agenda (continued)", x, y, contentWidth, sectionPaint);
                    y += dp(ctx, 10);
                }

                y += drawMultilineText(canvas, line, x, y, contentWidth, monoPaint) + dp(ctx, 2);
            }
        }

        // Finish last page
        pdf.finishPage(currentPage);

        // Write PDF to bytes
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try { pdf.writeTo(bos); } catch (Exception ignore) {}
        finally { pdf.close(); }
        return bos.toByteArray();
    }

    /* --------------------------- Drawing helpers --------------------------- */

    private static int drawLabelValue(Canvas c, int x, int y, String label, String value,
                                      Paint labelPaint, Paint valuePaint, int width, Context ctx) {
        int line = (int)(valuePaint.getTextSize() + dp(ctx, 6));
        c.drawText(label + ":", x, y + valuePaint.getTextSize(), labelPaint);
        int labelWidth = (int) labelPaint.measureText(label + ":  ");
        int used = drawMultilineText(c, value, x + labelWidth + dp(ctx, 4), y, width - labelWidth - dp(ctx, 4), valuePaint);
        return Math.max(y + line, y + used + dp(ctx, 6));
    }

    private static int drawMultilineText(Canvas c, String text, int x, int y, int width, Paint p) {
        if (text == null || text.isEmpty()) return 0;
        int start = 0, totalH = 0;
        float lineH = (float) Math.ceil(p.getTextSize() * 1.4f);
        final int len = text.length();
        while (start < len) {
            int count = p.breakText(text, start, len, true, width, null);
            c.drawText(text, start, start + count, x, y + p.getTextSize() + totalH, p);
            start += count;
            totalH += lineH;
        }
        return (int) totalH;
    }

    private static int dp(Context ctx, int v) {
        return (int) (v * ctx.getResources().getDisplayMetrics().density + 0.5f);
    }
}
