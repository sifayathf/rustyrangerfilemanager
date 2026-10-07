package com.rustyranger.app.ui.screens.browser

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rustyranger.app.data.ffi.EventObserver
import com.rustyranger.app.data.ffi.FfiEngineEvent
import com.rustyranger.app.data.ffi.FfiNavItem
import com.rustyranger.app.data.ffi.FfiVfsNode
import com.rustyranger.app.data.ffi.PlatformEngineHandle
import com.rustyranger.app.util.FileOpener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class SortField { NAME, SIZE, TYPE, DATE }
data class SortConfig(val field: SortField = SortField.NAME, val ascending: Boolean = true)

sealed class VfsUiState {
    object Loading : VfsUiState()
    data class Success(
        val currentUri: String,
        val pathHistory: List<String>,
        val nodes: List<FfiVfsNode>,
        val selectedNodes: Set<String> = emptySet(),
        val searchQuery: String = "",
        val isSearchActive: Boolean = false,
        val navItems: List<FfiNavItem> = emptyList(),
        val activePreviewNode: FfiVfsNode? = null,
        val viewerImages: List<FfiVfsNode> = emptyList(),
        val viewerInitialIndex: Int = 0,
        val isViewerOpen: Boolean = false,
        val viewerScrollRequestedIndex: Int? = null,
        val sortConfig: SortConfig = SortConfig(),
        val clipboardNodes: Set<String> = emptySet(),
        val isClipboardCut: Boolean = false
    ) : VfsUiState()
    data class Error(val message: String) : VfsUiState()
}

