package com.kandaidea.osmdroidmaptest.map;

import android.graphics.Color;
import android.graphics.Paint;

import org.osmdroid.views.overlay.Polygon;

public class PolygonWithLabel extends Polygon
{
    Paint textPaint = null;
    Polygon polygon;
    String label;
    PolygonWithLabel(Polygon polygon, String label)
    {
        this.polygon = polygon;
        this.label = label;
        textPaint = new Paint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(40f);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

}
