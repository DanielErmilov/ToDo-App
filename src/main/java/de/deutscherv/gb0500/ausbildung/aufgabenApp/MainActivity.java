package de.deutscherv.gb0500.ausbildung.aufgabenApp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    List<Aufgabe> offeneAufgabenListe = new ArrayList<>();
    List<Aufgabe> erledigteAufgabenListe = new ArrayList<>();

    private Alarm alarm;
    private OffeneAufgabenAdapter offeneAufgabenAdapter;
    private ErledigteAufgabenAdapter erledigteAufgabenAdapter;

    private TextView leereOffeneAufgabenNachrichtTextView;
    private TextView leereErledigteAufgabenNachrichtTextView;
    private RecyclerView offeneAufgabenRecyclerView;
    private RecyclerView erledigteAufgabenRecyclerView;

    private AppDatenbank datenbank;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton neueAufgabeKnopf = findViewById(R.id.addAufgabeBtn);

        leereOffeneAufgabenNachrichtTextView = findViewById(R.id.empty_text_view1);

        leereErledigteAufgabenNachrichtTextView = findViewById(R.id.empty_text_view2);

        offeneAufgabenRecyclerView = findViewById(R.id.myRecyclerView1);

        erledigteAufgabenRecyclerView = findViewById(R.id.myRecyclerView2);

        datenbank = Room.databaseBuilder(getApplicationContext(), AppDatenbank.class, "aufgaben.db").build();

        alarm = new Alarm(this, this, datenbank, leereOffeneAufgabenNachrichtTextView,
                leereErledigteAufgabenNachrichtTextView, offeneAufgabenRecyclerView, erledigteAufgabenRecyclerView);

        offeneAufgabenAdapter  = new OffeneAufgabenAdapter(this, alarm, datenbank,
                leereOffeneAufgabenNachrichtTextView, leereErledigteAufgabenNachrichtTextView,
                offeneAufgabenRecyclerView, erledigteAufgabenRecyclerView);

        erledigteAufgabenAdapter  = new ErledigteAufgabenAdapter(this, offeneAufgabenAdapter, alarm,
                datenbank, leereOffeneAufgabenNachrichtTextView, leereErledigteAufgabenNachrichtTextView,
                offeneAufgabenRecyclerView, erledigteAufgabenRecyclerView);

        offeneAufgabenAdapter.setErledigteAufgabenAdapter(erledigteAufgabenAdapter);

        offeneAufgabenRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        offeneAufgabenRecyclerView.setAdapter(this.offeneAufgabenAdapter);

        erledigteAufgabenRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        erledigteAufgabenRecyclerView.setAdapter(this.erledigteAufgabenAdapter);

        alarm.setBeideAdapter(offeneAufgabenAdapter, erledigteAufgabenAdapter);

        ladeAufgabenUndUpdateUI();

        neueAufgabeKnopf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.alertNeueAufgabeErstellen();
                offeneAufgabenAdapter.notifyDataSetChanged();
                erledigteAufgabenAdapter.notifyDataSetChanged();
            }
        });

    }

    private void ladeAufgabenUndUpdateUI() {
        executor.execute(() -> {
            leseDatenbankUndAkualisierListen();

            runOnUiThread(() -> {
                aktualisiereViewSichtbarkeit();
                offeneAufgabenAdapter.notifyDataSetChanged();
                erledigteAufgabenAdapter.notifyDataSetChanged();
            });
        });
        executor.shutdown();
    }

    private void leseDatenbankUndAkualisierListen() {
        offeneAufgabenListe = datenbank.aufgabenDao().bekommeAlleOffeneAufgaben();
        erledigteAufgabenListe = datenbank.aufgabenDao().bekommeAlleErledigteAufgaben();

        offeneAufgabenListe.sort(Comparator.comparing(Aufgabe::getTitel));
        erledigteAufgabenListe.sort(Comparator.comparing(Aufgabe::getTitel));

        offeneAufgabenAdapter.updateListen(offeneAufgabenListe, erledigteAufgabenListe);
        erledigteAufgabenAdapter.updateListen(offeneAufgabenListe, erledigteAufgabenListe);
        alarm.updateListen(offeneAufgabenListe, erledigteAufgabenListe);
    }

    private void aktualisiereViewSichtbarkeit() {
        if (offeneAufgabenListe.isEmpty()) {
            leereOffeneAufgabenNachrichtTextView.setVisibility(View.VISIBLE);
            offeneAufgabenRecyclerView.setVisibility(View.GONE);
        } else {
            leereOffeneAufgabenNachrichtTextView.setVisibility(View.GONE);
            offeneAufgabenRecyclerView.setVisibility(View.VISIBLE);
        }
        if (erledigteAufgabenListe.isEmpty()) {
            leereErledigteAufgabenNachrichtTextView.setVisibility(View.VISIBLE);
            erledigteAufgabenRecyclerView.setVisibility(View.GONE);
        } else {
            leereErledigteAufgabenNachrichtTextView.setVisibility(View.GONE);
            erledigteAufgabenRecyclerView.setVisibility(View.VISIBLE);
        }
    }


}


