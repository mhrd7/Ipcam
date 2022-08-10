package com.mehrdad.ipcam;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.nio.Buffer;
import java.util.List;
//import com.hoho.android.usbserial.driver.UsbSerialDriver;
//import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.mehrdad.driver.UsbSerialDriver;
import com.mehrdad.driver.UsbSerialPort;
import com.mehrdad.driver.UsbSerialProber;
import com.mehrdad.util.SerialInputOutputManager;

public class MainActivity extends Activity {
    private static final int WRITE_WAIT_MILLIS = 100;
    private static final int READ_WAIT_MILLIS = 100;

    private CameraPreview mPreview;
    private CameraManager mCameraManager;
    private boolean mIsOn = true;
    private SocketClient mThread;
    private SocketClient mmThread;
    private String mIP = "192.168.31.210";
    private int mPort = 8888;
    private String request;
    private byte[] response;
    private  String response1;
  //  private int len;
    private PendingIntent mPermissionIntent;
    private UsbDevice device;
    private static final String ACTION_USB_PERMISSION = "com.android.usb.USB_PERMISSION";
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final int PERMISSION_REQUEST_CODE = 1;
    static class ListItem {
        UsbDevice device;
        int port;
        UsbSerialDriver driver;

        ListItem(UsbDevice device, int port, UsbSerialDriver driver) {
            this.device = device;
            this.port = port;
            this.driver = driver;
        }
    }
        private int baudRate = 115200;
        private boolean withIoManager = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (mIsOn) {
            if (mIP == null) {
                mThread = new SocketClient(mPreview);
            } else {
                mThread = new SocketClient(mPreview, mIP, mPort);

            }

            mIsOn = false;

        } else {
            closeSocketClient();
            reset();
        }


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                closeSocketClient();
                reset();

                mIP = "192.168.31.211";
                mmThread = new SocketClient(mPreview, mIP, mPort);

            }

        }, 10000);

        // get an image from the camera


        mCameraManager = new CameraManager(this);
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCameraManager.getCamera());
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);



        // Find all available drivers from attached devices
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers =
                UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            Toast.makeText(getApplicationContext(), "No serial USB devices!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Open a connection to the first available driver
        UsbSerialDriver driver;
        try {
            driver = availableDrivers.get(0);
        } catch (Exception ignored) {
            Toast.makeText(getApplicationContext(), "No correct serial USB devices!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

       /* // Check and grant permissions
        if (!checkAndRequestPermission(manager, driver.getDevice())) {
            Toast.makeText(getApplicationContext(),
                    "Please grant permission and try again", Toast.LENGTH_LONG).show();
            return;
        } */

        // Open USB device
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            Toast.makeText(getApplicationContext(), "Error opening USB device!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        UsbSerialPort port = driver.getPorts().get(0);
        try {
            // Open first available serial port
            try {
                port.open(connection);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                port.write("hello".getBytes(), WRITE_WAIT_MILLIS);

                byte buffer[] = new byte[16];
                int numBytesRead = port.read(buffer, 1000);
                Log.d(TAG, "Read " + numBytesRead + " bytes.");
                response1= buffer.toString();
                TextView response = (TextView)findViewById(R.id.response);
                response.setText(response1);




             /*   byte[] buffer = new byte[50];
                int len = port.read(buffer, READ_WAIT_MILLIS);
                response1= buffer.toString();
                TextView response = (TextView)findViewById(R.id.response);
                response.setText("response1");
*/
                //Toast.makeText(getApplicationContext(), response1,Toast.LENGTH_SHORT).show();

                //port.write(request, WRITE_WAIT_MILLIS);
                //len = port.read(response, READ_WAIT_MILLIS);

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }


    }

    public void tt(String st) {

        Toast.makeText(MainActivity.this, st, Toast.LENGTH_LONG).show();


    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.ipcamera, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // TODO Auto-generated method stub
//        int id = item.getItemId();
//        switch (id) {
//            case R.id.:
//                setting();
//                break;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    private void setting() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.activity_server_setting, null);
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.setting_title)
                .setView(textEntryView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        EditText ipEdit = (EditText) textEntryView.findViewById(R.id.ip_edit);
                        EditText portEdit = (EditText) textEntryView.findViewById(R.id.port_edit);
                        mIP = ipEdit.getText().toString();
                        mPort = Integer.parseInt(portEdit.getText().toString());

                        Toast.makeText(MainActivity.this, "New address: " + mIP + ":" + mPort, Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked cancel so do some stuff */
                    }
                })
                .create();
        dialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeSocketClient();
        mPreview.onPause();
        mCameraManager.onPause();              // release the camera immediately on pause event
        reset();
    }

    private void reset() {
        mIsOn = true;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mCameraManager.onResume();
        mPreview.setCamera(mCameraManager.getCamera());
    }

    private void closeSocketClient() {
        if (mThread == null)
            return;

        mThread.interrupt();
        try {
            mThread.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mThread = null;
    }
/*
    private void usbSetting(){
        try {
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.isEmpty()){
                Toast toast = Toast.makeText(getApplicationContext(),
                        "FAIL", Toast.LENGTH_SHORT);
                toast.show();
            }
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

// Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }

// Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            manager.requestPermission(device, mPermissionIntent);

            return;
        }

// Read some data! Most have just one port (port 0).
        UsbSerialPort port = driver.getPorts().get(0);
        try {
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            // usbIoManager = new SerialInputOutputManager(usbSerialPort, this);
            //   usbIoManager.start();
            port.write("hello".getBytes(), WRITE_WAIT_MILLIS);


            // port.write(request, WRITE_WAIT_MILLIS);
            //    len = port.read(response, READ_WAIT_MILLIS);

            byte buffer[] = new byte[16];
            int numBytesRead = port.read(buffer, 1000);
            Log.d(TAG, "Read " + numBytesRead + " bytes.");
        } catch (IOException e) {
            // Deal with error.
        } finally {
            try {
                port.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }*/

    private boolean checkAndRequestPermission(UsbManager manager, UsbDevice usbDevice) {
        // Check if permissions already exists
        if (hasPermissions(getApplicationContext(), PERMISSIONS)
                && manager.hasPermission(usbDevice))
            return true;
        else {
            // Request GPS permissions
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS,
                    PERMISSION_REQUEST_CODE);

            // Request USB permission
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                    0, new Intent(ACTION_USB_PERMISSION), 0);
            manager.requestPermission(usbDevice, pendingIntent);
            return false;
        }
    }

    /**
     * Checks for permissions
     * @param context Activity
     * @param permissions List of permissions
     * @return true if all permissions were granted
     */
    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }



}
