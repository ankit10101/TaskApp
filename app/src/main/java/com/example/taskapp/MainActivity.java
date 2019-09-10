package com.example.downloadmanager1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URI;

public class MainActivity extends AppCompatActivity {

    Button button;
    EditText editText;
    private long downloadID;
    private String url;

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                Toast.makeText(MainActivity.this, "Download Completed", Toast.LENGTH_SHORT).show();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.download);
        editText = findViewById(R.id.etURL);
        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (    //Check for permissions
                        ActivityCompat.checkSelfPermission(
                                getBaseContext(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                                == PackageManager.PERMISSION_GRANTED
                ) {
                    beginDownload();
                } else {
                    //else request permissions
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                            12345
                    );
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
    }

    private void beginDownload() {
        File file = new File(getExternalFilesDir(null), "Dummy");
        // Create a DownloadManager.Request with all the information necessary to start the download
        url = editText.getText().toString();
//        if (!url.startsWith("http://") || !url.startsWith("https://")) {
//            url = "https://" + url;
//        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setTitle("Dummy File")// Title of the Download Notification
                .setDescription("Downloading")// Description of the Download Notification
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)// Visibility of the download Notification
                .setDestinationUri(Uri.fromFile(file))// Uri of the destination file.
                .setMimeType(getMimeType(url))
                .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true);// Set if download is allowed on roaming network
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadID = downloadManager.enqueue(request);// enqueue puts the download request in the queue.
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 12345) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                beginDownload();

            } else {
                url = editText.getText().toString();
//                if (!url.startsWith("http://") || !url.startsWith("https://")) {
//                    url = "https://" + url;
//                }
                String type = getMimeType(url);
                if (type.startsWith("image")) {
                    startActivity(new Intent(this, ImageActivity.class).setData(Uri.parse(url)));
                } else {
                    startActivity(new Intent(this, VideoActivity.class).setData(Uri.parse(url)));
                }
            }
        }
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

}
