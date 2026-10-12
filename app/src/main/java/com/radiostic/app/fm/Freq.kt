package com.radiostic.app.fm

import java.util.Locale

object Freq {

    fun parseKhz(input: String): Long? {
        val clean = input.trim()
            .lowercase(Locale.ROOT)
            .replace("mhz", "")
            .replace("khz", "")
            .trim()
            .replace(',', '.')

        if (clean.isEmpty()) return null

        val doubleVal = clean.toDoubleOrNull() ?: return null

        // If entered as direct kHz (e.g., 91900 or 104000)
        val khzVal = if (doubleVal >= 76000.0 && doubleVal <= 108000.0) {
            doubleVal.toLong()
        } else if (doubleVal >= 76.0 && doubleVal <= 108.0) {
            (doubleVal * 1000).toLong()
        } else {
            return null
        }

        // Round to nearest 50/100 kHz standard FM step
        return (khzVal / 50) * 50
    }

    fun formatMhz(kHz: Long): String {
        val mhz = kHz / 1000.0
        return String.format(Locale.US, "%.1f MHz", mhz)
    }
}
