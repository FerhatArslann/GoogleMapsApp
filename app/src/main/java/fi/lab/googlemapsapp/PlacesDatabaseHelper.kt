package fi.lab.googlemapsapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * SQLite-tietokannan apuluokka paikkojen tallentamiseen.
 * Laajentaa SQLiteOpenHelper-luokkaa tietokannan luomiseen ja päivittämiseen.
 */
class PlacesDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "places.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "places"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_TIMESTAMP = "timestamp"
    }

    /**
     * onCreate()-metodi kutsutaan, kun tietokanta luodaan ensimmäistä kertaa.
     * Metodi luo tarvittavat taulut tietokantaan.
     */
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAME TEXT NOT NULL, " +
                "$COLUMN_LATITUDE REAL NOT NULL, " +
                "$COLUMN_LONGITUDE REAL NOT NULL, " +
                "$COLUMN_TIMESTAMP INTEGER NOT NULL)"
        db.execSQL(createTable)
    }

    /**
     * onUpgrade()-metodi kutsutaan, kun tietokanta päivitetään uuteen versioon.
     * Metodi poistaa vanhan taulun ja luo uuden.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}