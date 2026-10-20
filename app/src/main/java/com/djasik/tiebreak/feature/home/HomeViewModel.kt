package com.djasik.tiebreak.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djasik.tiebreak.domain.model.TeamId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(Home.UiState())
    val uiState: StateFlow<Home.UiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<Home.UiEvent>()
    val uiEvent: SharedFlow<Home.UiEvent> = _uiEvent.asSharedFlow()

    fun onUserEvent(event: Home.UserEvent) {
        when (event) {
            is Home.UserEvent.SelectTeam -> selectTeam(event.teamId)
            Home.UserEvent.StartMatch -> startMatch()
        }
    }

    private fun selectTeam(teamId: TeamId) {
        _uiState.update {
            it.copy(
                selectedTeamId = teamId
            )
        }
    }

    private fun startMatch() {
        val selectedTeamId = _uiState.value.selectedTeamId ?: return

        viewModelScope.launch {
            _uiEvent.emit(
                Home.UiEvent.NavigateToMatch(
                    teamId = selectedTeamId
                )
            )
        }
    }
}