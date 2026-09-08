package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ProgramEntity
import com.example.data.ProgramRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProgramViewModel(private val repository: ProgramRepository) : ViewModel() {
    val programs: StateFlow<List<ProgramEntity>> = repository.allPrograms.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addProgram(title: String, description: String) {
        viewModelScope.launch {
            repository.insert(ProgramEntity(title = title, description = description))
        }
    }
    
    fun deleteProgram(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }
}

class ProgramViewModelFactory(private val repository: ProgramRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProgramViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProgramViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
