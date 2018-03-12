package com.example.higor.imageviewertest

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.Nullable
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.facebook.common.executors.CallerThreadExecutor
import com.facebook.common.references.CloseableReference
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.stfalcon.frescoimageviewer.ImageViewer
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity(), ImageOverlayView.IShareClickListener {

    private var mImageOverlayView : ImageOverlayView? = null
    private var mBitmap : Bitmap? = null
    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Fresco.initialize(this)

        val imageRequestBuilder = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse("https://www.noao.edu/image_gallery/images/d3/ic1396-800.jpg"))
                .setProgressiveRenderingEnabled(true)

        val imagePipeline = Fresco.getImagePipeline()
        val dataSource = imagePipeline.fetchDecodedImage(imageRequestBuilder.build(), this)

        dataSource.subscribe(object : BaseBitmapDataSubscriber() {
            override fun onFailureImpl(dataSource: com.facebook.datasource.DataSource<CloseableReference<CloseableImage>>?) {
                Log.d("asdfasf", "failed")
            }

            override fun onNewResultImpl(@Nullable bitmap: Bitmap?) {
                // You can use the bitmap in only limited ways
                mBitmap = bitmap
            }

        }, CallerThreadExecutor.getInstance())

        mImageOverlayView = ImageOverlayView(this)
        mImageOverlayView?.setListener(this)

        button.setOnClickListener {
            val images = arrayOf("https://www.noao.edu/image_gallery/images/d3/ic1396-800.jpg")
            ImageViewer.Builder(this, images)
                    .setStartPosition(0)
                    .hideStatusBar(true)
                    .allowZooming(true)
                    .allowSwipeToDismiss(true)
                    .setOverlayView(mImageOverlayView)
                    .setCustomImageRequestBuilder(imageRequestBuilder)
                    .show()
        }
    }

    private fun shareBitmap() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/jpeg"
        val bytes = ByteArrayOutputStream()
        mBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

        //val path = MediaStore.Images.Media.insertImage(contentResolver, mBitmap, "Title", null)
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
        val path = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        shareIntent.putExtra(Intent.EXTRA_STREAM, path)
        startActivity(Intent.createChooser(shareIntent, "Select"))
    }

    override fun onShareClick() {
        requestRead()
    }

    fun requestRead() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
        } else {
            shareBitmap()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                shareBitmap()
            } else {
                // Permission Denied
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
