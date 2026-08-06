package com.outime.app.presentation.theme

import androidx.compose.ui.graphics.Color

// ──────────────────────────────────────────────────────────────────────────
// OuTime Design System — Paleta ForestGreen
// ──────────────────────────────────────────────────────────────────────────

// Brand
val ForestPrimary = Color(0xFF2F5D50)
val ForestPrimaryLight = Color(0xFF3D7A69)
val ForestSecondary = Color(0xFF6E9F87)
val ForestAccent = Color(0xFFC9823A)

// Surfaces
val ForestBackground = Color(0xFFF9F7F2)
val ForestSurface = Color(0xFFFFFFFF)
val ForestBorder = Color(0xFFE2DDD5)

// Text
val ForestTextPrimary = Color(0xFF1A2922)
val ForestTextSecondary = Color(0xFF4A5E57)
val ForestTextTertiary = Color(0xFF8A9E98)

// Feedback
val ForestError = Color(0xFFC54B4B)

// ── Derivadas para roles de Material 3 (Light) ──────────────────────────────

// Primary
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = ForestPrimaryLight
val OnPrimaryContainer = Color(0xFFFFFFFF)

// Secondary
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFD4E5DC)
val OnSecondaryContainer = Color(0xFF1A2922)

// Tertiary (Accent)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFF5E0CB)
val OnTertiaryContainer = Color(0xFF3D2A14)

// Background
val OnBackground = ForestTextPrimary

// Surface
val OnSurface = ForestTextPrimary
val SurfaceVariant = ForestBorder
val OnSurfaceVariant = ForestTextSecondary

// Error
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFF4D4D4)
val OnErrorContainer = Color(0xFF4A1010)

// Outline
val Outline = ForestTextTertiary
val OutlineVariant = ForestBorder

// Inverse
val InverseSurface = Color(0xFF1A2922)
val InverseOnSurface = Color(0xFFF9F7F2)
val InversePrimary = ForestPrimaryLight