package com.iss;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ResultActivity extends AppCompatActivity implements View.OnClickListener{

    private Integer[] records;
    private File SaveScore;
    private ScoreAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SaveScore = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + "won_time" + ".txt");
        records = getRecords();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        adapter = new ScoreAdapter(this,records);
        ListView listView = findViewById(R.id.listView);
        if (listView != null) {
            listView.setAdapter(adapter);
        }
        Button btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(this);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btnBack) {
            onBackPressed();
        }

        if (id == R.id.btnClear) {
            // btn1 clicked
            clearHistoryRecords();
            Toast.makeText(this,"Cleaned record", Toast.LENGTH_LONG).show();
        }
    }

    private Integer[] getRecords(){
        final List<Integer> rec = new ArrayList<>();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileInputStream fis = new FileInputStream(SaveScore);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
                    String line;
                    while ((line = br.readLine()) != null) {
                        rec.add(Integer.parseInt(line));
                    }
                    br.close();
                    isr.close();
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

        try {
            thread.join();  // Wait for the thread to finish
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Collections.sort(rec);
        return rec.subList(0, Math.min(rec.size(), 5)).toArray(new Integer[0]);
    }

    private void clearHistoryRecords() {

        adapter.clear();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // use downloaded file
                adapter.notifyDataSetChanged();
            }
        });

        // 通知适配器数据发生改变

        try {
            FileOutputStream fos = new FileOutputStream(SaveScore, false);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write("");
            bw.flush();
            bw.close();
            osw.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
