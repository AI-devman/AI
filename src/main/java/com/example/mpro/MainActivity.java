package com.example.mpro;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final String MODEL_PATH = "mnist.tflite";
    private static final boolean QUANT = true;
    private static final String LABEL_PATH = "labels.txt";
    private static final int INPUT_SIZE = 28;

    private Classifier classifier;

    private Executor executor = Executors.newSingleThreadExecutor();

    Canvas cv;
    Bitmap bmp, targetbmp;
    ImageView imageView;
    TextView mText;
    private Path path = new Path();

    int width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        width = display.getWidth();

        imageView = (ImageView) findViewById(R.id.numberImage);
        mText = (TextView) findViewById(R.id.resultText);
        mText.setText("Welcome!");

        DrawImage();
        initTensorFlowAndLoadModel();
    }

    public void DrawImage() {
        bmp = Bitmap.createBitmap(140, 140, Bitmap.Config.ARGB_8888);
        cv = new Canvas(bmp);
        cv.drawColor(Color.LTGRAY);

        Draw();
    }

    public void Draw() {
        Paint pnt = new Paint();

        pnt.setAntiAlias(true);
        pnt.setStrokeWidth(5);
        pnt.setStyle(Paint.Style.STROKE);
        pnt.setStrokeJoin(Paint.Join.ROUND);
        pnt.setColor(Color.BLACK);

        cv.drawPath(path, pnt);
        imageView.setImageBitmap(bmp);
    }

    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX() / width * 140;
        float eventY = event.getY() / width * 140;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(eventX, eventY); //Only paint the line path.
                cv.drawColor(Color.BLUE);
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(eventX, eventY);
                break;
            case MotionEvent.ACTION_UP:
                cv.drawColor(Color.LTGRAY);
                break;
            default:
                return false;
        }

        // Schedules a repaint.
        Draw();
        return true;
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE,
                            QUANT);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    private String CleanString(String sstr) {
        char[] resultarray;
        char[] numbers = {'2', '3'};
        int k = 0;

        resultarray = sstr.toCharArray();
        String rstr = "1.";

        for (int i = 1; i < sstr.length() - 1; i++) {
            if (resultarray[i] == ',') {
                rstr += numbers[k] + ".";
                k += 1;
            } else {
                rstr += resultarray[i];
            }
        }

        return rstr;
    }

    public void onBtnDetect(View vw){
        targetbmp = Bitmap.createScaledBitmap(bmp, INPUT_SIZE, INPUT_SIZE,false);
        final List<Classifier.Recognition> results = classifier.recognizeImage(targetbmp);
        String resultstr = results.toString();
        mText.setText(CleanString(resultstr));
    }

    public void onBtnClear(View vw) {
        cv.drawColor(Color.LTGRAY);
        path.reset();
    }

    public void onBtnClose(View vw){
        finish();
    }

}