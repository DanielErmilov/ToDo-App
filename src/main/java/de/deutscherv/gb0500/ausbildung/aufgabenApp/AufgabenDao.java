package de.deutscherv.gb0500.ausbildung.aufgabenApp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AufgabenDao {
    @Query("SELECT * FROM Aufgabe")
    List<Aufgabe> getAllToDos();

    @Query("SELECT * FROM Aufgabe WHERE erledigt = 1")
    List<Aufgabe> bekommeAlleErledigteAufgaben();

    @Query("SELECT * FROM Aufgabe WHERE erledigt = 0")
    List<Aufgabe> bekommeAlleOffeneAufgaben();

    @Insert
    void fuegeNeueAufgabeHinzu(Aufgabe aufgabe);

    @Update
    void updateAufgabe(Aufgabe aufgabe);

    @Delete
    void loescheToDo(Aufgabe aufgabe);
}



