package com.nyoom.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "About",
            fontSize = 24.sp
        )
        Text(
            text = "AI usage disclaimer: this app was entirely vibecoded and is held together by Claude and dreams",
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 16.dp),
            textAlign = Center
        )
        Text(
            text = "This app is open source. If you want to contribute or report an issue, visit our GitHub repo:",
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 16.dp),
            textAlign = Center
        )
        Text(
            text = "https://github.com/Steeven9/nyoom",
            fontSize = 16.sp,
            color = Color.Blue,
            textAlign = Center,
            modifier = Modifier
                .padding(top = 6.dp)
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Steeven9/nyoom"))
                    context.startActivity(intent)
                }
        )
    }
}

