package fi.lab.googlemapsapp

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import fi.lab.googlemapsapp.R

/**
 * MainActivity on sovelluksen pääaktiviteetti.
 * Se hallinnoi kahta fragmenttia:
 * - UpperFragment: Paikan tietojen syöttämistä varten
 * - LowerFragment: Google Maps -kartan näyttämistä varten
 *
 * Aktiviteetti huolehtii fragmenttien välisestä kommunikaatiosta
 * ja hakutoiminnallisuudesta.
 */
class MainActivity : AppCompatActivity(),
    UpperFragment.OnPlaceSavedListener,
    LowerFragment.OnMapClickListener {

    // Fragmentit
    private lateinit var upperFragment: UpperFragment
    private lateinit var lowerFragment: LowerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Alusta fragmentit, jos tämä on ensimmäinen luontikerta (ei konfiguraatiomuutos)
        if (savedInstanceState == null) {
            setupFragments()
        }
    }

    /**
     * Alustaa fragmentit ja lisää ne niiden säiliöihin
     */
    private fun setupFragments() {
        // Luo fragmentti-instanssit
        upperFragment = UpperFragment()
        lowerFragment = LowerFragment()

        // Aseta kuuntelijat
        upperFragment.setOnPlaceSavedListener(this)
        lowerFragment.setOnMapClickListener(this)

        // Lisää fragmentit niiden säiliöihin
        val transaction = supportFragmentManager.beginTransaction()

        transaction.add(R.id.upper_fragment_container, upperFragment)
        transaction.add(R.id.lower_fragment_container, lowerFragment)

        transaction.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Lataa hakuvalikko
        menuInflater.inflate(R.menu.search_menu, menu)

        // Alusta hakutoiminnallisuus
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? SearchView

        searchView?.apply {
            // Aseta hakukentän vihjeteksti
            queryHint = getString(R.string.search_hint)

            // Aseta hakukuuntelija
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    // Etsi paikkoja, kun käyttäjä lähettää hakukyselyn
                    if (::lowerFragment.isInitialized) {
                        lowerFragment.searchPlaces(query)
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    // Haetaan vain, kun käyttäjä lähettää kyselyn
                    return false
                }
            })

            // Aseta sulkemiskuuntelija, joka palauttaa kaikki paikat, kun haku suljetaan
            setOnCloseListener {
                if (::lowerFragment.isInitialized) {
                    lowerFragment.refreshMap()
                }
                false
            }
        }

        return true
    }

    /**
     * Kutsutaan, kun paikka on tallennettu UpperFragment:issa
     *
     * @param place Tallennettu paikka
     */
    override fun onPlaceSaved(place: Place) {
        // Lisää merkki uudelle paikalle
        if (::lowerFragment.isInitialized) {
            lowerFragment.addMarkerForPlace(place)
            // Kohdista uuteen paikkaan
            lowerFragment.focusOnLocation(place.latitude, place.longitude)
        }
    }

    /**
     * Kutsutaan, kun karttaa klikataan LowerFragment:issa
     *
     * @param latitude Klikatun pisteen leveysaste
     * @param longitude Klikatun pisteen pituusaste
     */
    override fun onMapClicked(latitude: Double, longitude: Double) {
        // Päivitä koordinaatit UpperFragment:iin
        if (::upperFragment.isInitialized) {
            upperFragment.updateCoordinates(latitude, longitude)
        }
    }
}