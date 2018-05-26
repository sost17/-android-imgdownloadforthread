package com.col.commo.internet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    public String IMG_PATH;
    public String _IMG_PATH = "http://www.csost.com/img/im1.png";
    public String T_IMG_PATH = "http://lorempixel.com/800/800";
    private Button btn_getPic,btn_cancel;
    private EditText address;
    private ImageView img;
    private ProgressBar progressBar;
    private TextView tv_progress;
    private TextView tvCount;
    Bitmap bit;
    Task task;
    int count = 0;
    int tag = 0,kill_th = 0,s_tag = -1;
    final String filename = "local_temp_image";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btn_getPic = (Button)findViewById(R.id.btn_load_pic);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        img = (ImageView)findViewById(R.id.imageView1);
        progressBar = (ProgressBar)findViewById(R.id.progressBar1);
        tv_progress = (TextView)findViewById(R.id.tv_progress);
        tvCount = (TextView) findViewById(R.id.textView);
        address = (EditText) findViewById(R.id.editText);
        btn_cancel.setEnabled(false);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                kill_th = 1;
                s_tag = 0;
                img.setImageBitmap(bit);
                tv_progress.setText("取消成功");
                progressBar.setProgress(0);
                address.setText(IMG_PATH);
                btn_cancel.setEnabled(false);
                btn_getPic.setEnabled(true);
            }
        });
        btn_getPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int flg = 1;
                kill_th = 0;

                String Cloud_IMG_PATH  = address.getText().toString();
                if((Cloud_IMG_PATH.equals("图片地址（默认为个人云的图片）"))){
                    IMG_PATH = T_IMG_PATH;
                    if (task == null) {
                        task = new Task();
                    }
                    count = 0;
                    tag = 0;
                    task.execute(1);
                    final myThread th = new myThread();
                    th.start();
                } else if(Cloud_IMG_PATH.equals("")){
                    flg = 0;
                    Toast.makeText(MainActivity.this,"图片地址为空",Toast.LENGTH_LONG).show();
                } else if(Cloud_IMG_PATH.equals("localhost")){
                    IMG_PATH = _IMG_PATH;
                    if (task == null) {
                        task = new Task();
                    }
                    count = 0;
                    tag = 0;
                    task.execute(1);
                    final myThread th = new myThread();
                    th.start();
                } else if(Cloud_IMG_PATH.equals("default")){
                    IMG_PATH = T_IMG_PATH;
                    if (task == null) {
                        task = new Task();
                    }
                    count = 0;
                    tag = 0;
                    task.execute(1);
                    final myThread th = new myThread();
                    th.start();
                } else{
                    IMG_PATH = Cloud_IMG_PATH;
                    if (task == null) {
                        task = new Task();
                    }
                    count = 0;
                    tag = 0;
                    task.execute(1);
                    final myThread th = new myThread();
                    th.start();
                }
                if(flg == 1){
                    img.setImageResource(R.drawable.jiazai);
                    tv_progress.setText("下载数据 "+0+"%");
                    progressBar.setProgress(0);
                    btn_getPic.setEnabled(false);
                    btn_cancel.setEnabled(true);
                    address.setText(IMG_PATH);
                } else if(flg == 0){                    //图片地址为空
                    img.setImageBitmap(bit);
                    address.setText("");
                    progressBar.setProgress(0);
                    btn_getPic.setEnabled(true);
                    tv_progress.setText("");
                    tvCount.setText("计时");
                }


            }
        });

    }

    class myThread extends Thread{
        final Handle hd = new Handle();

        public void run() {
            URL url;
            HttpURLConnection conn = null;
            InputStream in = null;
            OutputStream out = null;
            int th_kill = 0;

            try{
                url = new URL(IMG_PATH);
                conn = (HttpURLConnection) url.openConnection();
                System.out.println(conn);

                conn.setDoInput(true);
                conn.setDoOutput(false);
                conn.setConnectTimeout(5*1000);
                in = conn.getInputStream();
                out = openFileOutput(filename,MODE_PRIVATE);
                byte[] buff = new byte[1024];
                int seg = 0 ;
                final long total = conn.getContentLength();

                long current = 0;
                int flag = 0;

                while ((seg = in.read(buff))!= -1) {
                    if(kill_th == 1){
                        th_kill = 1;
                        break;
                    }
                    out.write(buff, 0, seg);

                    current += seg;
                    int progress= 0;
                    if(total == -1) {
                        flag +=1;
                        progress += flag;
                        if(progress >= 100) progress = 100;
                    }else {
                        progress = (int) ((float)current/(float)total * 100f);
                    }

                    Message msg = Message.obtain();
                    msg.what = 0x100;
                    msg.arg1 = progress;

                    hd.sendMessage(msg);
                }
                System.out.println(seg);
                Message msg = Message.obtain();
                msg.what = 0x100;
                msg.arg1 = 100;
                if(seg == -1 ) hd.sendMessage(msg);
                else tag = 1;


                if(th_kill != 1){
                    hd.sendEmptyMessage(0x101);
                }


            } catch (SocketTimeoutException e){
                System.out.println("连接超时");
                tag = 1;
                Message msg = Message.obtain();
                msg.what = 0x123;
                msg.arg1 = 0;
                hd.sendMessage(msg);
                e.printStackTrace();
            }catch (ConnectException e){
                System.out.println("连接错误");
                tag = 1;
                Message msg = Message.obtain();
                msg.what = 0x123;
                msg.arg1 = -3;
                hd.sendMessage(msg);
                e.printStackTrace();
            }catch (UnknownHostException e){
                System.out.println("Host找不到");
                tag = 1;
                Message msg = Message.obtain();
                msg.what = 0x123;
                msg.arg1 = -2;
                hd.sendMessage(msg);
                e.printStackTrace();
            }catch (FileNotFoundException e) {
                System.out.println("找不到");
                e.printStackTrace();
            } catch (MalformedURLException e) {
                tag = 1;
                Message msg = Message.obtain();
                msg.what = 0x123;
                msg.arg1 = -1;
                hd.sendMessage(msg);
                System.out.println("URL错误");
                e.printStackTrace();
            } catch (Exception e){
                tag = 1;
                Message msg = Message.obtain();
                msg.what = 0x123;
                msg.arg1 = -3;
                hd.sendMessage(msg);
                System.out.println("错误");
                e.printStackTrace();
            }finally {
                if (conn != null) {

                    conn.disconnect();
                }
                if (in != null) {

                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {

                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    public class Handle extends Handler{
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x100:
                    tag = 0;
                    img.setImageResource(R.drawable.jiazai);
                    tv_progress.setText("下载数据 "+msg.arg1+"%");
                    progressBar.setProgress(msg.arg1);
                    btn_getPic.setEnabled(false);
                    address.setText(IMG_PATH);
                    break;

                case 0x101:
                    img.setImageBitmap(BitmapFactory.decodeFile(getFileStreamPath(filename).getAbsolutePath()));
                    if ((BitmapFactory.decodeFile(getFileStreamPath(filename).getAbsolutePath())) == null){
                        tv_progress.setText("没有图片");
                        img.setImageBitmap(bit);
                        progressBar.setProgress(0);
                        btn_cancel.setEnabled(false);
                        task.onCancelled();
                        tag = 1;
                        break;
                    } else if (tag == 0){
                        tv_progress.setText("下载完成");
                        btn_cancel.setEnabled(false);
                        tag = 1;
                        s_tag = 1;
                        break;
                    }
                case 0x123:
                    tag = 1;
                    s_tag = 1;
                    if(msg.arg1 == -1){
                        img.setImageBitmap(bit);
                        address.setText(IMG_PATH);
                        btn_cancel.setEnabled(false);
                        tvCount.setText("");
                        tv_progress.setText("图片地址错误");
                        task.onCancelled();

                    } else if(msg.arg1 == 0){
                        img.setImageBitmap(bit);
                        address.setText(IMG_PATH);
                        btn_cancel.setEnabled(false);
                        tvCount.setText("");
                        tv_progress.setText("连接超时，请检查网络和域名");
                        task.onCancelled();
                    }else if(msg.arg1 == -2){
                        img.setImageBitmap(bit);
                        address.setText(IMG_PATH);
                        btn_cancel.setEnabled(false);
                        tvCount.setText("");
                        tv_progress.setText("域名解析失败，请检查域名和网络");
                        task.onCancelled();
                    }else if(msg.arg1 == -3){
                        img.setImageBitmap(bit);
                        address.setText(IMG_PATH);
                        btn_cancel.setEnabled(false);
                        tvCount.setText("");
                        tv_progress.setText("网络连接失败，请检查网络");
                        task.onCancelled();
                    }
                    task.onPostExecute("result");
                    break;
            }
        }
    }

    private class Task extends AsyncTask<Integer,Integer,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Integer... params) {
            while(true){
                if ( tag == 1){
                    break;
                } else {
                    count += 1;
                    publishProgress(count);
                    System.out.println("没死没死没死没死没死没死没死没死没死没死没死没死");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (isCancelled()) {
                    return null;
                }
            }
            return "";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(progress);
            if (isCancelled()) {
                return;
            }

            tvCount.setText("时间：" + count);

        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(tag == 1 && s_tag == 0){
                img.setImageBitmap(bit);
                tv_progress.setText("取消成功");
                progressBar.setProgress(0);
                address.setText(IMG_PATH);
            }

            btn_getPic.setEnabled(true);
            btn_cancel.setEnabled(false);

            task = null;
        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();
            btn_getPic.setEnabled(false);

        }
    }
}
