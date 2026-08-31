package com.v2ray.ang.ui.main

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.ui.compose.AppDivider
import com.v2ray.ang.ui.compose.LocalDarkTheme
import com.v2ray.ang.ui.compose.verticalScrollbar

enum class MainDestination(@DrawableRes val iconRes: Int, @StringRes val labelRes: Int) {
    Subscriptions(R.drawable.ic_subscriptions_24dp, R.string.title_sub_setting),
    PerAppProxy(R.drawable.ic_per_apps_24dp, R.string.per_app_proxy_settings),
    Routing(R.drawable.ic_routing_24dp, R.string.routing_settings_title),
    UserAssets(R.drawable.ic_file_24dp, R.string.title_user_asset_setting),
    Settings(R.drawable.ic_settings_24dp, R.string.title_settings),
    Promotion(R.drawable.ic_promotion_24dp, R.string.title_pref_promotion),
    Logcat(R.drawable.ic_logcat_24dp, R.string.title_logcat),
    CheckUpdate(R.drawable.ic_check_update_24dp, R.string.update_check_for_update),
    BackupRestore(R.drawable.ic_restore_24dp, R.string.title_configuration_backup_restore),
    About(R.drawable.ic_about_24dp, R.string.title_about)
}

private val primaryDrawerItems = listOf(
    MainDestination.Subscriptions,
    MainDestination.PerAppProxy,
    MainDestination.Routing,
    MainDestination.UserAssets,
    MainDestination.Settings
)

private val drawerItems = primaryDrawerItems + listOf(
    MainDestination.Promotion,
    MainDestination.Logcat,
    MainDestination.CheckUpdate,
    MainDestination.BackupRestore,
    MainDestination.About
)

@Composable
fun MainDrawerContent(drawerState: DrawerState, onNavigate: (MainDestination) -> Unit) {
    val drawerScrollState = rememberScrollState()

    ModalDrawerSheet(
        drawerState = drawerState,
        modifier = Modifier.fillMaxWidth(0.75f),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(drawerScrollState)
                .verticalScrollbar(drawerScrollState)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val isDarkTheme = LocalDarkTheme.current
                    Image(
                        painter = painterResource(R.mipmap.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        colorFilter = if (isDarkTheme) {
                            ColorFilter.tint(Color.White, BlendMode.SrcIn)
                        } else {
                            null
                        }
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // === اضافه شدن باکس اطلاعات کاربر در اینجا ===
            UserAccountInfoBox()
            AppDivider() 
            // ===========================================

            drawerItems.forEachIndexed { index, item ->
                if (index == primaryDrawerItems.size) AppDivider()
                NavigationDrawerItem(
                    label = { Text(stringResource(item.labelRes)) },
                    selected = false,
                    onClick = { onNavigate(item) },
                    icon = { Icon(painterResource(item.iconRes), contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }

            // === اضافه شدن دکمه خروج دقیقا در این قسمت ===
            val context = LocalContext.current // دریافت کانتکست
            
            Spacer(modifier = Modifier.weight(1f)) // این کد دکمه را به پایین صفحه هل می‌دهد
            
            Button(
                onClick = {
                    // پاک کردن اطلاعات حساب کاربری
                    val sharedPref = context.getSharedPreferences("v2rayng_user_data", android.content.Context.MODE_PRIVATE)
                    sharedPref.edit().clear().apply()
                    
                    // پاک کردن تمام سرورها برای امنیت (اختیاری)
                    com.v2ray.ang.handler.AngConfigManager.clearConfig()
                    
                    // بازگشت به صفحه لاگین
                    val intent = android.content.Intent(context, com.v2ray.ang.ui.LoginActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Text("Logout", color = Color(0xFFEB5757), fontWeight = FontWeight.Bold)
            }
            // ============================================

        } // این براکت بسته شدن Column است
    }
}

@Composable
fun UserAccountInfoBox() {
    // استفاده از SharedPreferences استاندارد به جای MmkvManager
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("v2rayng_user_data", Context.MODE_PRIVATE)
    
    val remainingData = sharedPref.getString("user_remaining_data", "نامشخص")
    val daysLeft = sharedPref.getString("user_days_left", "نامشخص")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A201C))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "وضعیت حساب شما",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "حجم باقی‌مانده: $remainingData گیگابایت", color = Color.LightGray)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "اعتبار باقی‌مانده: $daysLeft روز", color = Color.LightGray)
        }
    }
}
