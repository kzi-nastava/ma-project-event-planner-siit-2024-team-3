package com.example.eveant.admin;

import android.content.Context;
import android.graphics.*;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

public class BarChartView extends View {

    private String title = "";
    private String[] labels = new String[0];
    private int[] values = new int[0];

    private final Paint axis = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bar = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint txt = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float labelAngleDeg = 45f; // rotate X labels so more fit

    public BarChartView(Context c) { super(c); init(); }
    public BarChartView(Context c, AttributeSet a) { super(c, a); init(); }
    public BarChartView(Context c, AttributeSet a, int s) { super(c, a, s); init(); }

    private void init() {
        axis.setColor(Color.BLACK);
        axis.setStrokeWidth(3f);
        bar.setColor(Color.rgb(128,148,219));
        txt.setColor(Color.DKGRAY);
        txt.setTextSize(36f);
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(48f);
        titlePaint.setFakeBoldText(true);
    }

    public void setData(String title, String[] labels, int[] values) {
        this.title = title == null ? "" : title;
        this.labels = labels == null ? new String[0] : labels;
        this.values = values == null ? new int[0] : values;
        invalidate();
    }

    public void setLabelAngleDegrees(float deg) {
        this.labelAngleDeg = deg;
        invalidate();
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        int w = getWidth();
        int h = getHeight();
        int margin = dp(16);

        if (!TextUtils.isEmpty(title)) {
            c.drawText(title, margin, margin + dp(8), titlePaint);
        }

        int top = margin + dp(40);
        int bottom = h - margin - dp(56); // extra space for rotated labels
        int left = margin + dp(12);
        int right = w - margin;

        c.drawLine(left, top, left, bottom, axis);
        c.drawLine(left, bottom, right, bottom, axis);

        if (values.length == 0 || labels.length != values.length) {
            txt.setTextSize(36f);
            c.drawText("No data", left + dp(12), (top + bottom) / 2f, txt);
            return;
        }

        int max = 1;
        for (int v : values) max = Math.max(max, v);

        int n = values.length;
        int gap = dp(8);
        int width = right - left;
        int barW = Math.max(dp(8), (width - gap * (n + 1)) / Math.max(1, n));
        int x = left + gap;

        // scale text based on N
        float valueTextSize = (n > 20) ? 24f : 30f;
        float labelTextSize = (n > 20) ? 22f : 28f;

        for (int i = 0; i < n; i++) {
            float ratio = (float) values[i] / (float) max;
            int barH = Math.max(dp(4), (int) (ratio * (bottom - top - dp(16))));
            int y = bottom - barH;

            c.drawRect(x, y, x + barW, bottom, bar);

            // value
            txt.setTextSize(valueTextSize);
            String vStr = String.valueOf(values[i]);
            c.drawText(vStr, x + barW / 2f - txt.measureText(vStr) / 2f, y - dp(4), txt);

            // rotated label
            txt.setTextSize(labelTextSize);
            String lab = labels[i] == null ? "" : labels[i];
            float cx = x + barW / 2f;
            float ly = bottom + dp(6);
            c.save();
            c.rotate(-labelAngleDeg, cx, ly);
            c.drawText(lab, cx - txt.measureText(lab) / 2f, ly + dp(18), txt);
            c.restore();

            x += barW + gap;
        }
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return (int) (v * d + 0.5f);
    }
}
