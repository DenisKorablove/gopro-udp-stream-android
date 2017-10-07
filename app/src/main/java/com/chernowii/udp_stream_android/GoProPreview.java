package com.chernowii.udp_stream_android;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class GoProPreview extends Activity {

    private final OkHttpClient client = new OkHttpClient();

    private Socket socket;

    private static final int SERVERPORT = 8554;
    private static final String SERVER_IP = "10.5.5.9";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_pro_preview);
        Button btn = findViewById(R.id.startStream);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Stream();
            }
        });
        Button btn2 = findViewById(R.id.previewbtn);

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Preview();
            }
        });
        try {

            FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());

            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onFailure() {
                    Toast.makeText(getApplicationContext(),"Failed to load FFmpeg",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess() {
                    Toast.makeText(getApplicationContext(),"Loaded ffmpeg",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFinish() {}
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
        }
    }
    void Stream(){
        //Call http://10.5.5.9/gp/gpControl/execute?p1=gpStream&a1=proto_v2&c1=restart

        final Request startpreview = new Request.Builder()
                .url(HttpUrl.get(URI.create("http://10.5.5.9/gp/gpControl/execute?p1=gpStream&a1=proto_v2&c1=restart")))
                .build();

        client.newCall(startpreview).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Camera not connected!",Toast.LENGTH_SHORT).show();
                }


            }
        });
        try {
            String[] cmd = {"-f", "mpegts", "-i", "udp://:8554", "-f", "mpegts","udp://127.0.0.1:8555/gopro?pkt_size=64"};
            FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());

            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onProgress(String message) {
                    Log.d("FFmpeg",message);

                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(getApplicationContext(),"Stream fail",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(String message) {}

                @Override
                public void onFinish() {}
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
        }
        //Preview();
    }

    void Preview() {
        org.videolan.libvlc.media.VideoView Mediaplayer = new org.videolan.libvlc.media.VideoView(getApplicationContext());
        Mediaplayer.findViewById(R.id.VLC);
        Mediaplayer.setVideoURI(Uri.parse("udp://@:8555/gopro"));
        Mediaplayer.start();
    }


}
