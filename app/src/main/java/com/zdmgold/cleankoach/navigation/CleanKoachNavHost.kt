package com.zdmgold.cleankoach.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.zdmgold.cleankoach.feature.activitymonitor.ActivityMonitorScreen
import com.zdmgold.cleankoach.feature.duplicates.DuplicatesScreen
import com.zdmgold.cleankoach.feature.home.HomeScreen
import com.zdmgold.cleankoach.feature.largefiles.LargeFilesScreen
import com.zdmgold.cleankoach.feature.networkspeed.NetworkSpeedScreen
import com.zdmgold.cleankoach.feature.photooptimizer.PhotoOptimizerScreen
import com.zdmgold.cleankoach.feature.screenshots.ScreenshotsScreen
import com.zdmgold.cleankoach.feature.similar.SimilarPhotosScreen
import com.zdmgold.cleankoach.feature.videooptimizer.VideoOptimizerScreen
import com.zdmgold.cleankoach.feature.wifisecurity.WifiSecurityScreen

@Composable
fun CleanKoachNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenSettings = { },
                onOpenLargeFiles = { navController.navigate(Routes.LARGE_FILES) },
                onOpenDuplicates = { navController.navigate(Routes.DUPLICATES) },
                onOpenSimilar = { navController.navigate(Routes.SIMILAR_PHOTOS) },
                onOpenScreenshots = { navController.navigate(Routes.SCREENSHOTS) },
                onOpenPhotoOptimizer = { navController.navigate(Routes.PHOTO_OPTIMIZER) },
                onOpenVideoOptimizer = { navController.navigate(Routes.VIDEO_OPTIMIZER) },
                onOpenActivityMonitor = { navController.navigate(Routes.ACTIVITY_MONITOR) },
                onOpenWifiSecurity = { navController.navigate(Routes.WIFI_SECURITY) },
                onOpenNetworkSpeed = { navController.navigate(Routes.NETWORK_SPEED) }
            )
        }

        composable(Routes.LARGE_FILES) {
            LargeFilesScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.DUPLICATES) {
            DuplicatesScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SIMILAR_PHOTOS) {
            SimilarPhotosScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SCREENSHOTS) {
            ScreenshotsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PHOTO_OPTIMIZER) {
            PhotoOptimizerScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.VIDEO_OPTIMIZER) {
            VideoOptimizerScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.ACTIVITY_MONITOR) {
            ActivityMonitorScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.WIFI_SECURITY) {
            WifiSecurityScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.NETWORK_SPEED) {
            NetworkSpeedScreen(onBack = { navController.popBackStack() })
        }
    }
}
