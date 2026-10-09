package com.zdmgold.cleankoach.feature.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.cleankoach.core.data.prefs.ConsentDataStore
import com.zdmgold.cleankoach.core.media.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaAccessViewModel @Inject constructor(
    private val permissionChecker: PermissionChecker,
    private val consentDataStore: ConsentDataStore
) : ViewModel() {

    private val _hasAccess = MutableStateFlow(check())
    val hasAccess: StateFlow<Boolean> = _hasAccess.asStateFlow()

    fun refresh() {
        _hasAccess.value = check()
    }

    fun onDisclosureAccepted() {
        viewModelScope.launch { consentDataStore.setMediaDisclosureAccepted(true) }
    }

    // Photos and videos both granted, or Android 14 "selected photos" access.
    private fun check(): Boolean =
        permissionChecker.hasFullMediaAccess() || permissionChecker.hasPartialMediaAccess()
}
