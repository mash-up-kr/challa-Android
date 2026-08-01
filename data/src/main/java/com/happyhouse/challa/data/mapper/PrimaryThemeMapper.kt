package com.happyhouse.challa.data.mapper

import androidx.datastore.preferences.core.Preferences
import com.happyhouse.challa.domain.model.PrimaryTheme
import com.happyhouse.challa.domain.result.ChallaResult

internal fun Preferences.toPrimaryThemeResult(primaryThemeKey: Preferences.Key<String>): ChallaResult<PrimaryTheme> =
    ChallaResult.Success(
        this[primaryThemeKey]
            ?.let { savedTheme ->
                PrimaryTheme.entries.firstOrNull { it.name == savedTheme }
            } ?: PrimaryTheme.LEMONADE,
    )
