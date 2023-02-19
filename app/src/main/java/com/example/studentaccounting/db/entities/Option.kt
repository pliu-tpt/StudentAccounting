package com.example.studentaccounting.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName= "option_table")
data class Option(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "option_id")
    var id: Int,

    @ColumnInfo(name = "option_name")
    var name: String,

    @ColumnInfo(name = "option_color")
    var color: String?, // hexadecimal string encoding the option's color
)

// One way to implement categories without the hassle of migrating the DB every time a new category
// is introduced is to create a UI that facilitates the selection of a String from
// the already existing list of Strings... (ie from a pure UI standpoint)


