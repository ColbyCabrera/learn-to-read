package com.example.readingfoundations.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.readingfoundations.data.Curriculum
import com.example.readingfoundations.data.Unit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnitDetailsViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val unitId: String = checkNotNull(savedStateHandle["unitId"])

    private val _uiState = MutableStateFlow<UnitDetailsUiState>(UnitDetailsUiState.Loading)
    val uiState: StateFlow<UnitDetailsUiState> = _uiState.asStateFlow()

    init {
        getUnitDetails()
    }

    private fun getUnitDetails() {
        val unit = Curriculum.units.find { it.id == unitId }
        if (unit != null) {
            val items = mutableListOf<UnitDetailsListItem>()
            unit.levels.forEach { (contentType, levels) ->
                items.add(UnitDetailsListItem.Header(contentType))
                levels.forEach { level ->
                    items.add(UnitDetailsListItem.LevelItem(level, contentType))
                }
            }
            _uiState.value = UnitDetailsUiState.Success(items)
        } else {
            _uiState.value = UnitDetailsUiState.Error
        }
    }
}

sealed interface UnitDetailsUiState {
    data class Success(val items: List<UnitDetailsListItem>) : UnitDetailsUiState
    object Error : UnitDetailsUiState
    object Loading : UnitDetailsUiState
}