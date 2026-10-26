package com.example.ui.office3d

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import com.example.R

class IsometricSpriteAtlas(
    val deskEmpty: ImageBitmap,
    val employeeFemale: ImageBitmap,
    val employeeMale: ImageBitmap,
    val serverRack: ImageBitmap,
    val plant: ImageBitmap,
    val waterCooler: ImageBitmap,
    val arcade: ImageBitmap,
    val whiteboard: ImageBitmap,
    val espresso: ImageBitmap,
    val meetingTable: ImageBitmap,
    val bookshelf: ImageBitmap
)

@Composable
fun rememberIsometricSpriteAtlas(): IsometricSpriteAtlas {
    val deskEmpty = ImageBitmap.imageResource(R.drawable.iso_desk_empty)
    val empFemale = ImageBitmap.imageResource(R.drawable.iso_employee_desk)
    val empMale = ImageBitmap.imageResource(R.drawable.iso_employee_male)
    val serverRack = ImageBitmap.imageResource(R.drawable.iso_server_rack)
    val plant = ImageBitmap.imageResource(R.drawable.iso_plant)
    val waterCooler = ImageBitmap.imageResource(R.drawable.iso_water_cooler)
    val arcade = ImageBitmap.imageResource(R.drawable.iso_arcade)
    val whiteboard = ImageBitmap.imageResource(R.drawable.iso_whiteboard)
    val espresso = ImageBitmap.imageResource(R.drawable.iso_espresso)
    val meetingTable = ImageBitmap.imageResource(R.drawable.iso_meeting_table)
    val bookshelf = ImageBitmap.imageResource(R.drawable.iso_bookshelf)

    return remember(
        deskEmpty, empFemale, empMale, serverRack, plant,
        waterCooler, arcade, whiteboard, espresso, meetingTable, bookshelf
    ) {
        IsometricSpriteAtlas(
            deskEmpty = deskEmpty,
            employeeFemale = empFemale,
            employeeMale = empMale,
            serverRack = serverRack,
            plant = plant,
            waterCooler = waterCooler,
            arcade = arcade,
            whiteboard = whiteboard,
            espresso = espresso,
            meetingTable = meetingTable,
            bookshelf = bookshelf
        )
    }
}
