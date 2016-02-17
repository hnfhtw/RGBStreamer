package com.hn.rgbstreamer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

// RGBStreamer Main Activity
// Version: V01_001
// Last Mofidied: 17.02.2016
// Author: HN (Bluetooth code: http://developer.android.com/guide/topics/connectivity/bluetooth.html)

public class RGBStreamer extends AppCompatActivity{

    // Declarations
    EditText noPanelRowsInput;
    EditText noPanelColInput;
    EditText noPixelRowsInput;
    EditText noPixelColInput;
    EditText colorDepthInput;
    EditText btReceiverNameInput;
    EditText rgbStreamingProtocolSizeInput;
    EditText rgbDrawingProtocolSizeInput;
    CheckBox ackEnabledStreamingCheckBox;
    CheckBox ackEnabledDrawingCheckBox;
    Button connectButton;
    Button drawButton;

    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    BluetoothDevice connectDevice;
    public static ConnectedThread mChannel;
    BluetoothSocket mSocket;
    ConnectThread mConnectThread;
    int REQUEST_ENABLE_BT = 1;
    boolean connected = false;
    int noPanelRows;
    int noPanelCol;
    int noPixelRows;
    int noPixelCol;
    int colorDepth;
    int rgbStreamingProtocolSize;
    int rgbDrawingProtocolSize;
    String btReceiverName;
    boolean ackEnabledStreaming;
    boolean ackEnabledDrawing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rgbstreamer);

        // Get a reference to all UI widgets
        noPanelRowsInput = (EditText) findViewById(R.id.noPanelRows);
        noPanelColInput = (EditText) findViewById(R.id.noPanelCol);
        noPixelRowsInput = (EditText) findViewById(R.id.noPixelRows);
        noPixelColInput = (EditText) findViewById(R.id.noPixelCol);
        colorDepthInput = (EditText) findViewById(R.id.colorDepth);
        btReceiverNameInput = (EditText) findViewById(R.id.btReceiverName);
        rgbStreamingProtocolSizeInput = (EditText) findViewById(R.id.rgbStreamingProtocolSize);
        ackEnabledStreamingCheckBox = (CheckBox) findViewById(R.id.ackEnabledStreamingCheckBox);
        rgbDrawingProtocolSizeInput = (EditText) findViewById(R.id.rgbDrawingProtocolSize);
        ackEnabledDrawingCheckBox = (CheckBox) findViewById(R.id.ackEnabledDrawingCheckBox);
        connectButton = (Button) findViewById(R.id.connectButton);
        drawButton = (Button) findViewById(R.id.drawButton);

        // Get saved configuration data using SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("RGBStreamerConfig", Context.MODE_PRIVATE);      // private mode -> only this app can access the file that will be created
        noPanelRows = sharedPref.getInt("noPanelRows", 1); // if the key doesn't exist -> take the default value 1
        noPanelCol = sharedPref.getInt("noPanelCol", 1); // if the key doesn't exist -> take the default value 1
        noPixelRows = sharedPref.getInt("noPixelRows", 32); // if the key doesn't exist -> take the default value 32
        noPixelCol = sharedPref.getInt("noPixelCol", 32); // if the key doesn't exist -> take the default value 32
        colorDepth = sharedPref.getInt("colorDepth", 8); // if the key doesn't exist -> take the default value 8
        rgbStreamingProtocolSize = sharedPref.getInt("rgbStreamingProtocolSize", 5); // if the key doesn't exist -> take the default value 5
        rgbDrawingProtocolSize = sharedPref.getInt("rgbDrawingProtocolSize", 6); // if the key doesn't exist -> take the default value 6
        btReceiverName = sharedPref.getString("btReceiverName", "RN42-5489"); // if the key doesn't exist -> take the default value "RN42-5489"
        ackEnabledStreaming = sharedPref.getBoolean("ackEnabledStreaming", false); // if the key doesn't exist -> take the default value false
        ackEnabledDrawing = sharedPref.getBoolean("ackEnabledDrawing", false); // if the key doesn't exist -> take the default value false

        // Set the UI elements according to the saved configuration
        noPanelRowsInput.setText(Integer.toString(noPanelRows));
        noPanelColInput.setText(Integer.toString(noPanelCol));
        noPixelRowsInput.setText(Integer.toString(noPixelRows));
        noPixelColInput.setText(Integer.toString(noPixelCol));
        colorDepthInput.setText(Integer.toString(colorDepth));
        rgbStreamingProtocolSizeInput.setText(Integer.toString(rgbStreamingProtocolSize));
        rgbDrawingProtocolSizeInput.setText(Integer.toString(rgbDrawingProtocolSize));
        btReceiverNameInput.setText(btReceiverName);
        ackEnabledStreamingCheckBox.setChecked(ackEnabledStreaming);
        ackEnabledDrawingCheckBox.setChecked(ackEnabledDrawing);

        // Add TextWatchers to the EditText input fields and OnCheckedChangeListener to the CheckBoxes
        noPanelRowsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    int temp = Integer.parseInt(noPanelRowsInput.getText().toString());
                    if (temp >= 1 && temp <= 8)
                        noPanelRows = temp;
                    else {
                        noPanelRowsInput.setText(Integer.toString(noPanelRows));
                        Toast.makeText(getApplicationContext(), "Allowed values: 1 to 8", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        noPanelColInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    int temp = Integer.parseInt(noPanelColInput.getText().toString());
                    if (temp >= 1 && temp <= 8)
                        noPanelCol = temp;
                    else {
                        noPanelColInput.setText(Integer.toString(noPanelCol));
                        Toast.makeText(getApplicationContext(), "Allowed values: 1 to 8", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        noPixelRowsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    int temp = Integer.parseInt(noPixelRowsInput.getText().toString());
                    if (temp >= 1 && temp <= 256)
                        noPixelRows = temp;
                    else {
                        noPixelRowsInput.setText(Integer.toString(noPixelRows));
                        Toast.makeText(getApplicationContext(), "Allowed values: 1 to 256", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        noPixelColInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    int temp = Integer.parseInt(noPixelColInput.getText().toString());
                    if(temp >= 1 && temp <= 256)
                        noPixelCol = temp;
                    else {
                        noPixelColInput.setText(Integer.toString(noPixelCol));
                        Toast.makeText(getApplicationContext(), "Allowed values: 1 to 256", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        colorDepthInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    int temp = Integer.parseInt(colorDepthInput.getText().toString());
                    if(temp >= 1 && temp <= 8)
                        colorDepth = temp;
                    else {
                        colorDepthInput.setText(Integer.toString(colorDepth));
                        Toast.makeText(getApplicationContext(), "Allowed values: 1 to 8", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        rgbStreamingProtocolSizeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    int temp = Integer.parseInt(rgbStreamingProtocolSizeInput.getText().toString());
                    if(temp >= 5 && temp <= 6)
                        rgbStreamingProtocolSize = temp;
                    else {
                        rgbStreamingProtocolSizeInput.setText(Integer.toString(rgbStreamingProtocolSize));
                        Toast.makeText(getApplicationContext(), "Allowed values: 5 or 6", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        ackEnabledStreamingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                                   @Override
                                                                   public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                                       ackEnabledStreaming = isChecked;
                                                                   }
                                                               }
        );

        rgbDrawingProtocolSizeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    int temp = Integer.parseInt(rgbDrawingProtocolSizeInput.getText().toString());
                    if(temp >= 5 && temp <= 6)
                        rgbDrawingProtocolSize = temp;
                    else {
                        rgbDrawingProtocolSizeInput.setText(Integer.toString(rgbDrawingProtocolSize));
                        Toast.makeText(getApplicationContext(), "Allowed values: 5 or 6", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        ackEnabledDrawingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                                   @Override
                                                                   public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                                       ackEnabledDrawing = isChecked;
                                                                   }
                                                               }
        );
    }

    @Override
    protected void onResume() {     // get global variables if the main activity is resumed
        super.onResume();
        Globals appState = ((Globals)getApplicationContext());
        if(appState != null)
        {
            this.mSocket = appState.getBluetoothSocket();
            this.mChannel = appState.getConnectedThread();
            this.connectDevice = appState.getBluetoothDevice();
            this.mConnectThread = appState.getConnectThread();
            this.connected = appState.getConnectionStatus();
        }
        if(connected == true)
            connectButton.setText("Disconnect");
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save the configuration data
        SharedPreferences sharedPref = getSharedPreferences("RGBStreamerConfig", Context.MODE_PRIVATE);      // private mode -> only this app can access the file that will be created
        SharedPreferences.Editor editor = sharedPref.edit();        // create editor to add stuff to the file
        editor.putInt("noPanelRows", noPanelRows);
        editor.putInt("noPanelCol", noPanelCol);
        editor.putInt("noPixelRows", noPixelRows);
        editor.putInt("noPixelCol", noPixelCol);
        editor.putInt("colorDepth", colorDepth);
        editor.putString("btReceiverName", btReceiverNameInput.getText().toString());
        editor.putInt("rgbStreamingProtocolSize", rgbStreamingProtocolSize);
        editor.putBoolean("ackEnabledStreaming", ackEnabledStreamingCheckBox.isChecked());
        editor.putInt("rgbDrawingProtocolSize", rgbDrawingProtocolSize);
        editor.putBoolean("ackEnabledDrawing", ackEnabledDrawingCheckBox.isChecked());
        editor.apply();
    }

    public void connectButtonClicked(View view)
    {
        Globals appState = ((Globals)getApplicationContext());

        // If not yet connected -> connect
        if(!connected) {
            // Step 1: Get Bluetooth Adapter of the device
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                // Possible error handling if the device has no Bluetooth functionality
            }

            // Step 2: Enable Bluetooth
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
            }

            // CHECK: Wait until Bluetooth adapter is enabled - app is blocked here -> this should be improved (intent above makes no sense in this case)!
            while (!mBluetoothAdapter.isEnabled()) {
            }

            // Step 3: Find all paired devices
            pairedDevices = mBluetoothAdapter.getBondedDevices();      // Get all paired devices
            if (pairedDevices.size() > 0) {
               // Toast.makeText(this, "Number of paired devices = " + pairedDevices.size(), Toast.LENGTH_SHORT).show();   // Show a toast with the number of paired devices
                for (BluetoothDevice device : pairedDevices) // Loop through all paired devices
                {
                    //outputText += device.getName() + "\n" + device.getAddress() + "\n";  // Generate a string containing name and address of all paired devices -> DEBUGGING
                    if (device.getName().equals(btReceiverNameInput.getText().toString())) {
                        connectDevice = device;
                        appState.setBluetoothDevice(connectDevice);
                    }
                }
            }

            // Step 4: Connect to target device
            mConnectThread = new ConnectThread(connectDevice);
            //while(mSocket==null){}
            mConnectThread.run();    // The ConnectThread creates a BluetoothSocket for a secure RFCOMM conection to the remote device and establishes the  connection
            Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();   // Show a toast if connection was successful
            appState.setConnectThread(mConnectThread);

            // DEBUG:
            mChannel = new ConnectedThread(mSocket);    // to send and receive data a new thread is created to avoid blocking, the ConnectedThread writes to the OutputStream of the BluetoothSocket and reads from the InputStream of the BluetoothSocket
            appState.setConnectedThread(mChannel);

            connectButton.setText("Disconnect");
            connected = true;
            appState.setConnectionStatus(connected);
        }

        // If already connected -> disconnect
        else
        {
            //mConnectThread.cancel();
            mChannel.cancel();
            try
            {
                mSocket.close();
            }
            catch (IOException e)
            {

            }
            connected = false;
            appState.setConnectionStatus(connected);
            connectButton.setText("Connect");
            Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK)  // Display a toast if Bluetooth was successfully enabled
        {
            Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show();
        }
        else if(requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED)   // Display a toast if enablind Bluetooth failed
        {
            Toast.makeText(this, "Failed to enable Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    public void drawButtonClicked(View view)
    {
        // Set global variables that they are available in the drawing app and not gone if the main activity is closed by the Android garbage collector
        Globals appState = ((Globals)getApplicationContext());
        appState.setNoPanelCol(noPanelCol);
        appState.setNoPanelRows(noPanelRows);
        appState.setNoPixelCol(noPixelCol);
        appState.setNoPixelRows(noPixelRows);
        appState.setColorDepth(colorDepth);
        appState.setRGBStreamingProtocolSize(rgbStreamingProtocolSize);
        appState.setAckEnabledStreaming(ackEnabledStreaming);
        appState.setRGBDrawingProtocolSize(rgbDrawingProtocolSize);
        appState.setAckEnabledDrawing(ackEnabledDrawing);

        if(mChannel == null)
            Toast.makeText(this, "Please connect to target device first.", Toast.LENGTH_LONG).show();
        else {
            Intent intent = new Intent(this, DrawPicture.class);
            startActivity(intent);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rgbstreamer, menu);
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

    public class ConnectThread extends Thread           // Thread to open a BluetoothSocket -> RFCOMM connection to the remote device
    {
        Globals appState = ((Globals)getApplicationContext());

        public ConnectThread(BluetoothDevice device)
        {
            BluetoothSocket tmp = null;

            try
            {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));        // this function returns a BluetoothSocket for a secure (authenticated) RFCOMM session
            }
            catch(IOException e)
            {

            }
            mSocket = tmp;
            appState.setBluetoothSocket(mSocket);
        }

        public void run()
        {
            mBluetoothAdapter.cancelDiscovery();
            try
            {
                mSocket.connect();          // connect to the remote device via the created BluetoothSocket, this call blocks until the socket is connected (without exception), or if the connection fails (exception)
            }
            catch (IOException connectException)
            {
                try
                {
                    mSocket.close();
                }
                catch (IOException closeException)
                {

                }
                return;
            }
        }

        public void cancel()
        {
            try
            {
                mSocket.close();
            }
            catch (IOException e)
            {

            }
        }
    }


    public class ConnectedThread extends Thread {                       // Thread to write to and read from the BluetoothSocket -> as the run() method is not used this is just a helper class
        private final BluetoothSocket mmSocket;                         // to utilize the BluetoothSocket. Normally this is used as a background thread that continuously reads the input
        private final InputStream mmInStream;                           // of the BluetoothSocket and sends obtained bytes to the calling activity via messages
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        /*public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }*/
        public int read() throws IOException {      // read a single byte from the BluetoothSocket
            return mmInStream.read();
        }

        // Send bytes to the remote device
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

       // Close the RFCOMM connection
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

   @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

    }
}
