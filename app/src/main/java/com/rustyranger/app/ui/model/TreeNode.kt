package com.rustyranger.app.ui.model

import com.rustyranger.app.data.ffi.FfiVfsNode

data class TreeNode(
    val node: FfiVfsNode,
    val depth: Int,
    val isExpanded: Boolean,
    val isLastChild: Boolean,
    val ancestorIsLast: List<Boolean>
)
