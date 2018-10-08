package unimelb.comp90018_instaviewer.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.utilities.PhotoOrCropUtil;

public class BluetoothActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

    private List<String> bluetoothDevices = new ArrayList<>();

    private ListView lvDevices;
    private ArrayAdapter<String> arrayAdapter;
    private final UUID MY_UUID = UUID.fromString("db713ac8-4b28-7f25-aafe-59733c27bsd3");

    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver mReceiver;

    private final String NAME = "Bluetooth_Socket";
    private BluetoothDevice selectDevice;
    private BluetoothSocket clientSocket;
    private OutputStream os;

    private Handler handler;
    private AcceptThread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        if (this.checkBleDevice()) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                            bluetoothDevices.add(device.getName() + ":" + device.getAddress() + "\n");
                            arrayAdapter.notifyDataSetChanged();
                        }
                    } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                        setTitle("Search Completed");
                    }
                }

            };

            this.handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    PhotoOrCropUtil.getInstance().showToast(BluetoothActivity.this, (String) msg.obj);
                }
            };


            this.lvDevices = findViewById(R.id.lvDevices);
            this.arrayAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1,
                    bluetoothDevices);
            this.lvDevices.setAdapter(arrayAdapter);
            this.lvDevices.setOnItemClickListener(this);
            Set<BluetoothDevice> devices = this.mBluetoothAdapter.getBondedDevices();
            if (devices.size() > 0) {
                for (BluetoothDevice bluetoothDevice : devices) {
                    this.bluetoothDevices.add(bluetoothDevice.getName() + ":" + bluetoothDevice.getAddress() + "\n");
                }
            }

            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(this.mReceiver, filter);
            filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(this.mReceiver, filter);

        }
    }

    public void onClick(View view) {
        if (this.checkPermissionACCESS_COARSE_LOCATION(this)) {
            setTitle("Searching...");
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            mBluetoothAdapter.startDiscovery();
        }
    }

    public boolean checkPermissionACCESS_COARSE_LOCATION(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("Bluetooth", context, Manifest.permission.ACCESS_COARSE_LOCATION);
                } else {
                    ActivityCompat.requestPermissions(
                            (Activity) context,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(BluetoothActivity.this, "Bluetooth Permission Granted",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BluetoothActivity.this, "Bluetooth Permission Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[]{permission},
                                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    private boolean checkBleDevice() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            this.mBluetoothAdapter = bluetoothManager.getAdapter();

            if (this.mBluetoothAdapter != null) {
                if (!this.mBluetoothAdapter.isEnabled()) {
                    if (!this.mBluetoothAdapter.enable()) {
                        PhotoOrCropUtil.getInstance().showToast(this, "Enable bluetooth failed");
                        return true;
                    } else {
                        PhotoOrCropUtil.getInstance().showToast(this, "Bluetooth is enabled");
                        return false;
                    }
                } else {
                    return true;
                }
            } else {
                PhotoOrCropUtil.getInstance().showToast(this, "Bluetooth is enabled");
                return false;
            }
        } else {
            PhotoOrCropUtil.getInstance().showToast(this, "BluetoothManager is null");
            return false;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String s = this.arrayAdapter.getItem(position);
        String address = s.substring(s.indexOf(":") + 1).trim();
        if (this.mBluetoothAdapter.isDiscovering()) {
            this.mBluetoothAdapter.cancelDiscovery();
        }
        if (this.selectDevice == null) {
            this.selectDevice = this.mBluetoothAdapter.getRemoteDevice(address);
        }
        try {
            if (clientSocket == null) {
                clientSocket = selectDevice.createRfcommSocketToServiceRecord(MY_UUID);
                clientSocket.connect();
                os = clientSocket.getOutputStream();
            }
            if (os != null) {
                String text = "test message";
                os.write(text.getBytes("UTF-8"));
            }

            PhotoOrCropUtil.getInstance().showToast(BluetoothActivity.this, "Message sent successfully");
        } catch (IOException e) {
            e.printStackTrace();
            PhotoOrCropUtil.getInstance().showToast(BluetoothActivity.this, "Message sent failed");
        }
    }

    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;
        private BluetoothSocket socket;
        private InputStream is;
        private OutputStream os;

        public AcceptThread() {
            try {
                serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (Exception e) {
            }
        }

        public void run() {
            try {
                socket = serverSocket.accept();
                is = socket.getInputStream();
                os = socket.getOutputStream();
                while (true) {
                    byte[] buffer = new byte[128];
                    int count = is.read(buffer);
                    Message msg = new Message();
                    msg.obj = new String(buffer, 0, count, "utf-8");
                    handler.sendMessage(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
