package com.rustyranger.app.ui.screens.browser

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rustyranger.app.data.ffi.FfiVfsNode
import com.rustyranger.app.ui.components.*
import com.rustyranger.app.ui.model.TreeNode
import com.rustyranger.app.ui.screens.SettingsScreen
import com.rustyranger.app.util.FileOpener
import com.rustyranger.app.util.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VfsBrowserScreen(
    viewModel: VfsBrowserViewModel,
    settingsStore: SettingsStore? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val uiSettings by (settingsStore?.uiSettingsStateFlow?.collectAsState() ?: mutableStateOf(null))

    val viewMode = uiSettings?.viewMode ?: SettingsStore.VIEW_MODE_GRID
    val showPreviewPane = uiSettings?.showPreviewPane ?: false
    val gridColumns = uiSettings?.gridColumnCount ?: 4
    val listDensity = uiSettings?.listDensityLevel ?: 4
    val uiFontLevel = uiSettings?.uiFontLevel ?: 3
    val uiPaddingLevel = uiSettings?.uiPaddingLevel ?: 3
    val itemSpacingLevel = uiSettings?.itemSpacingLevel ?: 4
    val gridHSpacingLevel = uiSettings?.gridHorizontalSpacingLevel ?: 4
    val gridVSpacingLevel = uiSettings?.gridVerticalSpacingLevel ?: 4
    val settingsWidthLevel = uiSettings?.settingsWidthLevel ?: 6
    val customBackgroundUri = uiSettings?.customBackgroundUri ?: ""
    val showImageThumbnails = uiSettings?.showImageThumbnails ?: true
    val showFileMetadata = uiSettings?.showFileMetadata ?: true
    val iconScaleLevel = uiSettings?.iconScaleLevel ?: 3
    val iconBgMode = uiSettings?.iconBgMode ?: SettingsStore.ICON_BG_MODE_ICON_TILE
    val iconBgColor = uiSettings?.iconBgColor ?: SettingsStore.ICON_BG_COLOR_SURFACE
    val iconAspectRatioLevel = uiSettings?.iconAspectRatioLevel ?: 3
    val imageThumbnailScale = uiSettings?.imageThumbnailScale ?: SettingsStore.IMAGE_THUMBNAIL_CROP

    // Tree Expansion & Children Cache State
    var expandedPaths by remember { mutableStateOf(setOf<String>()) }
    var childrenMap by remember { mutableStateOf(mapOf<String, List<FfiVfsNode>>()) }
    var isSelectionMode by remember { mutableStateOf(false) }

    // Dialog States
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var renameTargetNode by remember { mutableStateOf<FfiVfsNode?>(null) }
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQueryText by remember { mutableStateOf("") }

    // Determine if we can go back internally
    val currentHistorySize = (state as? VfsUiState.Success)?.pathHistory?.size ?: 0
    val currentUri = (state as? VfsUiState.Success)?.currentUri ?: ""
    val isStorageRoot = currentUri == "file://${android.os.Environment.getExternalStorageDirectory().absolutePath}"
    val canGoBackInternal = currentHistorySize > 1 || !isStorageRoot

    // Intercept System Back Button
    BackHandler(enabled = canGoBackInternal || drawerState.isOpen) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            val handled = viewModel.navigateBack()
            if (handled) {
                expandedPaths = emptySet()
                childrenMap = emptyMap()
            }
        }
    }

    val nodes = (state as? VfsUiState.Success)?.nodes ?: emptyList()
    
    // Instant Local Filtering
    val filteredNodes = remember(nodes, searchQueryText) {
        if (searchQueryText.isBlank()) nodes
        else nodes.filter { it.name.contains(searchQueryText, ignoreCase = true) }
    }

    // Flatten Tree Structure Algorithm
    val visibleTreeNodes = remember(filteredNodes, expandedPaths, childrenMap) {
        flattenNodes(
            nodes = filteredNodes,
            expandedPaths = expandedPaths,
            childrenMap = childrenMap,
            depth = 0,
            ancestorIsLast = emptyList()
        )
    }

    val gridNodes = filteredNodes

    val totalCount = if (viewMode == SettingsStore.VIEW_MODE_TREE) visibleTreeNodes.size else gridNodes.size
    val selectedNodesCount = (state as? VfsUiState.Success)?.selectedNodes?.size ?: 0
    val totalSize = remember(visibleTreeNodes, gridNodes, viewMode) { 
        if (viewMode == SettingsStore.VIEW_MODE_TREE) visibleTreeNodes.sumOf { it.node.sizeBytes }
        else gridNodes.sumOf { it.sizeBytes }
    }
    val selectedSize = remember(state, visibleTreeNodes, gridNodes, viewMode) {
        val selectedUris = (state as? VfsUiState.Success)?.selectedNodes ?: emptySet()
        if (viewMode == SettingsStore.VIEW_MODE_TREE) visibleTreeNodes.filter { selectedUris.contains(it.node.uri) }.sumOf { it.node.sizeBytes }
        else gridNodes.filter { selectedUris.contains(it.uri) }.sumOf { it.sizeBytes }
    }

    val lazyListState = rememberLazyListState()
    val lazyGridState = rememberLazyGridState()

    (state as? VfsUiState.Success)?.let { s ->
        LaunchedEffect(s.viewerScrollRequestedIndex) {
            val finalIndex = s.viewerScrollRequestedIndex
            if (finalIndex != null && finalIndex in s.viewerImages.indices) {
                val targetUri = s.viewerImages[finalIndex].uri
                if (viewMode == SettingsStore.VIEW_MODE_TREE) {
                    val indexInTree = visibleTreeNodes.indexOfFirst { it.node.uri == targetUri }
                    if (indexInTree >= 0) lazyListState.animateScrollToItem(indexInTree)
                } else {
                    val indexInGrid = gridNodes.indexOfFirst { it.uri == targetUri }
                    if (indexInGrid >= 0) lazyGridState.animateScrollToItem(indexInGrid)
                }
                viewModel.clearViewerScrollRequest()
            }
        }
    }

    val activePreviewNode = remember(showPreviewPane, (state as? VfsUiState.Success)?.selectedNodes, gridNodes, visibleTreeNodes) {
        if (!showPreviewPane) null
        else {
            val selectedUris = (state as? VfsUiState.Success)?.selectedNodes ?: emptySet()
            if (selectedUris.size == 1) {
                val singleSelectedUri = selectedUris.first()
                if (viewMode == SettingsStore.VIEW_MODE_TREE) {
                    visibleTreeNodes.find { it.node.uri == singleSelectedUri }?.node
                } else {
                    gridNodes.find { it.uri == singleSelectedUri }
                }
            } else null
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width((120 + settingsWidthLevel * 40).dp)
            ) {
                if (settingsStore != null && (drawerState.isOpen || drawerState.targetValue == DrawerValue.Open)) {
                    SettingsScreen(settingsStore = settingsStore, onNavigateBack = { scope.launch { drawerState.close() } })
                }
            }
        }
    ) {
        val searchPadMult = if (viewMode == SettingsStore.VIEW_MODE_GRID) {
            1f - ((gridColumns - 1) * 0.1f)
        } else {
            1f - ((listDensity - 1) * 0.1f)
        }
        val listPadMult = 1f - ((listDensity - 1) * 0.1f)
        val fontPadMult = 0.8f + ((uiFontLevel - 1) * 0.1f)
        val itemSpacingMult = itemSpacingLevel * 0.25f
        
        val gridHSpacing = when (gridHSpacingLevel) {
            1 -> 2.dp
            2 -> 4.dp
            3 -> 6.dp
            4 -> 8.dp
            5 -> 12.dp
            6 -> 18.dp
            else -> 8.dp
        }
        val gridVSpacing = when (gridVSpacingLevel) {
            1 -> 2.dp
            2 -> 4.dp
            3 -> 6.dp
            4 -> 8.dp
            5 -> 12.dp
            6 -> 18.dp
            else -> 8.dp
        }
        
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image Customization
            if (customBackgroundUri.isNotBlank()) {
                AsyncImage(
                    model = customBackgroundUri,
                    contentDescription = "Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Darken/lighten background so UI is readable
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)))
            } else {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            }

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .height(48.dp)
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onBackground)
                            }
                            Text(
                                text = "Rusty Ranger",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                            )
                            IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground)
                            }
                        }

                        (state as? VfsUiState.Success)?.let { s ->
                            BreadcrumbBar(
                                currentUri = s.currentUri,
                                onSegmentClick = { uri ->
                                    expandedPaths = emptySet()
                                    childrenMap = emptyMap()
                                    viewModel.loadNodes(uri)
                                }
                            )
                        }

                        if (isSearchVisible) {
                            val barHeight = (48 * searchPadMult).coerceAtLeast(24f).dp
                            val barPadding = (16 * searchPadMult).dp
                            val fontSize = (16 * searchPadMult).coerceAtLeast(10f).sp
                            val iconSize = (24 * searchPadMult).coerceAtLeast(14f).dp
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = barPadding, vertical = (8 * searchPadMult).dp)
                                    .height(barHeight)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f), RoundedCornerShape(50)),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = (12 * searchPadMult).dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BasicTextField(
                                        value = searchQueryText,
                                        onValueChange = { searchQueryText = it },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = fontSize
                                        ),
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.weight(1f),
                                        decorationBox = { innerTextField ->
                                            Box(contentAlignment = Alignment.CenterStart) {
                                                if (searchQueryText.isEmpty()) {
                                                    Text(
                                                        text = "Filter files...",
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = fontSize
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        }
                                    )
                                    if (searchQueryText.isNotEmpty()) {
                                        IconButton(
                                            onClick = { searchQueryText = "" },
                                            modifier = Modifier.size(iconSize)
                                        ) {
                                            Icon(
                                                Icons.Default.Close, 
                                                contentDescription = "Clear",
                                                modifier = Modifier.size(iconSize)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (viewMode == SettingsStore.VIEW_MODE_TREE && showFileMetadata) {
                            (state as? VfsUiState.Success)?.let { s ->
                                ColumnHeader(
                                    sortConfig = s.sortConfig,
                                    onSort = { viewModel.setSortConfig(it) }
                                )
                            }
                        }
                    }
                },
                bottomBar = {
                    Column {
                        if (showPreviewPane && activePreviewNode != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp, max = 200.dp)
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                            ) {
                                PreviewPane(
                                    node = activePreviewNode,
                                    onDismiss = { }
                                )
                            }
                        }
                        
                        DynamicBottomBar(
                            selectedCount = selectedNodesCount,
                            totalCount = totalCount,
                            selectedSize = selectedSize,
                            totalSize = totalSize,
                            isSelectionMode = isSelectionMode || selectedNodesCount > 0,
                            onHome = {
                                expandedPaths = emptySet()
                                childrenMap = emptyMap()
                                viewModel.loadNodes("file:///storage/emulated/0")
                            },
                            onUp = {
                                val handled = viewModel.navigateBack()
                                if (handled) {
                                    expandedPaths = emptySet()
                                    childrenMap = emptyMap()
                                }
                            },
                            onToggleMultiSelect = {
                                isSelectionMode = !isSelectionMode
                                if (!isSelectionMode) viewModel.clearSelection()
                            },
                            onSelectAll = { viewModel.selectAll() },
                            onCopy = {
                                val selected = (state as? VfsUiState.Success)?.selectedNodes ?: emptySet()
                                if (selected.isNotEmpty()) {
                                    viewModel.setClipboard(selected, isCut = false)
                                    viewModel.clearSelection()
                                    isSelectionMode = false
                                }
                            },
                            onCut = {
                                val selected = (state as? VfsUiState.Success)?.selectedNodes ?: emptySet()
                                if (selected.isNotEmpty()) {
                                    viewModel.setClipboard(selected, isCut = true)
                                    viewModel.clearSelection()
                                    isSelectionMode = false
                                }
                            },
                            onPaste = {
                                viewModel.pasteClipboard()
                            },
                            hasClipboard = (state as? VfsUiState.Success)?.clipboardNodes?.isNotEmpty() == true,
                            onMove = { },
                            onDelete = { viewModel.deleteSelectedNodes() },
                            onRename = {
                                val selected = (state as? VfsUiState.Success)?.selectedNodes ?: emptySet()
                                val targetUri = selected.firstOrNull()
                                if (targetUri != null) {
                                    val node = if (viewMode == SettingsStore.VIEW_MODE_TREE) {
                                        visibleTreeNodes.find { it.node.uri == targetUri }?.node
                                    } else {
                                        gridNodes.find { it.uri == targetUri }
                                    }
                                    if (node != null) renameTargetNode = node
                                }
                            },
                            onMore = { },
                            onCancelSelection = {
                                viewModel.clearSelection()
                                isSelectionMode = false
                            }
                        )
                    }
                },
                modifier = modifier
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (val s = state) {
                        is VfsUiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                        is VfsUiState.Error -> {
                            Text(
                                text = s.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        is VfsUiState.Success -> {
                            val isEmpty = if (viewMode == SettingsStore.VIEW_MODE_TREE) visibleTreeNodes.isEmpty() else gridNodes.isEmpty()
                            if (isEmpty) {
                                Text(
                                    text = "Empty Directory",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                if (viewMode == SettingsStore.VIEW_MODE_TREE) {
                                    LazyColumn(
                                        state = lazyListState,
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(horizontal = (8 * itemSpacingMult).dp, vertical = (4 * itemSpacingMult).dp)
                                    ) {
                                        items(
                                            items = visibleTreeNodes,
                                            key = { it.node.uri }
                                        ) { treeNode ->
                                            val node = treeNode.node
                                            val isSelected = s.selectedNodes.contains(node.uri)

                                            TreeRow(
                                                treeNode = treeNode,
                                                isSelected = isSelected,
                                                isSelectionMode = isSelectionMode,
                                                showFileMetadata = showFileMetadata,
                                                padMult = listPadMult,
                                                fontPadMult = fontPadMult,
                                                onExpandClick = {
                                                    if (node.isDirectory) {
                                                        val uri = node.uri
                                                        if (expandedPaths.contains(uri)) {
                                                            expandedPaths = expandedPaths - uri
                                                        } else {
                                                            expandedPaths = expandedPaths + uri
                                                            if (!childrenMap.containsKey(uri)) {
                                                                scope.launch {
                                                                    val sortCfg = (state as? VfsUiState.Success)?.sortConfig ?: SortConfig()
                                                                    val children = loadFolderChildren(uri, viewModel, sortCfg)
                                                                    childrenMap = childrenMap + (uri to children)
                                                                }
                                                            }
                                                        }
                                                    }
                                                },
                                                onNodeTap = {
                                                    if (isSelectionMode) {
                                                        viewModel.toggleSelection(node.uri)
                                                    } else {
                                                        if (node.isDirectory) {
                                                            expandedPaths = emptySet()
                                                            childrenMap = emptyMap()
                                                            viewModel.loadNodes(node.uri)
                                                        } else {
                                                            val mime = detectMime(File(FileOpener.cleanPath(node.uri)))
                                                            if (mime.startsWith("image/")) {
                                                                viewModel.openImageViewer(node.uri, visibleTreeNodes.map { it.node })
                                                            } else {
                                                                FileOpener.openFile(context, node.uri)
                                                            }
                                                        }
                                                    }
                                                },
                                                onRowLongClick = {
                                                    isSelectionMode = true
                                                    viewModel.toggleSelection(node.uri)
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    var currentColumns by remember(gridColumns) { mutableFloatStateOf(gridColumns.toFloat()) }
                                    
                                    LazyVerticalGrid(
                                        state = lazyGridState,
                                        columns = GridCells.Fixed(currentColumns.roundToInt().coerceIn(1, 6)),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .pointerInput(Unit) {
                                                detectTransformGestures { _, _, zoom, _ ->
                                                    val sensitivity = 1.2f
                                                    if (zoom > 1.0f) {
                                                        currentColumns = (currentColumns - (zoom - 1.0f) * sensitivity).coerceIn(1f, 6f)
                                                    } else if (zoom < 1.0f) {
                                                        currentColumns = (currentColumns + (1.0f - zoom) * sensitivity).coerceIn(1f, 6f)
                                                    }
                                                    
                                                    val newInt = currentColumns.roundToInt().coerceIn(1, 6)
                                                    if (newInt != gridColumns) {
                                                        scope.launch { settingsStore?.setGridColumnCount(newInt) }
                                                    }
                                                }
                                            },
                                        contentPadding = PaddingValues(horizontal = gridHSpacing, vertical = gridVSpacing),
                                        horizontalArrangement = Arrangement.spacedBy(gridHSpacing),
                                        verticalArrangement = Arrangement.spacedBy(gridVSpacing)
                                    ) {
                                        items(
                                            items = gridNodes,
                                            key = { it.uri }
                                        ) { node ->
                                            val isSelected = s.selectedNodes.contains(node.uri)
                                            GridItem(
                                                node = node,
                                                isSelected = isSelected,
                                                isSelectionMode = isSelectionMode,
                                                showImageThumbnails = showImageThumbnails,
                                                gridColumns = gridColumns,
                                                fontPadMult = fontPadMult,
                                                iconScaleLevel = iconScaleLevel,
                                                iconBgMode = iconBgMode,
                                                iconBgColorStr = iconBgColor,
                                                iconAspectRatioLevel = iconAspectRatioLevel,
                                                imageThumbnailScale = imageThumbnailScale,
                                                onNodeTap = {
                                                    if (isSelectionMode) {
                                                        viewModel.toggleSelection(node.uri)
                                                    } else {
                                                        if (node.isDirectory) {
                                                            expandedPaths = emptySet()
                                                            childrenMap = emptyMap()
                                                            viewModel.loadNodes(node.uri)
                                                        } else {
                                                            val mime = detectMime(File(FileOpener.cleanPath(node.uri)))
                                                            if (mime.startsWith("image/")) {
                                                                viewModel.openImageViewer(node.uri, gridNodes)
                                                            } else {
                                                                FileOpener.openFile(context, node.uri)
                                                            }
                                                        }
                                                    }
                                                },
                                                onRowLongClick = {
                                                    isSelectionMode = true
                                                    viewModel.toggleSelection(node.uri)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Image Viewer Overlay
            if ((state as? VfsUiState.Success)?.isViewerOpen == true) {
                val s = state as VfsUiState.Success
                ImageViewerScreen(
                    images = s.viewerImages,
                    initialIndex = s.viewerInitialIndex,
                    onClose = { finalIndex -> viewModel.closeImageViewer(finalIndex) }
                )
            }
        }
    }

    if (showNewFolderDialog) {
        NewFolderDialog(
            onConfirm = { name ->
                showNewFolderDialog = false
                viewModel.createNewFolder(name)
            },
            onDismiss = { showNewFolderDialog = false }
        )
    }

    renameTargetNode?.let { node ->
        RenameDialog(
            currentName = node.name,
            onConfirm = { newName ->
                renameTargetNode = null
                viewModel.renameNode(node.uri, newName)
            },
            onDismiss = { renameTargetNode = null }
        )
    }
}

private fun flattenNodes(
    nodes: List<FfiVfsNode>,
    expandedPaths: Set<String>,
    childrenMap: Map<String, List<FfiVfsNode>>,
    depth: Int,
    ancestorIsLast: List<Boolean>
): List<TreeNode> {
    val result = mutableListOf<TreeNode>()
    val count = nodes.size

    nodes.forEachIndexed { index, node ->
        val isLast = (index == count - 1)
        val isExpanded = expandedPaths.contains(node.uri)

        val treeNode = TreeNode(
            node = node,
            depth = depth,
            isExpanded = isExpanded,
            isLastChild = isLast,
            ancestorIsLast = ancestorIsLast
        )
        result.add(treeNode)

        if (node.isDirectory && isExpanded) {
            val children = childrenMap[node.uri] ?: emptyList()
            if (children.isNotEmpty()) {
                val nextAncestors = ancestorIsLast + isLast
                result.addAll(
                    flattenNodes(
                        nodes = children,
                        expandedPaths = expandedPaths,
                        childrenMap = childrenMap,
                        depth = depth + 1,
                        ancestorIsLast = nextAncestors
                    )
                )
            }
        }
    }

    return result
}

private suspend fun loadFolderChildren(
    uri: String, 
    viewModel: VfsBrowserViewModel, 
    sortConfig: SortConfig
): List<FfiVfsNode> = withContext(Dispatchers.IO) {
    val cleanPath = FileOpener.cleanPath(uri)
    val dir = File(cleanPath)
    if (!dir.exists() || !dir.isDirectory) return@withContext emptyList()

    val files = dir.listFiles() ?: return@withContext emptyList()
    val rawNodes = files.map { file ->
        FfiVfsNode(
            uri = "file://${file.absolutePath}",
            name = file.name,
            isDirectory = file.isDirectory,
            sizeBytes = if (file.isDirectory) 0L else file.length(),
            modifiedTimestamp = file.lastModified() / 1000,
            mimeType = if (file.isDirectory) "inode/directory" else detectMime(file)
        )
    }
    viewModel.sortNodes(rawNodes, sortConfig)
}

private fun detectMime(file: File): String {
    val ext = file.extension.lowercase()
    return when (ext) {
        "rs", "kt", "java", "py", "js", "ts", "json", "xml" -> "text/code"
        "png", "jpg", "jpeg", "webp", "gif" -> "image/*"
        "mp4", "mkv", "webm" -> "video/*"
        "mp3", "flac", "ogg" -> "audio/*"
        "pdf" -> "application/pdf"
        "apk" -> "application/vnd.android.package-archive"
        "zip", "tar", "gz", "7z" -> "application/zip"
        else -> "application/octet-stream"
    }
}
