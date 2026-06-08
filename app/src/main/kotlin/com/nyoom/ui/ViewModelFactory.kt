package com.nyoom.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nyoom.data.repository.TripRepository
import com.nyoom.ui.diary.DiaryViewModel
import com.nyoom.ui.map.MapViewModel
import com.nyoom.ui.riding.RidingViewModel

class ViewModelFactory(
    private val repository: TripRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            RidingViewModel::class.java -> RidingViewModel() as T
            DiaryViewModel::class.java -> DiaryViewModel(repository) as T
            MapViewModel::class.java -> MapViewModel(repository, null) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
