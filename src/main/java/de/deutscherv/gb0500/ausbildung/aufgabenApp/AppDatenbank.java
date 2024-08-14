package de.deutscherv.gb0500.ausbildung.aufgabenApp;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Aufgabe.class}, version = 1)
public abstract class AppDatenbank extends RoomDatabase {
    public abstract AufgabenDao aufgabenDao();
}





