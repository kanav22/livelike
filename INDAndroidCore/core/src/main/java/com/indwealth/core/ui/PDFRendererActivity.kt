package com.indwealth.core.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.indwealth.core.R
import com.indwealth.core.util.debouncedOnClick
import com.indwealth.core.util.log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import kotlinx.android.synthetic.main.activity_pdf_preview_activity.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * PDF Renderer Activity, Call directly or Extend it to support action button handling.
 * It also supports Multi page PDF rendering.
 */
open class PDFRendererActivity : CoreActivity() {

    private var mFileDescriptor: ParcelFileDescriptor? = null
    private var mPdfRenderer: PdfRenderer? = null
    private var mCurrentPage: PdfRenderer.Page? = null

    /**
     * Maintain status of PDF is loaded or failed to load.
     */
    private var isPdfLoaded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getContentView())

        setSupportActionBar(toolbar)
        supportActionBar!!.title = "PDF Preview"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        loadData()
    }

    override fun initViewModel() {
    }

    open fun getContentView(): Int {
        return R.layout.activity_pdf_preview_activity
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Entry point for loading intent information.
     * Override it to handle specialized mode in case of extending this class.
     */
    open fun loadData() {
        ("loadData called").log

        val url = intent.getStringExtra(EXTRA_URL)
        if (!TextUtils.isEmpty(url)) {
            loadPdfPreviewUrl(url)
            return
        }

        val filePath = intent.getStringExtra(EXTRA_FILE_PATH)
        if (!TextUtils.isEmpty(filePath)) {
            loadPdfPreviewFile(File(filePath))
            return
        }

        imageView!!.scaleType = ImageView.ScaleType.CENTER

        previousPageView.debouncedOnClick { showPage(mCurrentPage!!.index - 1) }
        nextPageView.debouncedOnClick { showPage(mCurrentPage!!.index + 1) }
    }

    private fun loadPdfPreviewUrl(url: String) {
        ("loadPdfPreviewUrl called").log
        showProgress(getString(R.string.msg_loading_preview), false)
        val file = File(cacheDir, FILE_PDF)
        val client = OkHttpClient.Builder().build()
        val request = Request.Builder().url(url).get().build()
        val call = client.newCall(request)
        ("loadPdfPreviewUrl loading file content started").log
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                ("loadPdfPreviewUrl loading file content failed").log
                uiHandler.post { showPreviewFailed() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                ("loadPdfPreviewUrl loading file content onResponse success? " + response.isSuccessful)
                handlePDFResponse(file, response)
            }
        })
    }

    private fun handlePDFResponse(file: File, response: Response) {
        val body = if (response.isSuccessful) response.body else null
        if (body != null) {
            try {
                val inputStream = body.byteStream()
                val fos = FileOutputStream(file)
                val buffer = ByteArray(64 * 1024)
                var count: Int = inputStream.read(buffer)
                while (count != -1) {
                    fos.write(buffer, 0, count)
                    count = inputStream.read(buffer)
                }
                fos.close()
                ("handlePDFResponse loading file content completed").log
                uiHandler.post { loadPdfPreviewFile(file) }
            } catch (io: IOException) {
                ("handlePDFResponse loading file content failed, IOE: ").log
                uiHandler.post { showPreviewFailed() }
            }
        } else {
            ("handlePDFResponse loading file content failed, body null").log
            uiHandler.post { showPreviewFailed() }
        }
    }

    private fun loadPdfPreviewFile(file: File) {
        ("loadPdfPreviewFile with file called").log
        showProgress(getString(R.string.msg_loading_preview), false)
        try {
            mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            // This is the PdfRenderer we use to render the PDF.
            if (mFileDescriptor != null) {
                mPdfRenderer = PdfRenderer(mFileDescriptor!!)
                mPdfRenderer!!.shouldScaleForPrinting()
                isPdfLoaded = true
                showPage(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showPreviewFailed()
        }

        hideProgress()
    }

    open fun showPreviewFailed() {
        ("showPreviewFailed called").log
        hideProgress()
        isPdfLoaded = false
    }

    /**
     * Closes the [PdfRenderer] and related resources.
     *
     * @throws IOException When the PDF file cannot be closed.
     */
    @Throws(IOException::class)
    private fun closeRenderer() {
        if (null != mCurrentPage) {
            mCurrentPage!!.close()
        }
        if (mPdfRenderer != null) {
            mPdfRenderer!!.close()
        }
        if (mFileDescriptor != null) {
            mFileDescriptor!!.close()
        }
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            try {
                closeRenderer()
            } catch (ignore: IOException) {
                ignore.printStackTrace()
            }
        }
    }

    /**
     * Shows the specified page of PDF to the screen.
     *
     * @param index The page index.
     */
    private fun showPage(index: Int) {
        ("showPage called with index $index").log
        if (mPdfRenderer!!.pageCount <= index) {
            return
        }

        // Make sure to close the current page before opening another one.
        if (null != mCurrentPage) {
            mCurrentPage!!.close()
        }
        mCurrentPage = mPdfRenderer!!.openPage(index)
        // Important: the destination bitmap must be ARGB (not RGB).
        ("showPage width: " + mCurrentPage!!.width + " height: " + mCurrentPage!!.height)

        val bitmap = Bitmap.createBitmap(resources.displayMetrics.densityDpi * mCurrentPage!!.width / 72,
                resources.displayMetrics.densityDpi * mCurrentPage!!.height / 72, Bitmap.Config.ARGB_8888)
        // Here, we render the page onto the Bitmap.
        // To render a portion of the page, use the second and third parameter. Pass nulls to get the default result.
        // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
        mCurrentPage!!.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
        // We are ready to show the Bitmap to user.
        imageView!!.setImageBitmap(bitmap)
        updateUIOnPageShown()
    }

    /**
     * Called when PDF page rendered on Image.
     */
    open fun updateUIOnPageShown() {
        if (getPageCount() > 1) {
            controlPanelView!!.visibility = View.VISIBLE
            previousPageView!!.visibility = if (mCurrentPage!!.index > 0) View.VISIBLE else View.INVISIBLE
            nextPageView!!.visibility = if (mCurrentPage!!.index < getPageCount() - 1) View.VISIBLE else View.INVISIBLE
            pageCounterTextView!!.text = String.format(Locale.getDefault(), "%d/%d", mCurrentPage!!.index + 1, getPageCount())
        } else {
            controlPanelView!!.visibility = View.GONE
        }
    }

    /**
     * Gets the number of pages in the PDF.
     *
     * @return The number of pages.
     */
    private fun getPageCount(): Int {
        return mPdfRenderer!!.pageCount
    }

    companion object {
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_URL = "pdf.url"
        private const val EXTRA_FILE_PATH = "pdf.file.path"
        private const val FILE_PDF = "temp_preview.pdf"

        /**
         * Launch PDF Renderer for URL (Public) PDF file
         *
         * @param context calling context
         * @param title activity title
         * @param url URL string
         */
        @JvmStatic
        fun launchForUrl(context: Context, title: String, url: String) {
            val intent = Intent(context, PDFRendererActivity::class.java)
            intent.putExtra(EXTRA_TITLE, title)
            intent.putExtra(EXTRA_URL, url)
            context.startActivity(intent)
        }

        /**
         * Launch PDF Renderer for Local PDF file (must be accessible)
         *
         * @param context calling context
         * @param title activity title
         * @param file file accessible
         */
        @JvmStatic
        fun launchForFile(context: Context, title: String, file: File) {
            val intent = Intent(context, PDFRendererActivity::class.java)
            intent.putExtra(EXTRA_TITLE, title)
            intent.putExtra(EXTRA_FILE_PATH, file.absolutePath)
            context.startActivity(intent)
        }
    }
}