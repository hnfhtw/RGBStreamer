package com.hn.rgbstreamer;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

// RGBStreamer - CanvasView - draws the panel and sends pixels utilizing the BluetoothSocket
// Version: V01_001
// Last Mofidied: 17.02.2016
// Author: HN

public class CanvasView extends View {

    public int width;
    public int height;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    Context context;
    private Paint mPaint;
    TextView outputText;
    private int xout = 0;
    private int yout = 0;
    private int color = Color.WHITE;
    private int SCALEFACTOR = 30;  // HN original 20 (for 640x640px), 30 (for 960x960px)
    private int[][] colorArray;

    private int colorDepth;
    private int pixels_width;
    private int pixels_height;

    public CanvasView(Context c, AttributeSet attrs) {  // constructor
        super(c, attrs);
        context = c;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(4f);

        // get width and height of the drawing area available on the panels (number of pixels)
        Globals appState = ((Globals)super.getContext().getApplicationContext());
        pixels_width = appState.getNoPanelCol()*appState.getNoPixelCol();
        pixels_height = appState.getNoPanelRows()*appState.getNoPixelRows();

        colorArray = new int[pixels_width][pixels_height];
        for(int i = 0; i < pixels_width; i++)
            for(int j = 0; j < pixels_height; j++)
                colorArray[i][j] = Color.rgb(100,100,100);
    }

