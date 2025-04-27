package fi.lab.googlemapsapp

import android.content.ContentValues
import android.database.Cursor

/**
 * PlacesManager-luokka tarjoaa metodit paikkojen tallentamiseen, hakemiseen ja poistamiseen tietokannasta.
 */
class PlacesManager(private val dbHelper: PlacesDatabaseHelper) {

    /**
     * Lisää uuden paikan tietokantaan
     *
     * @param name Paikan nimi/info
     * @param latitude Paikan leveysaste
     * @param longitude Paikan pituusaste
     * @return Lisätyn rivin ID tai -1, jos lisäys epäonnistui
     */
    fun insertPlace(name: String, latitude: Double, longitude: Double): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(PlacesDatabaseHelper.COLUMN_NAME, name)
            put(PlacesDatabaseHelper.COLUMN_LATITUDE, latitude)
            put(PlacesDatabaseHelper.COLUMN_LONGITUDE, longitude)
            put(PlacesDatabaseHelper.COLUMN_TIMESTAMP, System.currentTimeMillis())
        }
        return db.insert(PlacesDatabaseHelper.TABLE_NAME, null, values)
    }

    /**
     * Hakee kaikki paikat tietokannasta
     *
     * @return Lista Place-olioita
     */
    fun getAllPlaces(): List<Place> {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            PlacesDatabaseHelper.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "${PlacesDatabaseHelper.COLUMN_TIMESTAMP} DESC" // Järjestä aikaleiman mukaan, uusin ensin
        )

        val placesList = mutableListOf<Place>()
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(PlacesDatabaseHelper.COLUMN_ID))
                val name = getString(getColumnIndexOrThrow(PlacesDatabaseHelper.COLUMN_NAME))
                val latitude = getDouble(getColumnIndexOrThrow(PlacesDatabaseHelper.COLUMN_LATITUDE))
                val longitude = getDouble(getColumnIndexOrThrow(PlacesDatabaseHelper.COLUMN_LONGITUDE))
                val timestamp = getLong(getColumnIndexOrThrow(PlacesDatabaseHelper.COLUMN_TIMESTAMP))

                placesList.add(Place(name, latitude, longitude, id, timestamp))
            }
        }
        cursor.close()
        return placesList
    }

    /**
     * Hakee viimeksi lisätyn paikan
     *
     * @return Viimeisin lisätty paikka tai null, jos paikkoja ei ole
     */
    fun getMostRecentPlace(): Place? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            PlacesDatabaseHelper.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "${PlacesDatabaseHelper.COLUMN_TIMESTAMP} DESC",
            "1" // Rajoita hakutulokset yhteen
        )

        var place: Place? = null
        with(cursor) {
            if (moveToFirst()) {
                val id = getLong(getColumnIndexOrThrow(PlacesDatabaseHelper.COLUMN_ID))
                val name = getString(getColumnIndexOrThrow(PlacesDatabaseHelper.COLUMN_NAME))
                val latitude = getDouble(getColumnIndexOrThrow(PlacesDatabaseHelper.COLUMN_LATITUDE))
                val longitude = getDouble(getColumnIndexOrThrow(PlacesDatabaseHelper.COLUMN_LONGITUDE))
                val timestamp = getLong(getColumnIndexOrThrow(PlacesDatabaseHelper.COLUMN_TIMESTAMP))

                place = Place(name, latitude, longitude, id, timestamp)
            }
        }
        cursor.close()
        return place
    }

    /**
     * Etsii paikkoja nimen perusteella
     *
     * @param query Hakusana
     * @return Lista hakusanaa vastaavia paikkoja
     */
    fun searchPlaces(query: String): List<Place> {
        val db = dbHelper.readableDatabase
        val searchQuery = "%$query%" // Lisää %-merkit hakutermin ympärille LIKE-hakua varten

        val cursor = db.query(
            PlacesDatabaseHelper.TABLE_NAME,
            null,
            "${PlacesDatabaseHelper.COLUMN_NAME} LIKE ?",
            arrayOf(searchQuery),
            null,
            null,
            null
        )

        val placesList = mutableListOf<Place>()
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(PlacesDatabaseHelper.COLUMN_ID))
                val name = getString(getColumnIndexOrThrow(PlacesDatabaseHelper.COLUMN_NAME))
                val latitude = getDouble(getColumnIndexOrThrow(PlacesDatabaseHelper.COLUMN_LATITUDE))
                val longitude = getDouble(getColumnIndexOrThrow(PlacesDatabaseHelper.COLUMN_LONGITUDE))
                val timestamp = getLong(getColumnIndexOrThrow(PlacesDatabaseHelper.COLUMN_TIMESTAMP))

                placesList.add(Place(name, latitude, longitude, id, timestamp))
            }
        }
        cursor.close()
        return placesList
    }

    /**
     * Poistaa paikan sen ID:n perusteella
     *
     * @param id Poistettavan paikan ID
     * @return Poistettujen rivien määrä (1 jos onnistui, 0 jos ei löytynyt)
     */
    fun deletePlace(id: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            PlacesDatabaseHelper.TABLE_NAME,
            "${PlacesDatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    /**
     * Poistaa paikan koordinaattien perusteella
     *
     * @param latitude Leveysaste
     * @param longitude Pituusaste
     * @return Poistettujen rivien määrä
     */
    fun deletePlaceByCoordinates(latitude: Double, longitude: Double): Int {
        val db = dbHelper.writableDatabase

        // Käytä pientä kynnysarvoa liukulukujen vertailussa
        val threshold = 0.0000001

        val whereClause =
            "ABS(${PlacesDatabaseHelper.COLUMN_LATITUDE} - ?) < $threshold " +
                    "AND ABS(${PlacesDatabaseHelper.COLUMN_LONGITUDE} - ?) < $threshold"

        return db.delete(
            PlacesDatabaseHelper.TABLE_NAME,
            whereClause,
            arrayOf(latitude.toString(), longitude.toString())
        )
    }

    /**
     * Poistaa paikan nimen perusteella
     *
     * @param name Poistettavan paikan nimi
     * @return Poistettujen rivien määrä
     */
    fun deletePlaceByName(name: String): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            PlacesDatabaseHelper.TABLE_NAME,
            "${PlacesDatabaseHelper.COLUMN_NAME} = ?",
            arrayOf(name)
        )
    }
}