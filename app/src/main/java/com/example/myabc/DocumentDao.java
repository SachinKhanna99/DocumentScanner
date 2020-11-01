package com.example.myabc;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DocumentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveAll(List<com.example.myabc.Document> documents);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(com.example.myabc.Document document);

    @Query("SELECT * FROM Document ORDER BY documentId DESC")
    LiveData<List<com.example.myabc.Document>> findAll();

    @Query("SELECT * FROM Document WHERE name like :text ORDER BY documentId DESC")
    LiveData<List<com.example.myabc.Document>> search(String text );

    @Update
    void update( com.example.myabc.Document document );

    @Delete
    void delete( com.example.myabc.Document document );
}
