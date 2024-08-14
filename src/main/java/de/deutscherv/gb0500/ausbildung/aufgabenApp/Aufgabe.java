package de.deutscherv.gb0500.ausbildung.aufgabenApp;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Aufgabe {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "titel")
    private final String titel;
    @ColumnInfo(name = "beschreibung")
    private final String beschreibung;
    @ColumnInfo(name = "erledigt")
    private boolean erledigt;

    public Aufgabe(String titel, String beschreibung, Boolean erledigt) {
        this.titel=titel;
        this.beschreibung=beschreibung;
        this.erledigt=erledigt;
    }

    public void changeErledigt() {
        erledigt=!erledigt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitel() {
        return titel;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public boolean isErledigt() {
        return erledigt;
    }

}


