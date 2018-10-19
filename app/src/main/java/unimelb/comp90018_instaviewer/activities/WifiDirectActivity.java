package unimelb.comp90018_instaviewer.activities;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.concurrent.atomic.AtomicInteger;
import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.utilities.DeviceInformation;
import unimelb.comp90018_instaviewer.utilities.MyAdapter;
import unimelb.comp90018_instaviewer.utilities.WifiDirectBroadcastReceiver;

public class WifiDirectActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final int RESULT_LOAD_IMAGE = 2;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    private static final int STATE_PHOTO_RECEIVED = 1;
    private static final int STATE_PHOTO_SENT = 2;
    private static final int STATE_PHOTO_SENT_FAILED = 3;
    private static final int STATE_TO_SEND_PHOTO = 4;
    private static final int STATE_MSG = 5;
    private Object lock = new Object();

    private TextView statusView;
    private ImageView imageView;

    private ListView lvDevices;
    private MyAdapter myAdapter;

    public WifiP2pManager.PeerListListener peerListListener;
    public WifiP2pManager.ConnectionInfoListener connectionInfoListener;

    private WifiManager wifimanager;
    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;
    private List<WifiP2pDevice> peers = new ArrayList<>();

    private WifiDirectBroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    private Bitmap bitmap;
    private BitmapDrawable bitmapDrawable;

    private Handler handler;
    private float mPosX;
    private float mPosY;
    private float mCurrentPosX;
    private float mCurrentPosY;
    private volatile boolean upReacted = false;
    private volatile boolean isSendingPhoto = false;
    private volatile AtomicInteger currentSendStatus = new AtomicInteger();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_direct);

        this.statusView = findViewById(R.id.status_view);
        this.imageView = findViewById(R.id.pic_view);
        this.lvDevices = findViewById(R.id.lv_devices);

        this.myAdapter = new MyAdapter();
        this.myAdapter.setContext(this);
        this.lvDevices.setAdapter(myAdapter);

        this.initDataStructures();
        this.initListeners();
        new ImgServerAsyncTask().execute();
    }

    public void onClick(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (this.checkPermissionWRITE_EXTERNAL_STORAGE(this)
                && this.checkPermissionREAD_EXTERNAL_STORAGE(this))) {
            this.myAdapter.setData(new LinkedList());
            this.myAdapter.notifyDataSetChanged();
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

    //check write permission
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

    //check read permission
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

    //click the listed device
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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

    //choose photo from the gallery
    private void choosePhoto() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    //behaviors of the wifi manager
    private void initDataStructures() {
        this.wifimanager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.p2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        this.p2pChannel = p2pManager.initialize(this, getMainLooper(), null);

        this.broadcastReceiver = new WifiDirectBroadcastReceiver(p2pManager, p2pChannel, WifiDirectActivity.this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(p2pManager.WIFI_P2P_STATE_CHANGED_ACTION); // when wifi switches on/off
        intentFilter.addAction(p2pManager.WIFI_P2P_PEERS_CHANGED_ACTION); // the list of devices refreshed
        intentFilter.addAction(p2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION); //
        intentFilter.addAction(p2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void connectDevice(final WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;

        p2pManager.connect(p2pChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
                currentSendStatus.set(1);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListeners() {
        this.imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isSendingPhoto) {
                    Toast.makeText(getApplicationContext(), "Last photo is broadcasting, wait for a moment", Toast.LENGTH_SHORT).show();
                    return false;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mPosX = event.getX();
                        mPosY = event.getY();
                        upReacted = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mCurrentPosX = event.getX();
                        mCurrentPosY = event.getY();
                        if (Math.abs(mCurrentPosX - mPosX) >= 100 && upReacted == false) {
                            upReacted = true;
                            if (isSendingPhoto == false) {
                                handler.obtainMessage(STATE_TO_SEND_PHOTO, 0, 0, null).sendToTarget();
                            } else {
                                Toast.makeText(getApplicationContext(), "Last photo is broadcasting, wait for a moment", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mCurrentPosX = event.getX();
                        mCurrentPosY = event.getY();
                        if (Math.abs(mCurrentPosX - mPosX) < 2 && Math.abs(mCurrentPosY - mPosY) < 2) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkPermissionREAD_EXTERNAL_STORAGE(WifiDirectActivity.this)) {
                                choosePhoto();
                            }
                        }
                        break;
                    default:
                        break;
                }

                return true;
            }


        });

        // the devices in the same local network
        this.peerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {
                Collection<WifiP2pDevice> refreshedPeers = peerList.getDeviceList();
                if (!refreshedPeers.equals(peers)) {
<<<<<<< HEAD
                    peers.clear();
                    peers.addAll(refreshedPeers);
                    deviceNames.clear();
                    devices = getFollowers(refreshedPeers);

                    arrayAdapter.notifyDataSetChanged();
=======
                    synchronized (lock) {
                        peers.clear();
                        peers.addAll(refreshedPeers);
                    }
                    List<DeviceInformation> deviceList = new LinkedList<>();
                    for (WifiP2pDevice device : refreshedPeers) {
                        /*
                           an if-else statement should be added here in order to list only
                           current users'friends
                        */
                        DeviceInformation deviceInformation = new DeviceInformation();
                        deviceInformation.setDeviceName(device.deviceName);
                        deviceInformation.setAddress(device.deviceAddress);
                        deviceList.add(deviceInformation);
                    }
                    myAdapter.setData(deviceList);
                    myAdapter.notifyDataSetChanged();
>>>>>>> 690297236f7edbe35e94db1aa0c7ea7df7f69387
                }

                if (peers.size() == 0) {
                    Toast.makeText(getApplicationContext(), "No device is found", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        };

        // works when connecting or connected
        this.connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                //when passively connected by others
                if (info.groupFormed && info.isGroupOwner) {
                    statusView.setText("Be Connected by " + info.groupOwnerAddress.getHostAddress());
                    if (isSendingPhoto) {
                        currentSendStatus.set(1);
                    }
                } else if (info.groupFormed) { //when actively collects others
                    statusView.setText("Connected to " + info.groupOwnerAddress.getHostAddress());
                    if (isSendingPhoto) {
                        new ClientSocketHandler(info.groupOwnerAddress.getHostAddress()).start();
                    }
                }
            }
        };

        this.handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case STATE_PHOTO_RECEIVED:
                        byte[] readBuffer = (byte[]) msg.obj;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(readBuffer, 0, msg.arg1);
                        imageView.setImageBitmap(bitmap);
                        new ImgServerAsyncTask().execute();
                        break;
                    case STATE_PHOTO_SENT:
                        currentSendStatus.set(2);
                        break;
                    case STATE_PHOTO_SENT_FAILED:
                        currentSendStatus.set(1);
                        break;
                    case STATE_TO_SEND_PHOTO:
                        broadcastPhoto();
                        break;
                    case STATE_MSG:
                        Toast.makeText(WifiDirectActivity.this, (String) msg.obj,
                                Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
    }
    /* Rio's code later */
    private WifiP2pDevice[] getFollowers(Collection<WifiP2pDevice> refreshedPeers) {
        WifiP2pDevice[] followers = new WifiP2pDevice[refreshedPeers.size()];
        int index = 0;

        for (WifiP2pDevice device : refreshedPeers) {
            /*
               an if-else statement should be added here in order to list only
               current users'friends
            */

            deviceNames.add(device.deviceName + ": " + device.deviceAddress);
            followers[index] = device;
            index++;
        }

        return followers;
    }

    private void broadcastPhoto() {
        if (this.isSendingPhoto == false) {
            this.isSendingPhoto = true;
            synchronized (lock) {
                Toast.makeText(WifiDirectActivity.this, "start to broadcast photo",
                        Toast.LENGTH_LONG).show();
                new PhotoSenderMonitor().start();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Last photo is broadcasting, wait for a moment", Toast.LENGTH_LONG).show();
        }
    }

    private class ClientSocketHandler extends Thread {
        private String serviceAddress;

        public ClientSocketHandler(String serviceAddress) {
            this.serviceAddress = serviceAddress;
        }

        @Override
        public void run() {
            Socket socket = new Socket();
            try {
                // build connection
                socket.connect(new InetSocketAddress(serviceAddress, 4545), 30000);
                OutputStream outputStream = socket.getOutputStream();

                bitmapDrawable = ((BitmapDrawable) imageView.getDrawable());
                bitmap = bitmapDrawable.getBitmap();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

                outputStream.write(out.toByteArray());
                outputStream.flush();
                Message message = Message.obtain();
                message.what = STATE_PHOTO_SENT;
                handler.sendMessage(message);
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_PHOTO_SENT_FAILED;
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

    private class ImgServerAsyncTask extends AsyncTask<Void, Void, Void> {
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

                handler.obtainMessage(STATE_PHOTO_RECEIVED, out.toByteArray().length, -1, out.toByteArray()).sendToTarget();
                inputstream.close();
                client.close();
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class PhotoSenderMonitor extends Thread {
        private List<WifiP2pDevice> toBeSentPeers = new LinkedList<>();
        public PhotoSenderMonitor() {
            for (WifiP2pDevice peer : peers) {
                this.toBeSentPeers.add(peer);
            }
        }
        @Override
        public void run() {
            for (WifiP2pDevice device : toBeSentPeers) {
                for (int i = 0; i < 3; i++) {
                    unregisterReceiver(broadcastReceiver);
                    registerReceiver(broadcastReceiver, intentFilter);
                    currentSendStatus.set(0);
                    connectDevice(device);
                    long start = System.currentTimeMillis();
                    while (true) {
                        if (currentSendStatus.get() == 1 || currentSendStatus.get() == 2) {
                            break;
                        } else {
                            if (System.currentTimeMillis() - start >= 30000) {
                                break;
                            }
                        }
                    }

                    if (currentSendStatus.get() == 2) {
                        break;
                    } else {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            handler.obtainMessage(STATE_MSG, 0, 0, "photo broadcast is done").sendToTarget();
            isSendingPhoto = false;
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
