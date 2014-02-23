package es.mgl.imesh;

import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler = new Handler();
    private BluetoothGatt myGatt;
    private Button goBabyGo;
    private SeekBar seekbar;
    private Button menu;
    private static TextView quantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        goBabyGo = (Button) findViewById(R.id.button);
        seekbar = (SeekBar) findViewById(R.id.seekBar1);
        menu = (Button) findViewById(R.id.menu);
        quantity = (TextView) findViewById(R.id.quantitiy);

        menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent go = new Intent(getApplicationContext(), Drawer.class);
                startActivity(go);
                overridePendingTransition(R.anim.right, R.anim.left);
            }
        });

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Ensures Bluetooth is available on the device and it is enabled. If
        // not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 3);
        }

        goBabyGo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                scanLeDevice(true);
            }
        });

    }

    private static final long SCAN_PERIOD = 10000;

    private void scanLeDevice(final boolean enable) {

        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {

                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                if (myGatt != null)
                    myGatt.close();
            }
        }, SCAN_PERIOD);

        UUID service = UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFF0");
        UUID[] uids = { service };

        mBluetoothAdapter.startLeScan(mLeScanCallback);

    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            // Log.d(TAG, "DEVICE " + device.getAddress());
            if (device.getAddress().equals("00:02:72:3F:E7:AD"))
                device.connectGatt(getApplicationContext(), false, mGattCallback);
            // runOnUiThread(new Runnable() {
            // @Override
            // public void run() {
            // Log.d("TAG", "BLE DEVICES!!" + device.getName() + " " + " " +
            // device.getAddress());
            // if (device.getName().equals("test")) {
            // mBluetoothAdapter.stopLeScan(mLeScanCallback);
            // // device.connectGatt(getApplicationContext(), false,
            // // mGattCallback);
            //
            // }
            // }
            // });
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private boolean done = false;

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
    protected static final String TAG = "TAG";

    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                UUID service = UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFF0");
                gatt.discoverServices();
                myGatt = gatt;

                // Log.i(TAG, "Attempting to start service discovery:" +
                // gatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;

                Log.i(TAG, "Disconnected from GATT server.");

            }
        }

        @Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "SUCCESS IN CONNECTION TO GATT");
                // displayGattServices(gatt.getServices());
                UUID service = UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFF0");
                UUID characteristic = UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFF2");
                Log.d(TAG, "====================================================================="
                        + gatt.getService(service).getCharacteristic(characteristic).getUuid());
                // if (!done) {
                // done = true;
                gatt.readCharacteristic(gatt.getService(service).getCharacteristic(characteristic));
                // }
                // Log.d(TAG, "car---> " +
                // gatt.getService(service).getCharacteristic(characteristic).getValue());

                // Log.d(TAG, "" + gatt.getService(service));
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "called " + characteristic.getUuid());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                int value = Integer.parseInt(characteristic.getStringValue(0));
                seekbar.setMax(100);
                seekbar.setProgress(value);
                quantity.setText("" + value);
                quantity.invalidate();
                Log.d(TAG, "CHARACTERSITICCCCCCCCCCCCCCCCC    " + characteristic.getStringValue(0));

                gatt.close();
            }
        }

    };
}
