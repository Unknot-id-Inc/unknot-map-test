package com.example.mapboxtest

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val DEVICE_ID_PREF = stringPreferencesKey("device_id")

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")