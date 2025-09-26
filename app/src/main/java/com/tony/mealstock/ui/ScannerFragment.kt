package com.tony.mealstock.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.mlkit.vision.barcode.*
import com.google.mlkit.vision.common.InputImage
import com.tony.mealstock.R
import com.tony.mealstock.data.*
import com.tony.mealstock.net.OFF
import kotlinx.coroutines.*
import java.util.concurrent.Executors

class ScannerFragment: Fragment() {
  private lateinit var db: AppDb; private lateinit var pdao: ProductDao; private lateinit var ldao: ScanLogDao
  private lateinit var preview: PreviewView; private lateinit var txtCode: TextView; private lateinit var txtName: TextView; private lateinit var img: ImageView; private lateinit var txtQty: TextView
  private var lastCode: String? = null; private var lastName: String = "Produit inconnu"; private var lock=false
  private val req = registerForActivityResult(ActivityResultContracts.RequestPermission()) { g -> if (g) startCamera() }
  private val scanner by lazy { val opts = BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_EAN_13,Barcode.FORMAT_EAN_8,Barcode.FORMAT_UPC_A,Barcode.FORMAT_UPC_E,Barcode.FORMAT_CODE_128).build(); BarcodeScanning.getClient(opts) }

  override fun onCreateView(i: LayoutInflater, c: ViewGroup?, b: Bundle?): View {
    val v = i.inflate(R.layout.fragment_scanner, c, false)
    db = AppDb.get(requireContext()); pdao = db.productDao(); ldao = db.scanLogDao()
    preview = v.findViewById(R.id.previewView); txtCode=v.findViewById(R.id.txtCode); txtName=v.findViewById(R.id.txtName); img=v.findViewById(R.id.img); txtQty=v.findViewById(R.id.txtQty)
    v.findViewById<Button>(R.id.btnAdd).setOnClickListener { lastCode?.let { updateQty(it, +1.0) } }
    v.findViewById<Button>(R.id.btnRemove).setOnClickListener { lastCode?.let { updateQty(it, -1.0) } }
    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) startCamera() else req.launch(Manifest.permission.CAMERA)
    return v
  }

  private fun startCamera() {
    val f = ProcessCameraProvider.getInstance(requireContext())
    f.addListener({
      val p = f.get()
      val prev = Preview.Builder().build().apply { setSurfaceProvider(preview.surfaceProvider) }
      val an = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also { it.setAnalyzer(Executors.newSingleThreadExecutor(), ::analyze) }
      p.unbindAll(); p.bindToLifecycle(viewLifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, prev, an)
    }, ContextCompat.getMainExecutor(requireContext()))
  }

  private fun analyze(pr: ImageProxy) {
    if (lock) { pr.close(); return }
    val media = pr.image ?: run { pr.close(); return }
    val input = InputImage.fromMediaImage(media, pr.imageInfo.rotationDegrees)
    scanner.process(input).addOnSuccessListener { list ->
      val code = list.firstOrNull()?.rawValue
      if (!code.isNullOrBlank()) onBarcode(code)
    }.addOnCompleteListener { pr.close() }
  }

  private fun onBarcode(code: String) {
    if (lock) return
    lock = true
    txtCode.text = code
    lastCode = code
    viewLifecycleOwner.lifecycleScope.launch {
      val (name, url) = withContext(Dispatchers.IO) {
        try { val r = OFF.api.getProduct(code); (r.product?.product_name ?: "Produit inconnu") to (r.product?.image_url ?: "") }
        catch (_: Exception) { "Produit inconnu" to "" }
      }
      lastName = name
      txtName.text = name
      if (url.isNotEmpty()) Glide.with(requireContext()).load(url).into(img) else img.setImageDrawable(null)
      val qty = withContext(Dispatchers.IO) {
        val f = pdao.find(code)
        if (f == null) { pdao.upsert(Product(code, name)); 0.0 } else f.qty
      }
      txtQty.text = "Qté: ${qty.toInt()}"
      preview.postDelayed({ lock = false }, 1100)
    }
  }

  private fun updateQty(code: String, delta: Double) {
    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
      val p = pdao.find(code) ?: Product(code, lastName)
      val newQ = (p.qty + delta).coerceAtLeast(0.0)
      pdao.upsert(p.copy(name = lastName, qty = newQ))
      ldao.insert(ScanLog(barcode = code, deltaQty = delta))
      val q = pdao.find(code)?.qty ?: 0.0
      withContext(Dispatchers.Main) { txtQty.text = "Qté: ${q.toInt()}" }
    }
  }
}
