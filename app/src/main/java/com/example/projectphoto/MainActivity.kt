package com.example.projectphoto

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap


class MainActivity : AppCompatActivity() {

    private lateinit var captureIv: ImageView
    private lateinit var saveImageBtn: Button
    private lateinit var convertToGrayBtn: Button
    private lateinit var negativeFilterBtn: Button
    private lateinit var sepiaFilterBtn: Button
    private lateinit var seekBarBrightness: SeekBar
    private lateinit var seekBarContrast: SeekBar
    private lateinit var edgeDetectionBtn: Button
    private lateinit var removeFiltersBtn: Button
    private var isGrayScale = false
    private lateinit var imageUrl: Uri
    private var brightnessValue = 0
    private var contrastValue = 0
    private val camera = Camera(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        saveImageBtn = findViewById(R.id.saveImageBtn)
        saveImageBtn.visibility = View.GONE

        convertToGrayBtn = findViewById(R.id.convertToGrayBtn)
        convertToGrayBtn.visibility = View.GONE

        negativeFilterBtn = findViewById(R.id.negativeFilterBtn)
        negativeFilterBtn.visibility = View.GONE

        sepiaFilterBtn = findViewById(R.id.sepiaFilterBtn)
        sepiaFilterBtn.visibility = View.GONE

        seekBarBrightness = findViewById(R.id.seekBarBrightness)
        seekBarBrightness.visibility = View.GONE

        seekBarContrast = findViewById(R.id.seekBarContrast)
        seekBarContrast.visibility = View.GONE

        edgeDetectionBtn = findViewById(R.id.edgeDetectionBtn)
        edgeDetectionBtn.visibility = View.GONE

        removeFiltersBtn = findViewById(R.id.removeFiltersBtn)
        removeFiltersBtn.visibility = View.GONE

        captureIv = findViewById(R.id.captureImageView)

        val captureImgBtn = findViewById<Button>(R.id.captureImgBtn)
        captureImgBtn.setOnClickListener {
            isGrayScale = false
            imageUrl = camera.createImageUri()
            contract.launch(imageUrl)

            saveImageBtn.visibility = View.GONE
            convertToGrayBtn.visibility = View.GONE
            negativeFilterBtn.visibility = View.GONE
            sepiaFilterBtn.visibility = View.GONE
            seekBarBrightness.visibility = View.GONE
            seekBarContrast.visibility = View.GONE
            edgeDetectionBtn.visibility = View.GONE
            removeFiltersBtn.visibility = View.GONE
        }

        saveImageBtn.setOnClickListener {
            camera.saveImageToGallery(captureIv, imageUrl, isGrayScale)
        }

        convertToGrayBtn.setOnClickListener {
            val bitmap = captureIv.drawable.toBitmap()
            if (isGrayScale) {
                captureIv.setImageBitmap(bitmap)
                isGrayScale = false
            } else {
                val grayBitmap = camera.toGrayscale(bitmap)
                captureIv.setImageBitmap(grayBitmap)
                isGrayScale = true
            }
        }

        negativeFilterBtn.setOnClickListener {
            val bitmap = captureIv.drawable.toBitmap()
            val negativeBitmap = camera.applyNegativeFilter(bitmap)
            captureIv.setImageBitmap(negativeBitmap)
        }

        sepiaFilterBtn.setOnClickListener {
            val bitmap = captureIv.drawable.toBitmap()
            val sepiaBitmap = camera.applySepiaFilter(bitmap)
            captureIv.setImageBitmap(sepiaBitmap)
        }

        seekBarBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                brightnessValue = progress - 100
                applyBrightnessAndContrast(brightnessValue, contrastValue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarContrast.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                contrastValue = progress - 100
                applyBrightnessAndContrast(brightnessValue, contrastValue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        edgeDetectionBtn.setOnClickListener {
            val bitmap = captureIv.drawable.toBitmap()
            val edgeBitmap = camera.applyEdgeDetection(bitmap)
            captureIv.setImageBitmap(edgeBitmap)
        }

        removeFiltersBtn.setOnClickListener {
            captureIv.setImageURI(imageUrl)
            isGrayScale = false
            seekBarBrightness.progress = 100
            seekBarContrast.progress = 100
        }
    }

    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            captureIv.setImageURI(null)
            captureIv.setImageURI(imageUrl)
            saveImageBtn.visibility = View.VISIBLE
            convertToGrayBtn.visibility = View.VISIBLE
            negativeFilterBtn.visibility = View.VISIBLE
            sepiaFilterBtn.visibility = View.VISIBLE
            seekBarBrightness.visibility = View.VISIBLE
            seekBarContrast.visibility = View.VISIBLE
            edgeDetectionBtn.visibility = View.VISIBLE
            removeFiltersBtn.visibility = View.VISIBLE
        }
    }
    private fun applyBrightnessAndContrast(brightnessValue: Int, contrastValue: Int) {
        val bitmap = captureIv.drawable.toBitmap()
        val adjustedBitmap = camera.applyBrightnessAndContrast(bitmap, brightnessValue, contrastValue)
        captureIv.setImageBitmap(adjustedBitmap)
    }
}
