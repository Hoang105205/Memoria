package com.example.memoria.data.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

/**
 * A Room projection class used to retrieve a FavFolder along with its calculated total word count.
 * Note: This class is NOT annotated with @Entity because it does not represent a standalone
 * table in the database. It is simply a data container to capture the combined results
 * of a custom DAO query (e.g., a query involving a JOIN or COUNT operation).
 */
public class FavFolderWithCount {

    /**
     * The embedded FavFolder entity.
     * The @Embedded annotation instructs Room to automatically map all columns from the
     * underlying 'fav_folders' table directly into this object's fields.
     */
    @Embedded
    public FavFolder folder;

    /**
     * The calculated total number of words within the folder.
     * The @ColumnInfo(name = "word_count") annotation maps this field to the
     * 'word_count' alias generated in the SQL query.
     */
    @ColumnInfo(name = "word_count")
    public int wordCount;
}