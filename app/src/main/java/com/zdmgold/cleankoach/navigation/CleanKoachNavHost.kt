package com.zdmgold.cleankoach.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.zdmgold.cleankoach.feature.duplicates.DuplicatesScreen
import com.zdmgold.cleankoach.feature.home.HomeScreen
import com.zdmgold.cleankoach.feature.largefiles.LargeFilesScreen
import com.zdmgold.cleankoach.feature.screenshots.ScreenshotsScreen
import com.zdmgold.cleankoach.feature.similar.SimilarPhotosScreen

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
                onOpenPhotoOptimizer = { },
                onOpenVideoOptimizer = { },
                onOpenActivityMonitor = { },
                onOpenWifiSecurity = { },
                onOpenNetworkSpeed = { }
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
    }
}
