package com.v2ray.ang.ui.main

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

// === تابع پشتیبانی از چند زبانگی ===
fun getStr(fa: String, en: String): String {
    val lang = Locale.getDefault().language
    return if (lang == "fa" || lang == "ar") fa else en
}

// === هدر شیک و پرمیوم شبیه Windscribe ===
@Composable
fun WindscribeHeader(
    isRunning: Boolean,
    displayText: String,
    onMenuClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color(0xFF12141A))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF4A1010)) 
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onMenuClick) {
                            Icon(painterResource(R.drawable.ic_menu_24dp), contentDescription = "Menu", tint = Color.White)
                        }
                        Text(
                            text = getStr("شبکه اختصاصی", "V2RAY PRO"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = 2.sp
                        )
                        IconButton(onClick = onSearchClick) {
                            Icon(painterResource(R.drawable.ic_search_24dp), contentDescription = "Search", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isRunning) getStr("متصل", "CONNECTED") else getStr("خاموش", "OFF"),
                        color = if (isRunning) Color(0xFF83D6B5) else Color(0xFFA3A3A3),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (isRunning) getStr("ارتباط ایمن", "Secure Tunnel") else getStr("آماده اتصال", "Ready to Connect"),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .background(Color(0xFF5B4B1A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.ic_routing_24dp), contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = getStr("وضعیت شبکه", "Network Status"), color = Color.White, fontSize = 14.sp)
                    }
                    Text(text = displayText, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // دکمه بزرگ اتصال
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 24.dp)
                .size(72.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(if (isRunning) Color(0xFF83D6B5) else Color.White)
                .clickable { onConnectClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(if (isRunning) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play),
                contentDescription = "Connect",
                tint = if (isRunning) Color.White else Color(0xFF191919),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// === بنر حساب کاربری پایین لیست ===
@Composable
fun WindscribeAccountBanner() {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("v2rayng_user_data", Context.MODE_PRIVATE)
    val remaining = sharedPref.getString("user_remaining_data", "0") ?: "0"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D24)),
        border = BorderStroke(1.dp, Color(0xFF2A2D35)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00382E))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("$remaining\nGB", color = Color(0xFF66E2B3), fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(getStr("دسترسی پرمیوم فعال", "Premium Access Active"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(getStr("از اینترنت بدون محدودیت لذت ببرید", "Enjoy unlimited everything"), color = Color(0xFF66E2B3), fontSize = 13.sp)
            }
            Icon(
                painter = painterResource(android.R.drawable.ic_media_next), 
                contentDescription = null, 
                tint = Color(0xFFA3A3A3)
            )
        }
    }
}

// === بدنه اصلی برنامه ===
@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onAction: (MainAction) -> Unit,
    onNavigate: (MainDestination) -> Unit,
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val groups = uiState.groups
    val isRunning = uiState.isRunning
    val displayText = mainViewModel.formatStatus(uiState.status)
    val selectedGuid = uiState.selectedGuid
    val confirmRemove = uiState.confirmRemove
    val shareQRCodeBitmap = uiState.shareQRCodeBitmap
    val doubleColumnDisplay = uiState.doubleColumnDisplay

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

    LaunchedEffect(groups, uiState.selectedGroupId) {
        if (groups.isEmpty()) return@LaunchedEffect
        val selectedIndex = groups.indexOfFirst { it.id == uiState.selectedGroupId }.takeIf { it >= 0 } ?: 0
        if (!pagerState.isScrollInProgress && pagerState.settledPage != selectedIndex) {
            pagerState.scrollToPage(selectedIndex)
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
        ShareMethodDialog(guid = guid, profile = profile, more = more, onDismiss = { shareTarget = null }, onAction = onAction, onRemove = removeServer)
    }
    if (shareQRCodeBitmap != null) {
        QRCodeDialog(bitmap = shareQRCodeBitmap, onDismiss = { onAction(MainAction.DismissQRCodeDialog) })
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { MainDrawerContent(drawerState = drawerState, onNavigate = { route -> scope.launch { drawerState.close() }; onNavigate(route) }) }
    ) {
        Scaffold(
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
            topBar = {},
            bottomBar = {}, // نوار پایینی پیش فرض حذف شد تا رابط کاربری دقیقا شبیه عکس شود
            floatingActionButton = {},
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF12141A)) // رنگ پس‌زمینه دارک
            ) {
                WindscribeHeader(
                    isRunning = isRunning,
                    displayText = displayText,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onConnectClick = { 
                        // اکشن صحیح بر اساس سورس اختصاصی شما جایگزین شد
                        onAction(MainAction.ToggleService) 
                    },
                    onSearchClick = { showSearch = !showSearch }
                )

                if (showSearch) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it; onAction(MainAction.Search(it)) },
                        placeholder = { Text(getStr("جستجو...", "Search..."), color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFF1A1D24), unfocusedContainerColor = Color(0xFF1A1D24), focusedTextColor = Color.White)
                    )
                }

                if (groups.isNotEmpty()) {
                    Box(modifier = Modifier.weight(1f)) {
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
                                onShareServer = { guid, profile -> shareTarget = Triple(guid, profile, false) },
                                onMoreServer = { guid, profile -> shareTarget = Triple(guid, profile, true) },
                                onRemoveServer = removeServer,
                                contentPadding = PaddingValues(bottom = 20.dp)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                WindscribeAccountBanner()
                Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))
            }
        }
    }
}
