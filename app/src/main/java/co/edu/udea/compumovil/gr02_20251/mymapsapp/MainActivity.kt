package co.edu.udea.compumovil.gr02_20251.mymapsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text


import androidx.compose.ui.tooling.preview.Preview
import co.edu.udea.compumovil.gr02_20251.mymapsapp.ui.theme.MyMapsAppTheme
import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder

import android.widget.Toast

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyMapsAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MapScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}


@Composable
fun MapScreen(modifier: Modifier) {
    val medellin = LatLng(6.25184,  -75.56359)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(medellin, 15f)
    }
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val markerState = remember { mutableStateListOf<MarkerState>() }
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }




    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    scope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                        markerState.add(MarkerState(position = userLatLng))
                    }
                } else {
                    Toast.makeText(context, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }




    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(top = 32.dp)) { // Añadimos espacio desde el top
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar dirección") },
                    trailingIcon = {
                        IconButton(onClick = {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            scope.launch {
                                try {
                                    val addresses = withContext(Dispatchers.IO) {
                                        geocoder.getFromLocationName(searchQuery.text, 1)
                                    }
                                    if (addresses != null && addresses.isNotEmpty()) {
                                        val location = addresses[0]
                                        val latLng = LatLng(location.latitude, location.longitude)
                                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                                        markerState.add(MarkerState(position = latLng))
                                    } else {
                                        Toast.makeText(context, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }
        },
        floatingActionButton = {
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, bottom = 24.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    FloatingActionButton(
                        onClick = {
                            when {
                                ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED -> {
                                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                        if (location != null) {
                                            val userLatLng = LatLng(location.latitude, location.longitude)
                                            scope.launch {
                                                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                                                markerState.add(MarkerState(position = userLatLng))
                                            }
                                        } else {
                                            Toast.makeText(context, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                else -> {
                                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            }
                        },
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Mi ubicación")
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ),
            ) {
                markerState.forEach { Marker(state = it) }
            }
        }
    }
}
