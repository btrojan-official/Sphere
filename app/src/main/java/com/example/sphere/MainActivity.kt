package com.example.sphere

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.math.cbrt
import kotlin.math.round
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var etRadius: EditText
    private lateinit var radiusUnitGroup : RadioGroup
    private lateinit var spMaterials: Spinner
    private lateinit var etVolume: EditText
    private lateinit var etWeight: EditText
    private lateinit var volumeUnitGroup: RadioGroup
    private lateinit var weightUnitGroup: RadioGroup
    private lateinit var btnCalculate: Button
    private lateinit var checkboxEditVolumeWeight : CheckBox
    private lateinit var imgSphere : ImageView
    private lateinit var main : LinearLayout

    // Material density map
    val materialsDensityMap: HashMap<String, Double> = hashMapOf(
        "water" to 997.0,
        "aluminium" to 2700.0,
        "silver" to 10490.0,
        "platinum" to 21450.0,
        "lead" to 11340.0
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main = findViewById<LinearLayout>(R.id.main)
        imgSphere = findViewById<ImageView>(R.id.sphereImg)

        etRadius = findViewById<EditText>(R.id.radiusInput)
        etVolume = findViewById<EditText>(R.id.volumeOutput)
        etWeight = findViewById<EditText>(R.id.weightOutput)
        spMaterials = findViewById<Spinner>(R.id.spMaterials)
        btnCalculate = findViewById<Button>(R.id.Submit)
        checkboxEditVolumeWeight = findViewById<CheckBox>(R.id.Enable)

        radiusUnitGroup = findViewById<RadioGroup>(R.id.rg1)
        volumeUnitGroup = findViewById<RadioGroup>(R.id.rg2)
        weightUnitGroup = findViewById<RadioGroup>(R.id.rg3)

        radiusUnitGroup.check(findViewById<RadioButton>(R.id.rbCm).getId())
        volumeUnitGroup.check(findViewById<RadioButton>(R.id.rbCm3).getId())
        weightUnitGroup.check(findViewById<RadioButton>(R.id.G).getId())

        // Populate the materials spinner
        val materials = materialsDensityMap.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, materials)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spMaterials.adapter = adapter

        // Enable or disable volume and weight fields based on checkbox
        etVolume.isEnabled = false
        etWeight.isEnabled = false
        checkboxEditVolumeWeight.setOnCheckedChangeListener { _, isChecked ->
            etVolume.isEnabled = isChecked
            etWeight.isEnabled = isChecked
        }

        // Calculate button logic
        btnCalculate.setOnClickListener {
            calculate()
        }

        if(isDarkMode()){
            val color = ContextCompat.getColor(this, R.color.imgSphereColor)
            imgSphere.setColorFilter(color)
        }
        imgSphere.setOnLongClickListener {
            changeSphereBackground()
            true
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val color = ContextCompat.getColor(this, R.color.imgSphereColor)
        val theme = ContextCompat.getColor(this, R.color.themee)
        imgSphere.setColorFilter(color)
        imgSphere.setBackgroundColor(theme)
        main.setBackgroundColor(theme)
    }


    private fun isDarkMode(): Boolean {
        val nightModeFlags = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }


    private fun changeSphereBackground(){
        imgSphere.setColorFilter(generateRandomHexColor())
    }

    private fun generateRandomHexColor(): Int {
        // Generate random RGB values
        val red = Random.nextInt(256)  // Range 0-255
        val green = Random.nextInt(256)
        val blue = Random.nextInt(256)

        // Convert the RGB values to a hexadecimal string and format it
        return Color.rgb(red, green, blue)
    }

    private fun calculate(){
        val radiusStr = etRadius.text.toString()
        val selectedMaterial = spMaterials.selectedItem.toString()

        val selectedWeightUnitId = weightUnitGroup.checkedRadioButtonId
        val selectedRadiusUnitId = radiusUnitGroup.checkedRadioButtonId
        val selectedVolumeUnitId = volumeUnitGroup.checkedRadioButtonId

        val volumeStr = etVolume.text.toString()
        val weightStr = etWeight.text.toString()


        if (radiusStr.isEmpty() && volumeStr.isEmpty() && weightStr.isEmpty() || (selectedWeightUnitId == -1 || selectedVolumeUnitId == -1 || selectedRadiusUnitId == -1)) {
            Toast.makeText(this, "Please enter a radius/volume/weight", Toast.LENGTH_SHORT).show()
            return
        }

        if (radiusStr.isNotEmpty()){
            val radius = radiusStr.toDouble()

            val radiusInMeters = if (selectedRadiusUnitId == R.id.rbCm) { radius / 100 } else { radius; }

            val volumeInCubicMeters = (4.0 / 3.0) * Math.PI * Math.pow(radiusInMeters, 3.0)
            val density = materialsDensityMap[selectedMaterial] ?: 0.0

            val volumeText = if (selectedVolumeUnitId == R.id.rbCm3) { (volumeInCubicMeters * 1_000_000) } else { volumeInCubicMeters }
            etVolume.setText((round(volumeText*100)/100).toString())

            val weightInKg = volumeInCubicMeters * density
            val weightText = when (selectedWeightUnitId) {
                R.id.G -> (weightInKg * 1000)
                R.id.Kg -> weightInKg
                R.id.T -> (weightInKg / 1000)
                else -> weightInKg
            }
            etWeight.setText((round(weightText*100)/100).toString())
        }
        else if (volumeStr.isNotEmpty()){
            val volume = volumeStr.toDouble()

            val volumeInMeters = if (selectedVolumeUnitId == R.id.rbCm3) { volume / 1000000 } else { volume; }

            val radiusInMeters = cbrt(3*volumeInMeters/(4*Math.PI))
            val density = materialsDensityMap[selectedMaterial] ?: 0.0

            val radiusText = if (selectedRadiusUnitId == R.id.rbCm) { (radiusInMeters * 100) }
            else { radiusInMeters }

            etRadius.setText((round(radiusText*100)/100).toString())

            val weightInKg = volumeInMeters * density
            val weightText = when (selectedWeightUnitId) {
                R.id.G -> (weightInKg * 1000)
                R.id.Kg -> weightInKg
                R.id.T -> (weightInKg / 1000)
                else -> weightInKg
            }
            etWeight.setText((round(weightText*100)/100).toString())
        }
        else if (weightStr.isNotEmpty()){
            val weight = weightStr.toDouble()

            var weightInKg = weight;
            if (selectedWeightUnitId == R.id.T) { weightInKg = weight * 1000 } else if (selectedWeightUnitId == R.id.G){ weightInKg = weight / 1000 }

            val density = materialsDensityMap[selectedMaterial] ?: 0.0
            val volumeInCubicMeters = weightInKg / density;

            val radiusInMeters = cbrt(3*volumeInCubicMeters/(4*Math.PI))
            val radiusText = if(selectedRadiusUnitId == R.id.rbCm){ radiusInMeters * 100 } else {radiusInMeters}

            val volumeText = if (selectedVolumeUnitId == R.id.rbCm3) { (volumeInCubicMeters * 1_000_000) } else { volumeInCubicMeters }
            etVolume.setText((round(volumeText*100)/100).toString())
            etRadius.setText((round(radiusText*100)/100).toString())
        }
    }
}
