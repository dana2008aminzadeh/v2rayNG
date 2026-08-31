package com.v2ray.ang.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.R
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.ui.compose.LocalDarkTheme
import com.v2ray.ang.ui.compose.QRCodeDialog
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Locale

// کارت جدید و پریمیوم برای انتخاب سریع‌ترین سرور
@Composable
fun AutoConnectFastestCard(mainViewModel: MainViewModel, onAction: (MainAction) -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isFa = Locale.getDefault().language == "fa" || Locale.getDefault().language == "ar"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                scope.launch {
                    // مرحله اول: استارت پینگ
                    android.widget.Toast.makeText(context, if(isFa) "در حال تست پینگ..." else "Testing Ping...", android.widget.Toast.LENGTH_SHORT).show()
                    onAction(MainAction.TestRealAllServers)
                    
                    // مرحله دوم: 4 ثانیه صبر برای دریافت پینگ از همه سرورها
                    kotlinx.coroutines.delay(4000)
                    
                    // مرحله سوم: مرتب‌سازی سرورها
                    onAction(MainAction.SortByTestResults)
                    android.widget.Toast.makeText(context, if(isFa) "سرورها بر اساس سرعت مرتب شدند!" else "Servers sorted by speed!", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF474747)) // کادر ملایم برای زیبایی بیشتر
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_routing_24dp),
                contentDescription = null,
                tint = Color(0xFFA3A3A3),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(if(isFa) "سریع‌ترین سرور" else "Fastest Server", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(if(isFa) "تست پینگ و انتخاب هوشمند" else "Auto-select lowest latency", color = Color(0xFF808080), fontSize = 13.sp)
            }
        }
    }
}

