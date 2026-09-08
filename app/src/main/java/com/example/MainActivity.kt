package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.AppDatabase
import com.example.data.ProgramRepository
import com.example.ui.AddProgramScreen
import com.example.ui.ProgramViewModel
import com.example.ui.ProgramViewModelFactory
import com.example.ui.StudioPodScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = AppDatabase.getDatabase(this)
        val repository = ProgramRepository(database.programDao())
        
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: ProgramViewModel = viewModel(
                        factory = ProgramViewModelFactory(repository)
                    )
                    
                    NavHost(navController = navController, startDestination = "studiopod") {
                        composable("studiopod") {
                            val programs by viewModel.programs.collectAsStateWithLifecycle()
                            
                            StudioPodScreen(
                                programs = programs,
                                onAddClick = {
                                    navController.navigate("add_program")
                                }
                            )
                        }
                        
                        composable("add_program") {
                            AddProgramScreen(
                                onBack = { navController.popBackStack() },
                                onSave = { title, desc ->
                                    viewModel.addProgram(title, desc)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
