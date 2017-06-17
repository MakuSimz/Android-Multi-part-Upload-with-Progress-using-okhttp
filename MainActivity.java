package com.okhttp.root.myapplication;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {
    private OkHttpClient client;
    private String response;
    private Button fileUploadBtn;
    private ProgressBar progressBar;
    TextView txt;
    Activity context;

    protected static String IPADDRESS = "http://10.42.0.1/hugefile/save_file.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new OkHttpClient();
        txt = (TextView) findViewById(R.id.output);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);
        fileUploadBtn = (Button) findViewById(R.id.btnFileUpload);
        context = this;
        pickFile();
    }

    private void pickFile() {

        fileUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialFilePicker()
                        .withActivity(MainActivity.this)
                        .withRequestCode(10).start();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);


        final File file = new File(data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
        new AsyncTask<Integer, Integer, String>() {
            NotificationManager mNotifyManager;
            NotificationCompat.Builder mBuilder;

            @Override
            protected String doInBackground(Integer... params) {
                try {
                    //response= uploadFiles(file);
                    // publishProgress();
                    String fileExtention = getFileExt(file.getName());
                    String filename = file.getName();
                    Log.d("getAbsolutePath ", file.getAbsolutePath() + "");
                    Log.d("getCanonicalPath ", file.getCanonicalPath() + "");
                    MultipartBody body = RequestBuilder.uploadRequestBody(filename, fileExtention, "someUploadToken", file);

                    CountingRequestBody monitoredRequest = new CountingRequestBody(body, new CountingRequestBody.Listener() {
                        @Override
                        public void onRequestProgress(long bytesWritten, long contentLength) {
                            //Update a progress bar with the following percentage
                            float percentage = 100f * bytesWritten / contentLength;
                            if (percentage >= 0) {
                                //TODO: Progress bar
                                publishProgress(Math.round(percentage));
                                Log.d("progress ", percentage + "");
                            } else {
                                //Something went wrong
                                Log.d("No progress ", 0 + "");
                            }
                        }
                    });
                    response = ApiCall.POST(client, IPADDRESS, monitoredRequest);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }

            @Override
            protected void onPostExecute(String result) {
                progressBar.setVisibility(View.GONE);
                txt.setText(result);
                mBuilder.setContentText("Upload complete");
                // Removes the progress bar
                mBuilder.setProgress(0, 0, false);
                mNotifyManager.notify(0, mBuilder.build());
            }

            @Override
            protected void onPreExecute() {
                txt.setText("Task Starting...");
                mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mBuilder = new NotificationCompat.Builder(context);
                mBuilder.setContentTitle("Uploading")
                        .setContentText("Upload in progress")
                        .setSmallIcon(R.drawable.ic_certificate_box);
                Toast.makeText(context, "Uploading files... The upload progress is on notification bar.", Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onProgressUpdate( Integer... values) {
                txt.setText("Running..." + values[0]);
                progressBar.setProgress(values[0]);
                if ((values[0])%25==0){
                    mBuilder.setProgress(100, values[0], false);
                    // Displays the progress bar on notification
                    mNotifyManager.notify(0, mBuilder.build());
                }
                
            }


        }.execute();

    }


    public static String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }
}
