package com.arthurtoso.conversormoeda
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class User(
    var brl: Double,
    var usd: Double,
    var btc: Double
): Parcelable


