package fi.lab.googlemapsapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Alafragmentti, joka sisältää Google Maps -kartan
 */
class LowerFragment : Fragment(), OnMapReadyCallback {

    // Google Maps -karttaobjekti
    private var map: GoogleMap? = null

    // Tietokantahallinta
    private lateinit var placesHelper: PlacesDatabaseHelper
    private lateinit var placesManager: PlacesManager

    // Kartan paikkojen ja ID:iden yhdistäminen
    private val markerPlaceMap = HashMap<Marker, Long>()

    // Rajapinta kommunikointiin pääaktiviteetin kanssa
    interface OnMapClickListener {
        fun onMapClicked(latitude: Double, longitude: Double)
    }

    private var mapClickListener: OnMapClickListener? = null

    /**
     * Asettaa kuuntelijan kartan klikkaustapahtumalle
     */
    fun setOnMapClickListener(listener: OnMapClickListener) {
        this.mapClickListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Lataa fragmentin layout
        val view = inflater.inflate(R.layout.fragment_lower, container, false)

        // Alusta tietokantahallinnan oliot
        placesHelper = PlacesDatabaseHelper(requireContext())
        placesManager = PlacesManager(placesHelper)

        // Hae karttafragmentti ja pyydä karttaobjektia
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        return view
    }

    /**
     * Kutsutaan kun kartta on valmis käytettäväksi
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Aseta kartan asetukset
        map?.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isCompassEnabled = true
            uiSettings.isMapToolbarEnabled = true
            uiSettings.isZoomGesturesEnabled = true
            uiSettings.isScrollGesturesEnabled = true
        }

        // Aseta oletussijainti (Lahti)
        val defaultLocation = LatLng(60.9827, 25.6612)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))

        // Aseta kartan klikkauksen kuuntelija
        map?.setOnMapClickListener { latLng ->
            mapClickListener?.onMapClicked(latLng.latitude, latLng.longitude)
        }

        // Aseta merkin klikkauksen kuuntelija
        map?.setOnMarkerClickListener { marker ->
            // Keskitä kartta merkkiin
            map?.animateCamera(CameraUpdateFactory.newLatLng(marker.position))
            // Näytä infoikkuna
            marker.showInfoWindow()
            true
        }

        // Aseta infoikkunan paikkojen poistamiselle
        map?.setOnInfoWindowClickListener { marker ->
            // Poistetaan paikka kun infoikkunaa klikataan
            val placeId = markerPlaceMap[marker]
            if (placeId != null) {
                showDeleteConfirmationDialog(marker, placeId)
            }
        }

        // Lataa tallennetut paikat tietokannasta
        loadPlacesFromDatabase()
    }

    /**
     * Näyttää vahvistusdialogin paikan poistamisesta
     */
    private fun showDeleteConfirmationDialog(marker: Marker, placeId: Long) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Poista paikka")
            .setMessage("Haluatko varmasti poistaa tämän paikan?")
            .setPositiveButton("Poista") { _, _ ->
                if (placesManager.deletePlace(placeId) > 0) {
                    marker.remove()
                    markerPlaceMap.remove(marker)
                    Toast.makeText(context, getString(R.string.place_removed), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.error_removing_place),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Peruuta", null)
            .show()
    }

    /**
     * Lataa kaikki tallennetut paikat tietokannasta ja lisää ne kartalle
     */
    private fun loadPlacesFromDatabase() {
        if (map == null) {
            return
        }

        // Tyhjennä nykyiset merkit
        map?.clear()
        markerPlaceMap.clear()

        // Hae kaikki paikat tietokannasta
        val places = placesManager.getAllPlaces()

        // Lisää merkki jokaiselle paikalle
        for (place in places) {
            addMarkerForPlace(place)
        }

        // Jos paikkoja on, kohdista viimeisimpään
        if (places.isNotEmpty()) {
            val mostRecent = places[0] // Lista on järjestetty aikaleiman mukaan, uusin ensin
            val position = LatLng(mostRecent.latitude, mostRecent.longitude)
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
        }
    }

    /**
     * Lisää merkki paikalle kartalla
     *
     * @param place Paikka, jolle merkki lisätään
     */
    fun addMarkerForPlace(place: Place) {
        if (map == null) {
            return
        }

        val position = LatLng(place.latitude, place.longitude)

        // Lisää merkki kartalle
        val marker = map?.addMarker(
            MarkerOptions()
                .position(position)
                .title(place.name)
        )

        // Tallenna paikan ID merkin yhteyteen
        if (marker != null) {
            markerPlaceMap[marker] = place.id
        }
    }

    /**
     * Kohdistaa kartan tiettyyn sijaintiin
     *
     * @param latitude Leveysaste
     * @param longitude Pituusaste
     */
    fun focusOnLocation(latitude: Double, longitude: Double) {
        if (map != null) {
            val position = LatLng(latitude, longitude)
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
        }
    }

    /**
     * Etsii paikkoja nimen perusteella
     *
     * @param query Hakusana
     */
    private var isSearching = false

    fun searchPlaces(query: String) {
        if (map == null || isSearching) {
            return
        }

        isSearching = true

        try {
            // Hae hakusanaa vastaavat paikat
            val places = placesManager.searchPlaces(query)

            if (places.isNotEmpty()) {
                // Kohdista ensimmäiseen löydettyyn paikkaan
                val first = places[0]
                val position = LatLng(first.latitude, first.longitude)
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))

                // Korosta hakutulokset esim. pompauttamalla infoikkunan
                for (place in places) {
                    // Etsi merkki, joka vastaa hakutulosta
                    val marker = findMarkerForPlace(place.id)
                    marker?.showInfoWindow()
                }

                // Näytä viesti hakutuloksista
                Toast.makeText(
                    context,
                    "Löytyi ${places.size} paikkaa hakusanalla '$query'",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(context, "Ei hakutuloksia hakusanalla '$query'", Toast.LENGTH_SHORT).show()
            }
        } finally {
            // Varmista että lippu nollataan aina
            isSearching = false
        }
    }

    /**
     * Etsii merkin paikan ID:n perusteella
     */
    private fun findMarkerForPlace(placeId: Long): Marker? {
        for ((marker, id) in markerPlaceMap) {
            if (id == placeId) {
                return marker
            }
        }
        return null
    }

    /**
     * Päivittää kartan näyttämään kaikki tallennetut paikat
     */
    fun refreshMap() {
        loadPlacesFromDatabase()
    }
}
