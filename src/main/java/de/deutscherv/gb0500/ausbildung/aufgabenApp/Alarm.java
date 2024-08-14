package de.deutscherv.gb0500.ausbildung.aufgabenApp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Alarm {
    private final Context mainContext;
    private List<Aufgabe> offeneAufgabenListe;
    private List<Aufgabe> erledigteAufgabenListe;
    private final AppDatenbank datenbank;
    private OffeneAufgabenAdapter offeneAufgabenAdapter;
    private ErledigteAufgabenAdapter erledigteAufgabenAdapter;
    private final Activity mainActivity;
    private final TextView leereOffeneAufgabenNachrichtTextView;
    private final TextView leereErledigteAufgabenNachrichtTextView;
    private final RecyclerView offeneAufgabenRecyclerView;
    private final RecyclerView erledigteAufgabenRecyclerView;

    public Alarm(Activity mainActivity, Context mainContext, AppDatenbank datenbank,
                 TextView leereOffeneAufgabenNachrichtTextView,
                 TextView leereErledigteAufgabenNachrichtTextView,
                 RecyclerView offeneAufgabenRecyclerView,
                 RecyclerView erledigteAufgabenRecyclerView) {
        this.mainContext = mainContext;
        this.mainActivity = mainActivity;
        this.datenbank = datenbank;
        this.leereOffeneAufgabenNachrichtTextView = leereOffeneAufgabenNachrichtTextView;
        this.leereErledigteAufgabenNachrichtTextView = leereErledigteAufgabenNachrichtTextView;
        this.offeneAufgabenRecyclerView = offeneAufgabenRecyclerView;
        this.erledigteAufgabenRecyclerView = erledigteAufgabenRecyclerView;
    }

    public void setBeideAdapter(OffeneAufgabenAdapter offeneAufgabenAdapter, ErledigteAufgabenAdapter erledigteAufgabenAdapter) {
        this.offeneAufgabenAdapter = offeneAufgabenAdapter;
        this.erledigteAufgabenAdapter = erledigteAufgabenAdapter;
    }

    public void updateListen(List<Aufgabe> offeneAufgabenListe, List<Aufgabe> erledigteAufgabenListe) {
        this.offeneAufgabenListe = offeneAufgabenListe;
        this.erledigteAufgabenListe = erledigteAufgabenListe;
    }

    public void alertAufgabeDetails(Aufgabe uebergebeneAufgabe) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mainContext);
        View dialogView = LayoutInflater.from(this.mainContext).inflate(R.layout.aufgabendetails_layout, null);
        TextView setID = dialogView.findViewById(R.id.setID);
        setID.setText(String.valueOf(uebergebeneAufgabe.getId()));
        TextView setTitel = dialogView.findViewById(R.id.setTitel);
        setTitel.setText(uebergebeneAufgabe.getTitel());
        TextView setBeschreibung = dialogView.findViewById(R.id.setBeschreibung);
        if (uebergebeneAufgabe.getBeschreibung().isEmpty()) {
            setBeschreibung.setText(R.string.keine_beschreibung_vorhanden);
        } else {
            setBeschreibung.setText(uebergebeneAufgabe.getBeschreibung());
        }
        CheckBox setCheckBox = dialogView.findViewById(R.id.setCheckBox);
        setCheckBox.setChecked(uebergebeneAufgabe.isErledigt());
        builder.setView(dialogView);
        setCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    offeneAufgabenListe.remove(uebergebeneAufgabe);
                    uebergebeneAufgabe.changeErledigt();
                    erledigteAufgabenListe.add(uebergebeneAufgabe);
                } else {
                    erledigteAufgabenListe.remove(uebergebeneAufgabe);
                    uebergebeneAufgabe.changeErledigt();
                    offeneAufgabenListe.add(uebergebeneAufgabe);
                }
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    datenbank.aufgabenDao().updateAufgabe(uebergebeneAufgabe);

                    mainActivity.runOnUiThread(() -> {
                        updateUI();

                    });
                });
                executor.shutdown();
            }
        });

        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.show();
    }

    public void alertBestaetigeLoeschvorgang(Aufgabe uebergebeneAufgabe) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mainContext);
        builder.setMessage("Soll die Aufgabe '" + uebergebeneAufgabe.getTitel() + "' wirklich gelöscht werden?");
        builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    datenbank.aufgabenDao().loescheToDo(uebergebeneAufgabe);
                    offeneAufgabenListe = datenbank.aufgabenDao().bekommeAlleOffeneAufgaben();
                    erledigteAufgabenListe = datenbank.aufgabenDao().bekommeAlleErledigteAufgaben();

                    mainActivity.runOnUiThread(() -> {
                        updateUI();
                    });

                });
                executor.shutdown();
            }
        });
        builder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    public void alertNeueAufgabeErstellen() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mainContext);
        View dialogView = LayoutInflater.from(this.mainContext).inflate(R.layout.dialog_neue_todo, null);
        EditText inputTitel = dialogView.findViewById(R.id.inputTitel);
        EditText inputBeschreibung = dialogView.findViewById(R.id.inputBeschreibung);
        builder.setView(dialogView);
        builder.setPositiveButton("Hinzufügen", new DialogInterface.OnClickListener() {
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                if (inputTitel.getText().toString().isEmpty()) {
                    alertEmptyTitel();
                } else {
                    Aufgabe neueAufgabe = new Aufgabe(inputTitel.getText().toString(), inputBeschreibung.getText().toString(), false);

                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        datenbank.aufgabenDao().fuegeNeueAufgabeHinzu(neueAufgabe);
                        offeneAufgabenListe.add(neueAufgabe);

                        offeneAufgabenListe = datenbank.aufgabenDao().bekommeAlleOffeneAufgaben();
                        erledigteAufgabenListe = datenbank.aufgabenDao().bekommeAlleErledigteAufgaben();

                        mainActivity.runOnUiThread(() -> {
                            updateUI();
                        });

                    });
                    executor.shutdown();
                }
            }
        });
        builder.setNegativeButton("Beenden", new DialogInterface.OnClickListener() {
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    public void alertEmptyTitel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mainContext);
        builder.setMessage("Aufgabe kann nicht erstellt werden. Titel darf nicht leer bleiben");
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private void updateUI() {
        offeneAufgabenListe.sort(Comparator.comparing(Aufgabe::getTitel));
        erledigteAufgabenListe.sort(Comparator.comparing(Aufgabe::getTitel));

        offeneAufgabenAdapter.updateListen(offeneAufgabenListe, erledigteAufgabenListe);
        erledigteAufgabenAdapter.updateListen(offeneAufgabenListe, erledigteAufgabenListe);
        updateListen(offeneAufgabenListe, erledigteAufgabenListe);

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
        offeneAufgabenAdapter.notifyDataSetChanged();
        erledigteAufgabenAdapter.notifyDataSetChanged();
    }


}