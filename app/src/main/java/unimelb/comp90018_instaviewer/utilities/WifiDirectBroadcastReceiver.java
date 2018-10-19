package unimelb.comp90018_instaviewer.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;
import unimelb.comp90018_instaviewer.activities.WifiDirectActivity;

/**
 * Created by xxq1 on 2018/10/18.
 */
public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;
    private WifiDirectActivity context;

    public WifiDirectBroadcastReceiver(WifiP2pManager p2pManager, WifiP2pManager.Channel p2pChannel, WifiDirectActivity activity) {
        this.p2pManager = p2pManager;
        this.p2pChannel = p2pChannel;
        this.context = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (p2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
        }
        else if (p2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            p2pManager.requestPeers(p2pChannel, this.context.peerListListener);
        }
        else if (p2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                p2pManager.requestConnectionInfo(p2pChannel, this.context.connectionInfoListener);
            } else {
                this.context.setStatus("Your Device is Disconnected.");
            }
        } else if (p2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
        }
    }
}