    // Measure the available size on the screen and set the size of the CanvasView to an integer multiple of the available pixels
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);

        if(w / pixels_width < h / pixels_height)
            SCALEFACTOR = w / pixels_width;
        else
            SCALEFACTOR = h / pixels_height;

        setMeasuredDimension(SCALEFACTOR * pixels_width, SCALEFACTOR * pixels_height);
    }

    // set textfield to write output text (for debugging)
    public void setOutput(TextView text1)
    {
        outputText = text1;
    }

    // set drawing color (selected by colorpicker
    public void setColor(int selectedColor)
    {
        color = selectedColor;

        //DEBUG Text:
        outputText.setText("Coordinates [x / y]: " + xout + " / " + yout + "\nColors: R = " + Color.red(color) + ", G = " + Color.green(color) + ", B = " + Color.blue(color));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = w;
        height = h;

        // Create a bitmap on which the canvas is drawn
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the grid (single pixels on the LED matrix)
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.rgb(100, 100, 100));                                                          // HN: deal with this color at saving bitmaps -> filter it out

        int pixelWidth = SCALEFACTOR;
        int pixelHeight = SCALEFACTOR;
        int borderWidth = 2;
        int rectWidth = SCALEFACTOR - 2*borderWidth;
        int rectHeight = SCALEFACTOR - 2*borderWidth;

        for(int i = 0; i < width / SCALEFACTOR; i++) {
            for (int j = 0; j < height / SCALEFACTOR; j++) {
                mPaint.setColor(colorArray[i][j]);
                canvas.drawRect(borderWidth + i * pixelWidth, borderWidth + j * pixelHeight, borderWidth + rectWidth + i * pixelWidth, borderWidth + rectHeight + j * pixelHeight, mPaint);
            }
        }

        //DEBUG Text:
        outputText.setText("Coordinates [x / y]: " + xout + " / " + yout + "\nColors: R = " + Color.red(color) + ", G = " + Color.green(color) + ", B = " + Color.blue(color));
    }

    // clear the picture - draw the initial grey grid again by filling the colorArray with initial grid color 100,100,100
    public void clearCanvas() {
        for(int i = 0; i < pixels_width; i++)
            for(int j = 0; j < pixels_height; j++)
                colorArray[i][j] = Color.rgb(100,100,100);
        invalidate();

        // Send "clear panel" command to panel
        Globals appState = ((Globals)super.getContext().getApplicationContext());
        appState.sendRGBDrawingPacket(254,254,0,0,0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        // Only save the coordinates if the touch happened inside of the panel
        if(x >= 0 && x <= width && y >= 0 && y <= height) {
            xout = (int) x / SCALEFACTOR;
            if(xout > (pixels_width-1))
                xout = (pixels_width-1);  // avoid colorArray overflow (index from 0 to 31)
            yout = (int) y / SCALEFACTOR;
            if(yout > (pixels_height-1))
                yout = (pixels_height-1);  // avoid colorArray overflow (index from 0 to 31)
        }
        else if(x < 0)
            xout = 0;
        else if(x > width)
            xout = width / SCALEFACTOR - 1; // - 1 to avoid xout from getting 32 -> crash otherwise (colorArray overflow -> index is from 0 to 31)
        else if(y < 0)
            yout = 0;
        else if(y > height)
            yout = height / SCALEFACTOR - 1; // - 1 to avoid yout from getting 32 -> crash otherwise (colorArray overflow -> index is from 0 to 31)

        colorArray[xout][yout] = color;

        invalidate();   // calls onDraw to redraw the canvas

        /* get RGB values of pixel of canvas - used for debugging
        int px = mBitmap.getPixel(xout, yout);
        int red = Color.red(px);
        int green = Color.green(px);
        int blue = Color.blue(px);*/

        //get RGB values of xout, yout (pixel on panel)
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        Globals appState = ((Globals)super.getContext().getApplicationContext());
        int ack = 0;

        // send RGB packet to the remote device - many equal packets are sent if the finger remains on the display and multiple touch events are detected
        if(appState.getAckEnabledDrawing())
        {
            do {
                appState.sendRGBDrawingPacket(xout, yout, Color.red(color), Color.green(color), Color.blue(color));

                try {
                    ack = RGBStreamer.mChannel.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(ack == 123)
                    break;
            }while(true);
        }
        else
        {
            appState.sendRGBDrawingPacket(xout, yout, Color.red(color), Color.green(color), Color.blue(color));
        }

        return true;
    }

    public void streamBitmap(Bitmap bitmapS)
    {
        // start streaming with "start streaming" command
        Globals appState = ((Globals)super.getContext().getApplicationContext());
        appState.sendRGBStreamingPacket(253,253,0,0,0);

        int ack = 0;
        int r,g,b;

        // streaming of bitmap
        for(int x = 0; x < 32; x++)     // loop through all pixels of bitmap
            for(int y = 0; y < 32; y++)
            {
                r = Color.red(bitmapS.getPixel(x, y));          // get R value of pixel
                g = Color.green(bitmapS.getPixel(x, y));        // get G value of pixel
                b = Color.blue(bitmapS.getPixel(x, y));         // get B value of pixel

                if(r == 100 && g == 100 && b == 100)            // don't stream color of grey grid
                {
                    r = 0;
                    g = 0;
                    b = 0;
                }

                if(appState.getAckEnabledStreaming())
                {
                    do {
                        appState.sendRGBStreamingPacket(x, y, r, g, b);
                        ack = 0;        // reset ack - otherwise it would remain at its old value if no byte can be read from the InputStream
                        try {
                            ack = RGBStreamer.mChannel.read();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(ack == 123)  // ack byte is defined to be 123
                            break;
                    }while(true);
                }
                else
                {
                    appState.sendRGBStreamingPacket(x, y, r, g, b);
                }

                // update color array to show picture on canvas
                colorArray[x][y] = bitmapS.getPixel(x,y);
            }

        // finish streaming with "stop streaming" command
        appState.sendRGBStreamingPacket(252,252,0,0,0);
        appState.sendRGBStreamingPacket(0,0,0,0,0);                      // HN Test - to avoid 252 remaining in the fifo buffer

        invalidate();   // update canvas
    }


    // save the picture to a png file
    public void saveBitmap(String filename) {

        Bitmap sBitmap = Bitmap.createBitmap(pixels_width, pixels_height, Bitmap.Config.ARGB_8888);     // create new Bitmap to save as file


        FileOutputStream out = null;

        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/RGBStreamer/";
        File dir = new File(file_path);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dir, filename+".png");

        try {
            //this.setDrawingCacheEnabled(true);
            out = new FileOutputStream(file);
           // mBitmap = this.getDrawingCache();     // Previously used to store bitmap with size of the canvas

            for(int x = 0; x < 32; x++)
                for(int y = 0; y < 32; y++)
                {
                    //if(colorArray[x][y] !=0xFF646464)   // could be used to filter out the color of grey grid
                        sBitmap.setPixel(x,y,colorArray[x][y]);
                   // else
                     //   sBitmap.setPixel(x,y,0xFF000000);   // could be used to replace grey grid color by black
                }



            sBitmap.compress(Bitmap.CompressFormat.PNG, 85, out); // compress bitmap to PNG format and write to the specified file
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                    addImageToGallery(file.getAbsolutePath(), super.getContext());  // add image to gallery
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Add image meta data to the Android Media provider -> that it appears in the gallery
    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }


    // Save color array -> to maintain it at display rotations
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putSerializable("colorArray", colorArray);
        return bundle;
    }

    // Restore color array -> to restore it at display rotations
    @Override
    public void onRestoreInstanceState(Parcelable state) {

        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            this.colorArray = (int[][]) bundle.getSerializable("colorArray");
            state = bundle.getParcelable("instanceState");
        }
        super.onRestoreInstanceState(state);
    }
}