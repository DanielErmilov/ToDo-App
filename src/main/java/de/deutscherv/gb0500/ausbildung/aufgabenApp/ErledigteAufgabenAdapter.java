package de.deutscherv.gb0500.ausbildung.aufgabenApp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ErledigteAufgabenAdapter extends RecyclerView.Adapter<ErledigteAufgabenAdapter.ErledigteAufgabenHolder> {
    private final Context mainContext;
    private List<Aufgabe> offeneAufgabenListe;
    private List<Aufgabe> erledigteAufgabenListe;
    private final AppDatenbank datenbank;
    private final OffeneAufgabenAdapter offeneAufgabenAdapter;
    private final Alarm alarm;
    private final TextView leereOffeneAufgabenNachrichtTextView;
    private final TextView leereErledigteAufgabenNachrichtTextView;
    private final RecyclerView offeneAufgabenRecyclerView;
    private final RecyclerView erledigteAufgabenRecyclerView;


    public ErledigteAufgabenAdapter(Context mainContext,
                                    OffeneAufgabenAdapter offeneAufgabenAdapter,
                                    Alarm alarm, AppDatenbank datenbank,
                                    TextView leereOffeneAufgabenNachrichtTextView,
                                    TextView leereErledigteAufgabenNachrichtTextView,
                                    RecyclerView offeneAufgabenRecyclerView,
                                    RecyclerView erledigteAufgabenRecyclerView) {
        this.mainContext = mainContext;
        this.offeneAufgabenAdapter = offeneAufgabenAdapter;
        this.alarm = alarm;
        this.datenbank = datenbank;
        this.leereOffeneAufgabenNachrichtTextView = leereOffeneAufgabenNachrichtTextView;
        this.leereErledigteAufgabenNachrichtTextView = leereErledigteAufgabenNachrichtTextView;
        this.offeneAufgabenRecyclerView = offeneAufgabenRecyclerView;
        this.erledigteAufgabenRecyclerView = erledigteAufgabenRecyclerView;
    }

    @Override
    public ErledigteAufgabenHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.mainContext).inflate(R.layout.erledigteaufgaben_layout, parent, false);
        return new ErledigteAufgabenHolder(view);
    }

    @Override
    public int getItemCount() {
        return this.erledigteAufgabenListe.size();
    }

    public void updateListen(List<Aufgabe> offeneAufgabenListe, List<Aufgabe> erledigteAufgabenListe) {
        this.offeneAufgabenListe = offeneAufgabenListe;
        this.erledigteAufgabenListe = erledigteAufgabenListe;
    }


    public static class ErledigteAufgabenHolder extends RecyclerView.ViewHolder {
        private final TextView textTitel;
        private final CheckBox checkBox;
        private final ImageView loeschIcon;
        private final LinearLayout myLinearLayout;


        public ErledigteAufgabenHolder(View itemView) {
            super(itemView);
            this.myLinearLayout = itemView.findViewById(R.id.myLinearLayout);
            this.textTitel = itemView.findViewById(R.id.textViewItem);
            this.checkBox = itemView.findViewById(R.id.checkBox);
            this.loeschIcon = itemView.findViewById(R.id.loeschIcon);

        }

        void SetDetails(Aufgabe aufgabe) {
            this.textTitel.setText(aufgabe.getTitel());
        }
    }

    @Override
    public void onBindViewHolder(ErledigteAufgabenHolder holder, int position) {
        Aufgabe aufgabe = this.erledigteAufgabenListe.get(position);
        holder.checkBox.setChecked(true);
        holder.SetDetails(aufgabe);

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    Aufgabe gecheckteAufgabe = erledigteAufgabenListe.get(adapterPosition);
                    if (!isChecked) {
                        erledigteAufgabenListe.remove(gecheckteAufgabe);
                        gecheckteAufgabe.changeErledigt();
                        offeneAufgabenListe.add(gecheckteAufgabe);
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
                            datenbank.aufgabenDao().updateAufgabe(gecheckteAufgabe);
                        });
                        executor.shutdown();
                        offeneAufgabenListe.sort(Comparator.comparing(Aufgabe::getTitel));
                        erledigteAufgabenListe.sort(Comparator.comparing(Aufgabe::getTitel));

                        updateListen(offeneAufgabenListe, erledigteAufgabenListe);
                        offeneAufgabenAdapter.updateListen(offeneAufgabenListe, erledigteAufgabenListe);
                        alarm.updateListen(offeneAufgabenListe, erledigteAufgabenListe);

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
                        notifyItemRemoved(adapterPosition);
                        offeneAufgabenAdapter.notifyDataSetChanged();
                    }

                }
            }
        });
        holder.loeschIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.alertBestaetigeLoeschvorgang(aufgabe);
            }
        });
        holder.myLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.alertAufgabeDetails(aufgabe);
            }
        });
    }

}
