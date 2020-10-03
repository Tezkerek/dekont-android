package ro.ande.dekont.util

import androidx.appcompat.widget.Toolbar
import androidx.customview.widget.Openable
import androidx.navigation.NavController
import ro.ande.dekont.R

fun Toolbar.setupWithIndividualNavController(
        navController: NavController,
        drawer: Openable? = null
) {
    val currentDest = navController.currentDestination

    title = currentDest?.label

    // If at start destination, show menu button
    if (currentDest?.id == navController.graph.startDestination) {
        setNavigationIcon(R.drawable.ic_menu_24)
        setNavigationOnClickListener { drawer?.open() }
    } else {
        setNavigationIcon(R.drawable.ic_arrow_left_24)
        setNavigationOnClickListener { navController.popBackStack() }
    }

}