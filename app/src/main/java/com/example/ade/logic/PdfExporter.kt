package com.example.ade.logic

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.ade.model.CalculationResult
import com.example.ade.ui.screens.formatAmount
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object PdfExporter {

    fun exportToPdf(context: Context, result: CalculationResult) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 500, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        var y = 40f
        paint.color = Color.BLACK
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("FACTURE ADE (Simulation)", 20f, y, paint)
        
        y += 30f
        paint.textSize = 10f
        paint.isFakeBoldText = false
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        canvas.drawText("Date: ${dateFormat.format(Date())}", 20f, y, paint)
        
        y += 30f
        paint.isFakeBoldText = true
        canvas.drawText("DÉTAILS", 20f, y, paint)
        paint.isFakeBoldText = false
        
        y += 20f
        canvas.drawText("Consommation: ${String.format(Locale.getDefault(), "%.2f", result.consumption)} m³", 20f, y, paint)
        y += 15f
        canvas.drawText("Montant Eau: ${formatAmount(result.waterAmount)}", 20f, y, paint)
        y += 15f
        canvas.drawText("Montant Assain.: ${formatAmount(result.sanitationAmount)}", 20f, y, paint)
        y += 15f
        canvas.drawText("Redevances Fixes: ${formatAmount(result.fixedFees)}", 20f, y, paint)
        y += 15f
        canvas.drawText("Frais Régulation: ${formatAmount(result.regulationFees)}", 20f, y, paint)
        y += 15f
        canvas.drawText("TVA: ${formatAmount(result.waterTva + result.sanitationTva)}", 20f, y, paint)
        
        y += 30f
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL TTC: ${formatAmount(result.totalTTC)}", 20f, y, paint)

        document.finishPage(page)

        val fileName = "Facture_${System.currentTimeMillis()}.pdf"
        val filePath = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        try {
            document.writeTo(FileOutputStream(filePath))
            Toast.makeText(context, "PDF enregistré: ${filePath.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Erreur lors de la génération du PDF", Toast.LENGTH_SHORT).show()
        }

        document.close()
    }
}
