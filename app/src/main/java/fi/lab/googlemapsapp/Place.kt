package fi.lab.googlemapsapp

import java.util.Locale

/**
 * Place-luokka, joka sisältää tiedot tallennetusta paikasta kartalla.
 * Implementoi Comparable, jotta paikat voidaan järjestää nimen mukaan.
 */
data class Place(
    var name: String,
    var latitude: Double,
    var longitude: Double,
    var id: Long = 0,
    var timestamp: Long = System.currentTimeMillis()
) : Comparable<Place> {

    /**
     * Palauttaa paikan tiedot merkkijonona
     */
    fun getInfo(): String = "$name (${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)})"

    /**
     * Vertailu nimen perusteella (aakkosjärjestys)
     */
    override fun compareTo(other: Place): Int {
        val s1: String = this.name.lowercase(Locale("fi", "FI"))
        val s2: String = other.name.lowercase(Locale("fi", "FI"))
        return s1.compareTo(s2)
    }
}