package id.iot.trialpp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.progressindicator.CircularProgressIndicator

class TempPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temp_page)

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Find views by ID
        val tempIndicator: CircularProgressIndicator = findViewById(R.id.tempIndicator)
        val tempText: TextView = findViewById(R.id.tempText)
        val humidityIndicator: CircularProgressIndicator = findViewById(R.id.humidityIndicator)
        val humidityText: TextView = findViewById(R.id.humidityText)
        val lineChart: LineChart = findViewById(R.id.lineChart)

        // Dummy data, replace with actual sensor data retrieval
        val temperature = getDummyTemperature()
        val humidity = getDummyHumidity()

        // Set the indicator values
        updateTemperature(tempIndicator, tempText, temperature)
        updateHumidity(humidityIndicator, humidityText, humidity)

        // Setup and populate the LineChart
        setupLineChart(lineChart)
        populateLineChart(lineChart)
    }

    private fun updateTemperature(tempIndicator: CircularProgressIndicator, tempText: TextView, temp: Float) {
        tempText.text = "$temp°C"
        tempIndicator.setProgressCompat((temp / 40 * 100).toInt(), true)
    }

    private fun updateHumidity(humidityIndicator: CircularProgressIndicator, humidityText: TextView, humidity: Float) {
        humidityText.text = "$humidity%"
        humidityIndicator.setProgressCompat((humidity / 100 * 100).toInt(), true)
    }

    private fun getDummyTemperature(): Float {
        return 19.0f
    }

    private fun getDummyHumidity(): Float {
        return 62.0f
    }

    private fun setupLineChart(lineChart: LineChart) {
        lineChart.setDrawGridBackground(false)
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)

        val xAxis: XAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        val leftAxis: YAxis = lineChart.axisLeft
        leftAxis.setDrawGridLines(false)

        val rightAxis: YAxis = lineChart.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.isEnabled = false
    }

    private fun populateLineChart(lineChart: LineChart) {
        val temperatureValues = ArrayList<Entry>()
        val humidityValues = ArrayList<Entry>()

        // Dummy data, replace with actual data
        for (i in 0..9) {
            temperatureValues.add(Entry(i.toFloat(), (Math.random() * 40).toFloat()))
            humidityValues.add(Entry(i.toFloat(), (Math.random() * 100).toFloat()))
        }

        val tempDataSet = LineDataSet(temperatureValues, "Temperature")
        tempDataSet.color = Color.BLUE
        tempDataSet.lineWidth = 2f
        tempDataSet.setDrawCircles(false)
        tempDataSet.setDrawValues(false)

        val humidityDataSet = LineDataSet(humidityValues, "Humidity")
        humidityDataSet.color = Color.GREEN
        humidityDataSet.lineWidth = 2f
        humidityDataSet.setDrawCircles(false)
        humidityDataSet.setDrawValues(false)

        val data = LineData(tempDataSet, humidityDataSet)
        lineChart.data = data
        lineChart.invalidate()
    }
}
