package app.waste2wealth.com.communities

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val expandedState = mutableStateOf(0f)
    val expandedState2 = mutableStateOf(0f)
    val currentPage = mutableStateOf(0)
}