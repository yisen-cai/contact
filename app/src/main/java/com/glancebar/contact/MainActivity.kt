package com.glancebar.contact

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.glancebar.contact.persistence.database.AppDatabase
import com.google.android.material.bottomnavigation.BottomNavigationView


/**
 * 应用主入口
 * @author Ethan Gary
 * @date 2020/12/24
 */
class MainActivity : AppCompatActivity() {

    private lateinit var navView: BottomNavigationView

    companion object {
        const val DURATION: Long = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // init database
        AppDatabase.initDatabase(this)

        setContentView(R.layout.activity_main)

        navView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_favorite,
                R.id.navigation_history,
                R.id.navigation_contacts
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)


        requestPermissions(
            arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_CALL_LOG
            ),
            0
        )
        navView.setupWithNavController(navController)
    }


    fun hideNavigator() {
        with(navView) {
            if (visibility == View.VISIBLE && alpha == 1f) {
                animate()
                    .alpha(0f)
                    .withEndAction { visibility = View.GONE }
                    .duration = DURATION
            }
        }
    }

    /**
     *
     */
    fun showNavigator() {
        with(navView) {
            if (visibility == View.GONE) {
                visibility = View.VISIBLE
                animate()
                    .alpha(1f)
                    .duration = 1
            }
        }
    }


//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        val inflater: MenuInflater = menuInflater
//        inflater.inflate(R.menu.contacts_menu, menu)
//        return true
//    }
}