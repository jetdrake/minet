package com.micool.minet.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.micool.minet.Helpers.Mapper;
import com.micool.minet.Models.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class GraphView extends View {

    private Paint paintBackground;
    private Paint landmark;
    private Paint active;
    private Context myContext;
    private int scale = 80;

    final String TAG = "graph";
    private Mapper mapper = new Mapper();

    public GraphView(Context context) {
        super(context);
        init(context);
    }

    public GraphView(Context context, AttributeSet attrs){
        super(context, attrs);
        init(context);
    }

    private void init(Context c){
        myContext = c;

        paintBackground = new Paint();
        paintBackground.setStyle(Paint.Style.FILL);
        paintBackground.setColor(Color.BLACK);

        landmark = new Paint();
        landmark.setStyle(Paint.Style.STROKE);
        landmark.setColor(Color.WHITE);
        landmark.setStrokeWidth(3);

        active = new Paint();
        active.setStyle(Paint.Style.STROKE);
        active.setColor(Color.RED);
        active.setStrokeWidth(3);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, getWidth(), getHeight(), paintBackground);
        int cx = getWidth()/2;
        int cy = getHeight()/2;
        int offset = scale / 2;
        for (Coordinate it : mapper.getMap()) {
            //int invY = getHeight() - it.y;
            int x = it.getX() * scale + 20 ;// + cx;
            int y = it.getY() * scale + 50;
            Log.d(TAG, "onDraw: "+ x+", "+y);
            if (it.isActive()){
                canvas.drawCircle(x, y, 10, active);
            } else {
                canvas.drawCircle(x, y, 10, landmark);
            }
        }
    }

    public void buildMapFromData(String data){
        mapper.convertPythonListToJava(data);
    }

    public void setActiveLandmark(String data){
        mapper.setActiveLandmark(data);
    }
}