// باکس جذاب نمایش آی‌پی و پینگ هنگام اتصال
@Composable
fun ConnectionStatusCard(isRunning: Boolean, displayText: String) {
    val isFa = Locale.getDefault().language == "fa" || Locale.getDefault().language == "ar"
    
    if (isRunning) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF00382E)), // رنگ سبز/کله‌غازی دارک
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF83D6B5))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_check_update_24dp), contentDescription = null, tint = Color(0xFFA0F2D0))
                    Spacer(Modifier.width(8.dp))
                    Text(if(isFa) "شما متصل هستید" else "You are Connected", color = Color(0xFFA0F2D0), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(12.dp))
                // نمایش اطلاعات آی‌پی و وضعیت اتصال
                Text(displayText, color = Color.White, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onAction: (MainAction) -> Unit,
    onNavigate: (MainDestination) -> Unit,
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val groups = uiState.groups
    val isLoading by mainViewModel.isLoading.collectAsStateWithLifecycle()
    val isRunning = uiState.isRunning
    val displayText = mainViewModel.formatStatus(uiState.status)
    val selectedGuid = uiState.selectedGuid
    val doubleColumnDisplay = uiState.doubleColumnDisplay
    val confirmRemove = uiState.confirmRemove
    val shareQRCodeBitmap = uiState.shareQRCodeBitmap

    val isDarkTheme = LocalDarkTheme.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showDelAllConfirm by remember { mutableStateOf(false) }
    var showDelDuplicateConfirm by remember { mutableStateOf(false) }
    var showDelInvalidConfirm by remember { mutableStateOf(false) }
    var showRemoveConfirm by remember { mutableStateOf<String?>(null) }

    var shareTarget by remember { mutableStateOf<Triple<String, ProfileItem, Boolean>?>(null) }
    val removeServer: (String) -> Unit = { guid ->
        if (confirmRemove) showRemoveConfirm = guid else onAction(MainAction.RemoveServer(guid))
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { groups.size.coerceAtLeast(1) }
    )

    val lazyListStates = remember { mutableStateMapOf<String, LazyListState>() }
    val lazyGridStates = remember { mutableStateMapOf<String, LazyGridState>() }

    LaunchedEffect(groups) {
        val validGroupIds = groups.map { it.id }.toSet()
        lazyListStates.keys.retainAll(validGroupIds)
        lazyGridStates.keys.retainAll(validGroupIds)
    }

    LaunchedEffect(groups, uiState.selectedGroupId) {
        if (groups.isEmpty()) return@LaunchedEffect
        val selectedIndex = groups.indexOfFirst { it.id == uiState.selectedGroupId }
            .takeIf { it >= 0 } ?: 0
        if (!pagerState.isScrollInProgress && pagerState.settledPage != selectedIndex) {
            pagerState.scrollToPage(selectedIndex)
        }
    }

    val latestGroups by rememberUpdatedState(groups)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val currentGroups = latestGroups
                if (page in currentGroups.indices) {
                    onAction(MainAction.SelectGroup(currentGroups[page].id))
                }
            }
    }

    MainDialogs(
        showDelAllConfirm = showDelAllConfirm,
        onDismissDelAll = { showDelAllConfirm = false },
        onConfirmDelAll = { showDelAllConfirm = false; onAction(MainAction.RemoveAllServers) },
        showDelDuplicateConfirm = showDelDuplicateConfirm,
        onDismissDelDuplicate = { showDelDuplicateConfirm = false },
        onConfirmDelDuplicate = { showDelDuplicateConfirm = false; onAction(MainAction.RemoveDuplicateServers) },
        showDelInvalidConfirm = showDelInvalidConfirm,
        onDismissDelInvalid = { showDelInvalidConfirm = false },
        onConfirmDelInvalid = { showDelInvalidConfirm = false; onAction(MainAction.RemoveInvalidServers) },
        showRemoveConfirm = showRemoveConfirm,
        onDismissRemove = { showRemoveConfirm = null },
        onConfirmRemove = { guid -> showRemoveConfirm = null; onAction(MainAction.RemoveServer(guid)) }
    )

    if (shareTarget != null) {
        val (guid, profile, more) = shareTarget!!
        ShareMethodDialog(
            guid = guid,
            profile = profile,
            more = more,
            onDismiss = { shareTarget = null },
            onAction = onAction,
            onRemove = removeServer,
        )
    }
    if (shareQRCodeBitmap != null) {
        QRCodeDialog(bitmap = shareQRCodeBitmap, onDismiss = { onAction(MainAction.DismissQRCodeDialog) })
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainDrawerContent(
                drawerState = drawerState,
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    onNavigate(route)
                }
            )
        }
    ) {
        Scaffold(
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
            topBar = {
                MainTopBar(
                    isLoading = isLoading,
                    showSearch = showSearch,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { query: String ->
                        searchQuery = query
                        onAction(MainAction.Search(query))
                    },
                    onSearchClose = {
                        searchQuery = ""
                        onAction(MainAction.Search(""))
                        showSearch = false
                    },
                    onSearchToggle = { show: Boolean -> showSearch = show },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onAction = onAction,
                    onMoreMenuAction = { action ->
                        when (action) {
                            MainMoreMenuAction.RestartService -> onAction(MainAction.RestartService)
                            MainMoreMenuAction.DeleteAll -> showDelAllConfirm = true
                            MainMoreMenuAction.DeleteDuplicate -> showDelDuplicateConfirm = true
                            MainMoreMenuAction.DeleteInvalid -> showDelInvalidConfirm = true
                            MainMoreMenuAction.ExportAll -> onAction(MainAction.ExportAll)
                            MainMoreMenuAction.LocateSelected -> onAction(MainAction.LocateSelectedServer)
                            MainMoreMenuAction.SortByTestResults -> onAction(MainAction.SortByTestResults)
                            MainMoreMenuAction.TestAll -> onAction(MainAction.TestAllServers)
                            MainMoreMenuAction.TestAllRealPing -> onAction(MainAction.TestRealAllServers)
                            MainMoreMenuAction.UpdateSubscriptions -> onAction(MainAction.UpdateSubscriptions)
                        }
                    }
                )
            },
            bottomBar = {
                MainBottomBar(
                    displayText = displayText,
                    isRunning = isRunning,
                    isDarkTheme = isDarkTheme,
                    onAction = onAction
                )
            },
            floatingActionButton = {},
        ) { innerPadding ->
            val layoutDirection = LocalLayoutDirection.current

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFF1E1E1E)) 
            ) {
                // نمایش کارت هوشمند انتخاب سرور
                AutoConnectFastestCard(mainViewModel = mainViewModel, onAction = onAction)
                
                // نمایش باکس زیبای آی‌پی و وضعیت فقط در زمان اتصال
                ConnectionStatusCard(isRunning = isRunning, displayText = displayText)

                if (groups.isNotEmpty()) {
                    if (groups.size > 1) {
                        GroupTabBar(
                            groups = groups,
                            selectedTabIndex = pagerState.currentPage.coerceIn(0, groups.lastIndex),
                            mainViewModel = mainViewModel,
                            onTabClick = { targetIndex ->
                                scope.launch {
                                    pagerState.navigateToPageOptimized(
                                        targetPage = targetIndex,
                                        animateAdjacentPage = true
                                    )
                                }
                            }
                        )
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = true,
                        beyondViewportPageCount = 1,
                        key = { page -> groups.getOrNull(page)?.id ?: "group-page-$page" }
                    ) { page ->
                        val group = groups.getOrNull(page) ?: return@HorizontalPager

                        GroupPagerPage(
                            groupId = group.id,
                            mainViewModel = mainViewModel,
                            selectedGuid = selectedGuid,
                            locateTarget = uiState.locateTarget,
                            doubleColumnDisplay = doubleColumnDisplay,
                            searchQuery = searchQuery,
                            lazyListStates = lazyListStates,
                            lazyGridStates = lazyGridStates,
                            onSelectServer = { guid -> onAction(MainAction.SelectServer(guid)) },
                            onEditServer = { guid, profile -> onAction(MainAction.EditServer(guid, profile)) },
                            onShareServer = { guid, profile ->
                                shareTarget = Triple(guid, profile, false)
                            },
                            onMoreServer = { guid, profile ->
                                shareTarget = Triple(guid, profile, true)
                            },
                            onRemoveServer = removeServer,
                            contentPadding = PaddingValues(
                                start = 0.dp,
                                top = 0.dp,
                                end = 0.dp,
                                bottom = 80.dp
                            )
                        )
                    }
                }
            }
        }
    }
}
