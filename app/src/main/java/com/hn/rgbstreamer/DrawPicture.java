package com.hn.rgbstreamer;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;      // HN SNAKE

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import com.hn.rgbstreamer.TextinputDialogFragment.TextinputDialogListener;
import com.hn.rgbstreamer.CanvasView.SnakeDirections;
import com.hn.rgbstreamer.CanvasView.GameStatusListener;

import org.w3c.dom.Text;

// RGBStreamer - DrawPicture activity
// Version: V01_005
// Last Mofidied: 10.10.2016 (snake game functionality added)
// Author: HN            (ColorPicker: https://github.com/QuadFlask/colorpicker,
//                        TextInputDialog: http://www.androidinterview.com/android-custom-dialog-box-example-android-dialog/
//                        PickImageFromGallery: http://programmerguru.com/android-tutorial/how-to-pick-image-from-gallery/)


public class DrawPicture extends AppCompatActivity implements TextinputDialogListener, GameStatusListener{

    private static final int RESULT_LOAD_IMG = 1;
    private CanvasView customCanvas;
    private static TextView outputText;
    private static TextView outputColor;
    private int initialColor;
    String imgDecodableString;
    private int px_width;
    private int px_height;
    private boolean isSnakeMode;
    private Handler snakeHandler;
    private boolean playSnake;
    Button playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Globals appState = ((Globals)getApplicationContext());
        this.isSnakeMode = appState.getSnakeMode();

        if(isSnakeMode) {
            setContentView(R.layout.activity_snake);
            snakeHandler = new Handler();
            playButton = (Button) findViewById(R.id.playButton1);
            setTitle("Snake");
        }
        else {
            setContentView(R.layout.activity_draw_picture);
            outputText = (TextView) findViewById(R.id.outputText);
            outputColor = (TextView) findViewById(R.id.outputColor);
        }

        // Get a reference to all UI widgets
        customCanvas = (CanvasView) findViewById(R.id.signature_canvas);
        customCanvas.setGameStatusListener(this);

        if(!isSnakeMode)
            customCanvas.setOutput(outputText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Globals appState = ((Globals)getApplicationContext());
        if(appState != null)    // get last color that was set and set it as initial color
        {
            if(appState.isColorSet() == true)
                this.initialColor = appState.getColor();
            else
                this.initialColor = Color.WHITE;

            if(!isSnakeMode) {
                //DEBUG Text:
                String hexColor = String.format("#%06X", (0xFFFFFF & initialColor));   // convert the int color to a hex format (for debugging)
                outputColor.setText("HEX Color: " + hexColor);  // output of the picked color (for debugging)
            }
            customCanvas.setColor(initialColor); // set the initial color in the canvas

            // get width and height of the drawing area available on the panels (number of pixels)
            px_width = appState.getNoPanelCol()*appState.getNoPixelCol();
            px_height = appState.getNoPanelRows()*appState.getNoPixelRows();
        }
    }

    public void backButtonClicked(View view)
    {
        Globals appState = ((Globals)getApplicationContext());
        appState.setSnakeMode(false);
        customCanvas.clearCanvas();
        isSnakeMode = false;
        super.finish();
    }

    public void streamBitmapButtonClicked(View view)
    {
        // Image Picker START
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
        // END Image Picker
    }

    // Image Picker START
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmapS = BitmapFactory.decodeFile(imgDecodableString,bmOptions);
                bitmapS = Bitmap.createScaledBitmap(bitmapS,px_width,px_height,true);

