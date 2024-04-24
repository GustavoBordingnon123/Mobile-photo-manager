package com.example.projectphoto

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
    private lateinit var removeFiltersBtn: Button
    private lateinit var seekBarBrightness: SeekBar
    private lateinit var seekBarContrast: SeekBar
    private lateinit var edgeDetectionBtn: Button
    private var isGrayScale = false
    private var imageCounter = 0
    private lateinit var imageUrl: Uri
    private var brightnessValue = 0
    private var contrastValue = 0

    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            captureIv.setImageURI(null)
            captureIv.setImageURI(imageUrl)
            saveImageBtn.visibility = View.VISIBLE
            convertToGrayBtn.visibility = View.VISIBLE
            negativeFilterBtn.visibility = View.VISIBLE
            sepiaFilterBtn.visibility = View.VISIBLE
            removeFiltersBtn.visibility = View.VISIBLE
            seekBarBrightness.visibility = View.VISIBLE
            seekBarContrast.visibility = View.VISIBLE
            edgeDetectionBtn.visibility = View.VISIBLE
        }
    }

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

        removeFiltersBtn = findViewById(R.id.removeFiltersBtn)
        removeFiltersBtn.visibility = View.GONE

        seekBarBrightness = findViewById(R.id.seekBarBrightness)
        seekBarBrightness.visibility = View.GONE

        seekBarContrast = findViewById(R.id.seekBarContrast)
        seekBarContrast.visibility = View.GONE

        edgeDetectionBtn = findViewById(R.id.edgeDetectionBtn)
        edgeDetectionBtn.visibility = View.GONE

        captureIv = findViewById(R.id.captureImageView)

        val captureImgBtn = findViewById<Button>(R.id.captureImgBtn)
        captureImgBtn.setOnClickListener {
            isGrayScale = false
            imageUrl = createImageUri()
            contract.launch(imageUrl)

            saveImageBtn.visibility = View.GONE
            convertToGrayBtn.visibility = View.GONE
            negativeFilterBtn.visibility = View.GONE
            sepiaFilterBtn.visibility = View.GONE
            removeFiltersBtn.visibility = View.GONE
            seekBarBrightness.visibility = View.GONE
            seekBarContrast.visibility = View.GONE
            edgeDetectionBtn.visibility = View.GONE
        }

        saveImageBtn.setOnClickListener {
            saveImageToGallery(imageUrl)
            val message = if (isGrayScale) "Imagem salva em tons de cinza com sucesso!" else "Imagem salva em cores com sucesso!"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        convertToGrayBtn.setOnClickListener {
            val bitmap = captureIv.drawable.toBitmap()
            if (isGrayScale) {
                captureIv.setImageBitmap(bitmap)
                isGrayScale = false
            } else {
                val grayBitmap = toGrayscale(bitmap)
                captureIv.setImageBitmap(grayBitmap)
                isGrayScale = true
            }
        }

        negativeFilterBtn.setOnClickListener {
            val bitmap = captureIv.drawable.toBitmap()
            val negativeBitmap = applyNegativeFilter(bitmap)
            captureIv.setImageBitmap(negativeBitmap)
        }

        sepiaFilterBtn.setOnClickListener {
            val bitmap = captureIv.drawable.toBitmap()
            val sepiaBitmap = applySepiaFilter(bitmap)
            captureIv.setImageBitmap(sepiaBitmap)
        }

        removeFiltersBtn.setOnClickListener {
            captureIv.setImageURI(imageUrl)
            isGrayScale = false
            seekBarBrightness.progress = 100
            seekBarContrast.progress = 100
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
            val edgeBitmap = applyEdgeDetection(bitmap)
            captureIv.setImageBitmap(edgeBitmap)
        }
    }

    private fun createImageUri(): Uri {
        val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val imageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "processed_image_$imageCounter.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        }
        imageCounter++
        return contentResolver.insert(imageCollection, imageDetails)!!
    }

    private fun saveImageToGallery(uri: Uri) {
        val bitmap = captureIv.drawable.toBitmap()
        val outputStream = contentResolver.openOutputStream(uri)!!
        if (isGrayScale) {
            val grayBitmap = toGrayscale(bitmap)
            grayBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        } else {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        outputStream.close()

        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = uri
        sendBroadcast(mediaScanIntent)
    }

    private fun toGrayscale(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val grayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(grayBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val filter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter

        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return grayBitmap
    }

    private fun applyNegativeFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val negativeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(negativeBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix(floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        ))
        val filter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter

        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return negativeBitmap
    }

    private fun applySepiaFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val sepiaBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(sepiaBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix(floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        val filter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter

        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return sepiaBitmap
    }

    private fun applyBrightnessAndContrast(brightnessValue: Int, contrastValue: Int) {
        val bitmap = captureIv.drawable.toBitmap()
        val adjustedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)

        val canvas = Canvas(adjustedBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val contrast = (1.0 + contrastValue / 100.0).toFloat()
        val brightness = brightnessValue

        val contrastMatrix = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, 0f,
            0f, contrast, 0f, 0f, 0f,
            0f, 0f, contrast, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))

        val brightnessMatrix = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, brightness.toFloat(),
            0f, 1f, 0f, 0f, brightness.toFloat(),
            0f, 0f, 1f, 0f, brightness.toFloat(),
            0f, 0f, 0f, 1f, 0f
        ))

        contrastMatrix.preConcat(brightnessMatrix)

        val filter = ColorMatrixColorFilter(contrastMatrix)
        paint.colorFilter = filter

        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        captureIv.setImageBitmap(adjustedBitmap)
    }

    private fun applyEdgeDetection(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val edgeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val sobelX = arrayOf(
            intArrayOf(-1, 0, 1),
            intArrayOf(-2, 0, 2),
            intArrayOf(-1, 0, 1)
        )

        val sobelY = arrayOf(
            intArrayOf(-1, -2, -1),
            intArrayOf(0, 0, 0),
            intArrayOf(1, 2, 1)
        )

        for (x in 1 until width - 1) {
            for (y in 1 until height - 1) {
                var pixelX = 0
                var pixelY = 0

                for (i in -1..1) {
                    for (j in -1..1) {
                        val neighborPixel = bitmap.getPixel(x + i, y + j)
                        val neighborGray = (Color.red(neighborPixel) + Color.green(neighborPixel) + Color.blue(neighborPixel)) / 3

                        pixelX += neighborGray * sobelX[i + 1][j + 1]
                        pixelY += neighborGray * sobelY[i + 1][j + 1]
                    }
                }

                var magnitude = Math.sqrt((pixelX * pixelX + pixelY * pixelY).toDouble()).toInt()
                magnitude = if (magnitude > 255) 255 else if (magnitude < 0) 0 else magnitude

                edgeBitmap.setPixel(x, y, Color.rgb(magnitude, magnitude, magnitude))
            }
        }

        return edgeBitmap
    }
}