class VfsBrowserViewModel(
    private val engineHandle: PlatformEngineHandle
) : ViewModel(), EventObserver {

    private val _uiState = MutableStateFlow<VfsUiState>(VfsUiState.Loading)
    val uiState: StateFlow<VfsUiState> = _uiState.asStateFlow()

    private var subscriptionId: String? = null

    init {
        subscriptionId = engineHandle.subscribeEvents(this)
        val initialPath = "file://${Environment.getExternalStorageDirectory().absolutePath}"
        loadNodes(initialPath, addToHistory = false)
    }

    fun loadNodes(uri: String, addToHistory: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val nodes = engineHandle.listNodes(uri)
                val navItems = engineHandle.getNavigationItems()
                
                val currentHistory = (_uiState.value as? VfsUiState.Success)?.pathHistory ?: emptyList()
                val currentSort = (_uiState.value as? VfsUiState.Success)?.sortConfig ?: SortConfig()
                val currentClipboard = (_uiState.value as? VfsUiState.Success)?.clipboardNodes ?: emptySet()
                val currentClipboardCut = (_uiState.value as? VfsUiState.Success)?.isClipboardCut ?: false
                
                val newHistory = if (addToHistory && uri != currentHistory.lastOrNull()) {
                    currentHistory + uri
                } else if (currentHistory.isEmpty()) {
                    listOf(uri)
                } else {
                    currentHistory
                }

                _uiState.value = VfsUiState.Success(
                    currentUri = uri,
                    pathHistory = newHistory,
                    nodes = sortNodes(nodes, currentSort),
                    navItems = navItems,
                    sortConfig = currentSort,
                    clipboardNodes = currentClipboard,
                    isClipboardCut = currentClipboardCut
                )
            } catch (e: Exception) {
                _uiState.value = VfsUiState.Error(e.message ?: "Failed to list directory")
            }
        }
    }
    
    fun setSortConfig(field: SortField) {
        val current = _uiState.value as? VfsUiState.Success ?: return
        val newAscending = if (current.sortConfig.field == field) !current.sortConfig.ascending else true
        val newConfig = SortConfig(field, newAscending)
        
        _uiState.value = current.copy(
            sortConfig = newConfig,
            nodes = sortNodes(current.nodes, newConfig)
        )
    }
    
    fun sortNodes(nodes: List<FfiVfsNode>, config: SortConfig): List<FfiVfsNode> {
        return nodes.sortedWith(Comparator { a, b ->
            if (a.isDirectory != b.isDirectory) {
                return@Comparator if (a.isDirectory) -1 else 1
            }
            val cmp = when (config.field) {
                SortField.NAME -> a.name.compareTo(b.name, ignoreCase = true)
                SortField.SIZE -> a.sizeBytes.compareTo(b.sizeBytes)
                SortField.DATE -> a.modifiedTimestamp.compareTo(b.modifiedTimestamp)
                SortField.TYPE -> {
                    val extA = a.name.substringAfterLast('.', "")
                    val extB = b.name.substringAfterLast('.', "")
                    extA.compareTo(extB, ignoreCase = true)
                }
            }
            if (config.ascending) cmp else -cmp
        })
    }

    fun navigateBack(): Boolean {
        val current = _uiState.value as? VfsUiState.Success ?: return false
        if (current.pathHistory.size > 1) {
            val updatedHistory = current.pathHistory.dropLast(1)
            val parentUri = updatedHistory.last()
            
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val nodes = engineHandle.listNodes(parentUri)
                    _uiState.update { state -> 
                        if (state is VfsUiState.Success) {
                            state.copy(
                                currentUri = parentUri,
                                pathHistory = updatedHistory,
                                nodes = sortNodes(nodes, state.sortConfig),
                                selectedNodes = emptySet(),
                                activePreviewNode = null
                            )
                        } else state
                    }
                } catch (e: Exception) {
                    // Ignore for now
                }
            }
            return true
        } else {
            // Check if we are already at the primary storage root to prevent going up into system folders
            val storageRoot = Environment.getExternalStorageDirectory().absolutePath
            val cleanPath = FileOpener.cleanPath(current.currentUri)
            
            // Allow going up only if we are not at the storage root
            if (cleanPath != storageRoot) {
                val currentFile = File(cleanPath)
                val parentFile = currentFile.parentFile
                if (parentFile != null && parentFile.exists()) {
                    val parentUri = "file://${parentFile.absolutePath}"
                    loadNodes(parentUri, addToHistory = false)
                    return true
                }
            }
        }
        return false
    }

    fun setPreviewNode(node: FfiVfsNode?) {
        _uiState.update { current ->
            if (current is VfsUiState.Success) current.copy(activePreviewNode = node) else current
        }
    }

    fun openImageViewer(targetUri: String, allNodes: List<FfiVfsNode>) {
        val current = _uiState.value as? VfsUiState.Success ?: return
        val imageNodes = allNodes.filter { 
            val ext = it.name.substringAfterLast('.', "").lowercase()
            ext in listOf("png", "jpg", "jpeg", "webp", "gif")
        }
        val initialIndex = imageNodes.indexOfFirst { it.uri == targetUri }.coerceAtLeast(0)
        _uiState.value = current.copy(
            viewerImages = imageNodes,
            viewerInitialIndex = initialIndex,
            isViewerOpen = true,
            viewerScrollRequestedIndex = null
        )
    }

    fun closeImageViewer(finalIndex: Int) {
        _uiState.update { current ->
            if (current is VfsUiState.Success) {
                current.copy(
                    isViewerOpen = false,
                    viewerScrollRequestedIndex = finalIndex
                )
            } else current
        }
    }
    
    fun clearViewerScrollRequest() {
        _uiState.update { current ->
            if (current is VfsUiState.Success && current.viewerScrollRequestedIndex != null) {
                current.copy(viewerScrollRequestedIndex = null)
            } else current
        }
    }

    fun toggleSelection(nodeUri: String) {
        _uiState.update { current ->
            if (current is VfsUiState.Success) {
                val updated = current.selectedNodes.toMutableSet().apply {
                    if (contains(nodeUri)) remove(nodeUri) else add(nodeUri)
                }
                current.copy(selectedNodes = updated)
            } else current
        }
    }

    fun selectAll() {
        _uiState.update { current ->
            if (current is VfsUiState.Success) {
                val allUris = current.nodes.map { it.uri }.toSet()
                current.copy(selectedNodes = allUris)
            } else current
        }
    }

    fun clearSelection() {
        _uiState.update { current ->
            if (current is VfsUiState.Success) current.copy(selectedNodes = emptySet()) else current
        }
    }

    fun createNewFolder(folderName: String) {
        val initialUri = (_uiState.value as? VfsUiState.Success)?.currentUri ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val parentPath = FileOpener.cleanPath(initialUri)
                val newDir = File(parentPath, folderName)
                if (!newDir.exists()) {
                    newDir.mkdirs()
                }
                loadNodes(initialUri, addToHistory = false)
            } catch (e: Exception) {
                // Ignore for now
            }
        }
    }

    fun deleteSelectedNodes() {
        val initialUri = (_uiState.value as? VfsUiState.Success)?.currentUri ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val latestState = _uiState.value as? VfsUiState.Success ?: return@launch
                latestState.selectedNodes.forEach { uri ->
                    val file = File(FileOpener.cleanPath(uri))
                    if (file.exists()) {
                        file.deleteRecursively()
                    }
                }
                loadNodes(initialUri, addToHistory = false)
            } catch (e: Exception) {
                // Ignore for now
            }
        }
    }

    fun renameNode(nodeUri: String, newName: String) {
        val initialUri = (_uiState.value as? VfsUiState.Success)?.currentUri ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(FileOpener.cleanPath(nodeUri))
                if (file.exists()) {
                    val newFile = File(file.parentFile, newName)
                    if (!newFile.exists()) {
                        file.renameTo(newFile)
                    } else {
                        // Target already exists! P0-4
                        // For now we just don't overwrite it. In the UI, we should show a snackbar.
                    }
                }
                loadNodes(initialUri, addToHistory = false)
            } catch (e: Exception) {
                // Ignore for now
            }
        }
    }

    fun setClipboard(uris: Set<String>, isCut: Boolean) {
        _uiState.update { current ->
            if (current is VfsUiState.Success) {
                current.copy(clipboardNodes = uris, isClipboardCut = isCut)
            } else current
        }
    }

    fun pasteClipboard() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val latestState = _uiState.value as? VfsUiState.Success ?: return@launch
                if (latestState.clipboardNodes.isEmpty()) return@launch
                val destDir = File(FileOpener.cleanPath(latestState.currentUri))
                if (!destDir.exists()) destDir.mkdirs()

                latestState.clipboardNodes.forEach { uri ->
                    val srcFile = File(FileOpener.cleanPath(uri))
                    if (srcFile.exists()) {
                        var destFile = File(destDir, srcFile.name)
                        
                        // Guard against copying/moving into itself or same folder
                        if (srcFile.canonicalPath == destFile.canonicalPath) {
                            if (latestState.isClipboardCut) return@forEach // cut and paste in same folder = no op
                            
                            val nameWithoutExt = srcFile.nameWithoutExtension
                            val ext = srcFile.extension.let { if (it.isNotEmpty()) ".$it" else "" }
                            var counter = 1
                            do {
                                destFile = File(destDir, "$nameWithoutExt ($counter)$ext")
                                counter++
                            } while (destFile.exists())
                        } else if (destFile.exists()) {
                            // Target exists but is different. For now, generate unique name to prevent data loss.
                            val nameWithoutExt = destFile.nameWithoutExtension
                            val ext = destFile.extension.let { if (it.isNotEmpty()) ".$it" else "" }
                            var counter = 1
                            do {
                                destFile = File(destDir, "$nameWithoutExt ($counter)$ext")
                                counter++
                            } while (destFile.exists())
                        }

                        // Guard against copying a folder into its own subfolder
                        if (destFile.canonicalPath.startsWith(srcFile.canonicalPath + File.separator)) {
                             return@forEach // Skip invalid recursion
                        }

                        if (latestState.isClipboardCut) {
                            val success = srcFile.renameTo(destFile) // Move
                            if (!success) {
                                // Fallback across mounts
                                if (srcFile.isDirectory) {
                                    srcFile.copyRecursively(destFile, overwrite = false)
                                } else {
                                    srcFile.copyTo(destFile, overwrite = false)
                                }
                                srcFile.deleteRecursively()
                            }
                        } else {
                            if (srcFile.isDirectory) {
                                srcFile.copyRecursively(destFile, overwrite = false)
                            } else {
                                srcFile.copyTo(destFile, overwrite = false)
                            }
                        }
                    }
                }
                
                _uiState.update { state -> 
                    if (state is VfsUiState.Success) {
                        state.copy(clipboardNodes = if (state.isClipboardCut) emptySet() else state.clipboardNodes)
                    } else state
                }
                loadNodes(latestState.currentUri, addToHistory = false)
            } catch (e: Exception) {
                // Ignore for now
            }
        }
    }

    fun performSearch(query: String) {
        val initialUri = (_uiState.value as? VfsUiState.Success)?.currentUri ?: return
        if (query.isBlank()) {
            loadNodes(initialUri, addToHistory = false)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val results = engineHandle.search(initialUri, query)
                _uiState.update { state -> 
                    if (state is VfsUiState.Success) {
                        state.copy(nodes = results, searchQuery = query, isSearchActive = true)
                    } else state
                }
            } catch (e: Exception) {
                // Ignore for now
            }
        }
    }

    override fun onEvent(event: FfiEngineEvent) {
        if (event.eventType == "VFS_CHANGED") {
            (uiState.value as? VfsUiState.Success)?.let { state ->
                loadNodes(state.currentUri, addToHistory = false)
            }
        }
    }

    override fun onCleared() {
        subscriptionId?.let { engineHandle.unsubscribeEvents(it) }
        super.onCleared()
    }
}
