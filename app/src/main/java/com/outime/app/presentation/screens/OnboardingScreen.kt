package com.outime.app.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outime.app.R
import com.outime.app.data.local.OnboardingPersistence
import com.outime.app.presentation.theme.ForestPrimary
import com.outime.app.presentation.theme.ForestSecondary
import com.outime.app.presentation.theme.ForestTextSecondary

private data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String,
    val isLast: Boolean
)

@Composable
fun OnboardingScreen(
    onCompleted: () -> Unit
) {
    val context = LocalContext.current
    var currentPage by rememberSaveable { mutableIntStateOf(0) }

    val pages = listOf(
        OnboardingPage(
            imageRes = R.drawable.onb1,
            title = "Justo a tiempo, sin sorpresas",
            description = "Porque sabemos lo que es importante para tí, gestiona tus citas desde un solo lugar.",
            isLast = false
        ),
        OnboardingPage(
            imageRes = R.drawable.onb2,
            title = "Reserva cuando quieras",
            description = "Consulta la disponibilidad de un servicio y encuentra el horario que mejor se adapta a ti.",
            isLast = false
        ),
        OnboardingPage(
            imageRes = R.drawable.onb3,
            title = "Tus citas, siempre contigo",
            description = "Consulta tus reservas, gestiona tus citas, en cualquier lugar, en cualquier momento.",
            isLast = true
        )
    )

    val page = pages[currentPage]

    fun completeOnboarding() {
        OnboardingPersistence.setOnboardingCompleted(context)
        onCompleted()
    }

    BackHandler(enabled = currentPage > 0) {
        currentPage--
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // -------------------------------------------------------------
        // TOP / SALTAR
        // -------------------------------------------------------------
        Spacer(modifier = Modifier.height(20.dp)) //20
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 20.dp,
                    end = 8.dp
                ),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Saltar",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 15.sp
                ),
                color = ForestPrimary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable {
                        completeOnboarding()
                    }
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // -------------------------------------------------------------
        // TITLE
        // -------------------------------------------------------------

        Text(
            text = page.title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 32.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                color = ForestPrimary
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // -------------------------------------------------------------
        // DECORATIVE LINE
        // -------------------------------------------------------------

        Box(
            modifier = Modifier
                .width(38.dp)
                .height(2.5.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(ForestSecondary)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // -------------------------------------------------------------
        // DESCRIPTION
        // -------------------------------------------------------------

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Medium,
                color = ForestTextSecondary
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        )

        // -------------------------------------------------------------
        // ESPACIO FLEXIBLE
        //
        // Este spacer permite que la zona visual del onboarding
        // ocupe realmente la pantalla en lugar de quedar concentrada
        // en la mitad superior.
        // -------------------------------------------------------------

        Spacer(modifier = Modifier.weight(0.35f))

        // -------------------------------------------------------------
        // ILLUSTRATION
        // -------------------------------------------------------------

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = when (currentPage) {
                    0 -> "Ilustración persona usando móvil para gestionar citas"
                    1 -> "Ilustración calendario y reserva de citas"
                    2 -> "Ilustración citas y código QR"
                    else -> "Ilustración OuTime"
                },
                modifier = Modifier
                    .size(250.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // -------------------------------------------------------------
        // PAGE INDICATORS
        // -------------------------------------------------------------

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            pages.indices.forEach { index ->
                val isActive = currentPage == index

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }
                        )
                )
            }
        }

        // -------------------------------------------------------------
        // SPACE BEFORE BUTTON
        // -------------------------------------------------------------

        Spacer(modifier = Modifier.height(40.dp)) //40

        // -------------------------------------------------------------
        // CTA
        // -------------------------------------------------------------

        Button(
            onClick = {
                if (page.isLast) {
                    completeOnboarding()
                } else {
                    currentPage++
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 4.dp),

            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = if (page.isLast) {
                    "Comenzar"
                } else {
                    "Siguiente"
                },
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = if (page.isLast) "Comenzar" else "Siguiente",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        // -------------------------------------------------------------
        // BOTTOM SPACE
        // -------------------------------------------------------------

        Spacer(modifier = Modifier.weight(0.55f))
    }
}