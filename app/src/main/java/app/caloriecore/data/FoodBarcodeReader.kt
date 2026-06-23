package app.caloriecore.data

import android.content.Context
import android.util.Log
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class FoodBarcodeReader(private val context: Context) {
    fun scan(
        onBarcodeDetected: (String) -> Unit,
        onUserCanceled: () -> Unit,
        onScannerFailure: (Throwable) -> Unit
    ) {
        val groceryCodeOptions = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_ITF
            )
            .enableAutoZoom()
            .build()

        GmsBarcodeScanning.getClient(context, groceryCodeOptions)
            .startScan()
            .addOnSuccessListener { barcode ->
                val codeDigits = barcode.rawValue.orEmpty().filter(Char::isDigit)
                if (codeDigits.isNotBlank()) {
                    onBarcodeDetected(codeDigits)
                } else {
                    onScannerFailure(IllegalStateException("empty_barcode"))
                }
            }
            .addOnCanceledListener {
                onUserCanceled()
            }
            .addOnFailureListener { error ->
                Log.e(Tag, "Barcode scan failed", error)
                onScannerFailure(error)
            }
    }

    private companion object {
        const val Tag = "FoodBarcodeReader"
    }
}
