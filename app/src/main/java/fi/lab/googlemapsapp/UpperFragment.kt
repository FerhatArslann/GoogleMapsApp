package fi.lab.googlemapsapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * Yläfragmentti, joka sisältää syöttökentät paikan tiedoille ja tallennuspainikkeen
 */
class UpperFragment : Fragment() {

    // UI-komponentit
    private lateinit var editPlaceName: EditText
    private lateinit var editLatitude: EditText
    private lateinit var editLongitude: EditText
    private lateinit var saveButton: Button

    // Tietokantahallinta
    private lateinit var placesHelper: PlacesDatabaseHelper
    private lateinit var placesManager: PlacesManager

    // Rajapinta kommunikointiin pääaktiviteetin kanssa
    interface OnPlaceSavedListener {
        fun onPlaceSaved(place: Place)
    }

    private var placeSavedListener: OnPlaceSavedListener? = null

    /**
     * Asettaa kuuntelijan paikan tallennustapahtumalle
     */
    fun setOnPlaceSavedListener(listener: OnPlaceSavedListener) {
        this.placeSavedListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Lataa fragmentin layout
        val view = inflater.inflate(R.layout.fragment_upper, container, false)

        // Alusta UI-komponentit
        editPlaceName = view.findViewById(R.id.editPlaceName)
        editLatitude = view.findViewById(R.id.editLatitude)
        editLongitude = view.findViewById(R.id.editLongitude)
        saveButton = view.findViewById(R.id.buttonSave)

        // Hakuominaisuus
        val editSearch = view.findViewById<EditText>(R.id.editSearch)
        val buttonSearch = view.findViewById<Button>(R.id.buttonSearch)

        buttonSearch.setOnClickListener {
            val query = editSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                // Kutsu MainActivity:n kautta hakutoimintoa
                (activity as? MainActivity)?.searchPlaces(query)
            }
        }

        // Alusta tietokantahallinnan oliot
        placesHelper = PlacesDatabaseHelper(requireContext())
        placesManager = PlacesManager(placesHelper)

        // Aseta tallennuspainikkeen kuuntelija
        saveButton.setOnClickListener {
            savePlace()
        }

        return view
    }

    /**
     * Tallentaa paikan tietokantaan ja ilmoittaa kuuntelijalle
     */
    private fun savePlace() {
        val name = editPlaceName.text.toString().trim()

        // Tarkista että nimi ei ole tyhjä
        if (name.isEmpty()) {
            Toast.makeText(context, "Anna paikalle nimi", Toast.LENGTH_SHORT).show()
            return
        }

        // Hae leveys- ja pituusaste
        val latStr = editLatitude.text.toString()
        val lngStr = editLongitude.text.toString()

        // Älä tallenna jos koordinaatteja ei ole asetettu
        if (latStr.isEmpty() || lngStr.isEmpty()) {
            Toast.makeText(context, "Valitse ensin sijainti kartalta", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val latitude = latStr.toDouble()
            val longitude = lngStr.toDouble()

            // Luo uusi paikka-olio
            val place = Place(name, latitude, longitude)

            // Tallenna tietokantaan
            val id = placesManager.insertPlace(name, latitude, longitude)

            if (id != -1L) {
                // Tietokantaan lisäys onnistui
                place.id = id

                // Ilmoita kuuntelijalle
                placeSavedListener?.onPlaceSaved(place)

                // Näytä onnistumisviesti
                Toast.makeText(context, "Paikka tallennettu", Toast.LENGTH_SHORT).show()

                // Tyhjennä nimikentän sisältö
                editPlaceName.text.clear()
            } else {
                // Tietokantaan lisäys epäonnistui
                Toast.makeText(context, "Virhe tallennettaessa paikkaa", Toast.LENGTH_SHORT).show()
            }

        } catch (e: NumberFormatException) {
            // Tämän ei pitäisi tapahtua, koska koordinaattikentät täytetään ohjelmallisesti
            Toast.makeText(context, "Virheellinen koordinaattimuoto", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Päivittää leveys- ja pituusastekentät
     *
     * @param latitude Leveysaste
     * @param longitude Pituusaste
     */
    fun updateCoordinates(latitude: Double, longitude: Double) {
        if (::editLatitude.isInitialized && ::editLongitude.isInitialized) {
            editLatitude.setText(latitude.toString())
            editLongitude.setText(longitude.toString())
        }
    }
}