                // Stream Bitmap
                customCanvas.streamBitmap(bitmapS);

            } else {
                Toast.makeText(this, "You haven't picked an image.",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "An error occurred.", Toast.LENGTH_LONG).show();
        }

    } // END Image Picker

    // clear the picture
    public void clearCanvas(View v) {
        customCanvas.clearCanvas();
    }

    // start color picker
    public void setColorButtonClicked(View v)
    {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .initialColor(0xFFFFFFFF)
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                .density(12)
                .lightnessSliderOnly()
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                        // do nothing -> could be used to show a toast with hex code of currently selected color
                    }
                })
                .setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {

                        if (!isSnakeMode) {
                            //DEBUG Text:
                            String hexColor = String.format("#%06X", (0xFFFFFF & selectedColor));   // convert the int color to a hex format (for debugging)
                            outputColor.setText(hexColor);  // output of the picked color (for debugging)
                        }
                        customCanvas.setColor(selectedColor); // set the new color in the canvas

                        Globals appState = ((Globals) getApplicationContext());
                        appState.setColor(selectedColor);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
    }

    // save the bitmap - step 1 -> call method which shows dialog to enter filename
    public void saveButtonClicked(View v) {
        showFilenameDialog();
    }

    // save the bitmap - step 2 -> show dialog to enter filename
    private void showFilenameDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TextinputDialogFragment inputNameDialog = new TextinputDialogFragment();
        inputNameDialog.setCancelable(true);
        inputNameDialog.inputFilterEnabled(true);
        inputNameDialog.setDialogTitle("Enter filename");
        inputNameDialog.show(fragmentManager, "Input Dialog");
    }

    // save the bitmap - step 3 -> the dialog fragment returns the entered filename as a string, call the customCanvas.saveBitmap() method and provide the filename
    @Override
    public void onFinishInputDialog(String inputText) {
        // -- Finish dialog box show msg
        if(inputText != null)
        {
            Toast.makeText(this, "File " + inputText + ".png saved!", Toast.LENGTH_SHORT).show();
            customCanvas.saveBitmap(inputText);
        }
        else
            Toast.makeText(this, "File not saved!", Toast.LENGTH_SHORT).show();
    }

    public void whiteButtonClicked(View v)
    {
        customCanvas.fillPanel(Color.rgb(0xff, 0xff, 0xff));
    }

    public void redButtonClicked(View v)
    {
        customCanvas.fillPanel(Color.rgb(0xff, 0x00, 0x00));
    }

    public void blueButtonClicked(View v)
    {
        customCanvas.fillPanel(Color.rgb(0x00, 0x00, 0xff));
    }

    public void greenButtonClicked(View v)
    {
        customCanvas.fillPanel(Color.rgb(0x00, 0xff, 0x00));
    }

    public void upButtonClicked(View v)
    {
        if(playSnake)
            customCanvas.setSnakeDirection(SnakeDirections.UP);
    }

    public void downButtonClicked(View v)
    {
        if(playSnake)
            customCanvas.setSnakeDirection(SnakeDirections.DOWN);
    }

    public void leftButtonClicked(View v)
    {
        if(playSnake)
            customCanvas.setSnakeDirection(SnakeDirections.LEFT);
    }

    public void rightButtonClicked(View v)
    {
        if(playSnake)
            customCanvas.setSnakeDirection(SnakeDirections.RIGHT);
    }

    public void playButtonClicked(View v)
    {
        if(!playSnake) {
            snakeHandler.post(snakeRunnable);
            playSnake = true;
            playButton.setText("Pause");
        }
        else {
            snakeHandler.removeCallbacks(snakeRunnable);
            playSnake = false;
            playButton.setText("Play");
        }
    }

    private Runnable snakeRunnable = new Runnable() {
        @Override
        public void run() {
            if(playSnake) {
                customCanvas.snakeUpdate();
                snakeHandler.postDelayed(snakeRunnable, 70);
            }
        }
    };

    public void gameStatusChanged(boolean gameover, int snakeLength){
        if(gameover) {
            playSnake = false;
            customCanvas.snakeReset();
            snakeHandler.removeCallbacks(snakeRunnable);
            Toast.makeText(this, "Game Over - Score: " + snakeLength, Toast.LENGTH_LONG).show();
            playButton.setText("Play");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_draw_picture, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
