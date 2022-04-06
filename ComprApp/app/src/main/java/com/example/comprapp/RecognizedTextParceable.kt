package com.example.comprapp

import android.graphics.Point
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RecognizedText(val name: String, val Points: Array<Point>?) : Parcelable
