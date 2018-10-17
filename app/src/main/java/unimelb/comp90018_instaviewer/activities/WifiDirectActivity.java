package unimelb.comp90018_instaviewer.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.utilities.WifiDirectBroadcastReceiver;

public class WifiDirectActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final int RESULT_LOAD_IMAGE = 2;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    private static final int STATE_MESSAGE_RECEIVED = 1;
    private static final int STATE_MESSAGE_SENT = 2;
    private static final int STATE_MESSAGE_SENT_FAILED = 3;

    private TextView statusView;
    private ImageView imageView;
    private Button btnSend, btnUpload, wifiToggle;

    private ListView lvDevices;
    private ArrayAdapter<String> arrayAdapter;

    public WifiP2pManager.PeerListListener peerListListener;
    public WifiP2pManager.ConnectionInfoListener connectionInfoListener;

    private WifiManager wifimanager;
    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;

    private List<WifiP2pDevice> peers = new ArrayList<>();
    private List<String> deviceNames = new LinkedList<>();
    private WifiP2pDevice[] devices;

    private WifiDirectBroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    private Bitmap bitmap;
    private BitmapDrawable bitmapDrawable;

    private WifiP2pInfo wifiP2pInfo;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_direct);

        this.wifiToggle = findViewById(R.id.wifi_toggle);
        this.statusView = findViewById(R.id.status_view);
        this.imageView = findViewById(R.id.pic_view);
        this.lvDevices = findViewById(R.id.lv_devices);
        this.btnSend = findViewById(R.id.btn_Send);
        this.btnUpload = findViewById(R.id.btn_Upload);

        this.arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                deviceNames);
        this.lvDevices.setAdapter(arrayAdapter);
        this.lvDevices.setOnItemClickListener(this);

        this.initDataStructures();
        this.initListeners();

        new ImgServerAsyncTask().execute();
    }

    public void onClick(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (this.checkPermissionWRITE_EXTERNAL_STORAGE(this)
                && this.checkPermissionREAD_EXTERNAL_STORAGE(this))) {
            p2pManager.discoverPeers(p2pChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    statusView.setText("Discovering Devices");
                }

                @Override
                public void onFailure(int reason) {
                    statusView.setText("Discovery Start Failed");
                }
            });
        }
    }

    public boolean checkPermissionWRITE_EXTERNAL_STORAGE(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                } else {
                    ActivityCompat.requestPermissions(
                            (Activity) context,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context, Manifest.permission.READ_EXTERNAL_STORAGE,
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                } else {
                    ActivityCompat.requestPermissions(
                            (Activity) context,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context,
                           final String permission, final int requestCode) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[]{permission},
                                requestCode);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.onClick(null);
                } else {
                    Toast.makeText(WifiDirectActivity.this, "PERMISSION REQUEST FAILED",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.onClick(null);
                } else {
                    Toast.makeText(WifiDirectActivity.this, "PERMISSION REQUEST FAILED",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final WifiP2pDevice device = devices[position];
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;

        p2pManager.connect(p2pChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(), "Can not connect", Toast.LENGTH_SHORT).show();
                return;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_LOAD_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();
                    imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                }
        }
    }

    private void choosePhoto() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    private void initDataStructures() {
        this.wifimanager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.p2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        this.p2pChannel = p2pManager.initialize(this, getMainLooper(), null);

        this.broadcastReceiver = new WifiDirectBroadcastReceiver(p2pManager, p2pChannel, WifiDirectActivity.this);

        if (wifimanager.isWifiEnabled()) {
            this.wifiToggle.setText("CLOSE WIFI");
        } else {
            this.wifiToggle.setText("OPEN WIFI");
        }

        intentFilter = new IntentFilter();
        intentFilter.addAction(p2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(p2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(p2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(p2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void initListeners() {
        this.wifiToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifimanager.isWifiEnabled()) {
                    wifimanager.setWifiEnabled(false);
                    wifiToggle.setText("OPEN WIFI");
                } else {
                    wifimanager.setWifiEnabled(true);
                    wifiToggle.setText("CLOSE WIFI");
                }
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkPermissionREAD_EXTERNAL_STORAGE(WifiDirectActivity.this)) {
                    choosePhoto();
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ClientSocketHandler(wifiP2pInfo.groupOwnerAddress.getHostAddress()).start();
            }
        });

        this.peerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {
                Collection<WifiP2pDevice> refreshedPeers = peerList.getDeviceList();
                if (!refreshedPeers.equals(peers)) {
                    peers.clear();
                    peers.addAll(refreshedPeers);
                    deviceNames.clear();
                    devices = new WifiP2pDevice[refreshedPeers.size()];

                    int index = 0;
                    for (WifiP2pDevice device : refreshedPeers) {
                        deviceNames.add(device.deviceName + ": " + device.deviceAddress);
                        devices[index] = device;
                        index++;
                    }
                    arrayAdapter.notifyDataSetChanged();
                }

                if (peers.size() == 0) {
                    Toast.makeText(getApplicationContext(), "No device is found", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        };

        this.connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                wifiP2pInfo = info;
                statusView.setText("Connected To: " + info.groupOwnerAddress.getHostAddress());
            }
        };

        this.handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case STATE_MESSAGE_RECEIVED:
                        byte[] readBuffer = (byte[]) msg.obj;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(readBuffer, 0, msg.arg1);
                        imageView.setImageBitmap(bitmap);
                        Toast.makeText(WifiDirectActivity.this, "Image has been updated",
                                Toast.LENGTH_SHORT).show();
                        new ImgServerAsyncTask().execute();
                        break;
                    case STATE_MESSAGE_SENT:
                        Toast.makeText(WifiDirectActivity.this, "Photo has ben sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case STATE_MESSAGE_SENT_FAILED:
                        Toast.makeText(WifiDirectActivity.this, "Photo send failed",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
    }

    public class ClientSocketHandler extends Thread {
        private String serviceAddress;

        public ClientSocketHandler(String serviceAddress) {
            this.serviceAddress = serviceAddress;
        }

        @Override
        public void run() {
            Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(serviceAddress, 4545), 30000);
                OutputStream outputStream = socket.getOutputStream();

                bitmapDrawable = ((BitmapDrawable) imageView.getDrawable());
                bitmap = bitmapDrawable.getBitmap();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

                outputStream.write(out.toByteArray());
                outputStream.flush();
                Message message = Message.obtain();
                message.what = STATE_MESSAGE_SENT;
                handler.sendMessage(message);
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_MESSAGE_SENT_FAILED;
                handler.sendMessage(message);
            } finally {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public class ImgServerAsyncTask extends AsyncTask<Void, Void, Void> {
        public ImgServerAsyncTask() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            byte[] buf = new byte[1024];
            int len;
            try {
                ServerSocket serverSocket = new ServerSocket(4545);
                Socket client = serverSocket.accept();

                InputStream inputstream = client.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                while ((len = inputstream.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
                handler.obtainMessage(STATE_MESSAGE_RECEIVED, out.toByteArray().length, -1, out.toByteArray()).sendToTarget();
                inputstream.close();
                client.close();
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    public void setStatus(String s) {
        this.statusView.setText(s);
    }
}
