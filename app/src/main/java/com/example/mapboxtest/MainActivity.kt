package com.example.mapboxtest

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mapboxtest.ui.theme.MapBoxTestTheme
import com.example.mapboxtest.ui.theme.appColors


private val basePermissions = listOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_WIFI_STATE,
    Manifest.permission.CHANGE_WIFI_STATE,
    Manifest.permission.CHANGE_NETWORK_STATE,
    Manifest.permission.ACCESS_NETWORK_STATE,
    Manifest.permission.WAKE_LOCK,
    Manifest.permission.FOREGROUND_SERVICE,
    Manifest.permission.ACTIVITY_RECOGNITION,
    Manifest.permission.ACCESS_BACKGROUND_LOCATION
)

@SuppressLint("InlinedApi")
private val permissionsRequired = permissionsCompat(basePermissions,
    Build.VERSION_CODES.TIRAMISU to listOf(
        Manifest.permission.NEARBY_WIFI_DEVICES,
        Manifest.permission.POST_NOTIFICATIONS
    ),
    Build.VERSION_CODES.S to listOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE
    ),
    -Build.VERSION_CODES.R to listOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
    )
)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MainActivityViewModel = viewModel()
            val state by vm.uiState.collectAsState()
            MapBoxTestTheme {
                Scaffold(
                    topBar = {
                        AppBar(
                            state = state,
                            onStartMockTracking = {
                                vm.startMockTrajectory()
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                        ,
                    ) {
                        PermissionsProvider(permissionsRequired) { allGranted, request ->
                            if (allGranted) {
                                when (val s = state) {
                                    is MainUiState.Init -> {}
                                    is MainUiState.Error -> ErrorScreen(s)
                                    is MainUiState.Ready, is MainUiState.Tracking -> MapScreen(
                                        state = s,
                                        onStartClicked = {
                                            vm.startSession()
                                        },
                                        onStopClicked = {
                                            vm.stop()
                                        },
                                        locationProvider = vm.locationProvider
                                    )
                                    is MainUiState.Registering -> RegisteringScreen(s)
                                }
                            } else {
                                Button(
                                    onClick = { request() }
                                ) {
                                    Text("Request permissions")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun BoxScope.RegisteringScreen(state: MainUiState.Registering) {
    Surface(
        modifier = Modifier
            .align(Alignment.Center)
    ) {
        Text(
            text = "Registering device..."
        )
    }
}

@Composable
fun BoxScope.ErrorScreen(state: MainUiState.Error) {
    Surface(
        modifier = Modifier
            .align(Alignment.Center)
    ) {
        Text(
            text = "Error: ${state.message}"
        )
    }
}

@Preview
@Composable
fun MapScreenPreview() {
    MapBoxTestTheme {
        Box(
            Modifier.fillMaxSize()
        ) {
            MapScreen(
                MainUiState.Ready("1234"),
                onStartClicked = {},
                onStopClicked = {},
                locationProvider = UnknotLocationProvider()
            )
        }
    }
}


@Composable
fun BoxScope.MapScreen(
    state: MainUiState,
    modifier: Modifier = Modifier,
    locationProvider: UnknotLocationProvider,
    onStartClicked: () -> Unit,
    onStopClicked: () -> Unit
) {
    val sessionStarted = state is MainUiState.Tracking

    MainMap(state, locationProvider)

    StartStopButton(sessionStarted, onStartClicked, onStopClicked)

    if (state is MainUiState.Tracking) {
        val location by locationProvider.state.collectAsState()
        val level = location?.level

        // Draw level indicator
        if (level != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(10.dp))
                    .padding(5.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.baseline_stairs_24),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer)
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = key2name(level),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun BoxScope.StartStopButton(
    sessionStarted: Boolean,
    onStartClicked: () -> Unit,
    onStopClicked: () -> Unit
) {

    val align by animateAlignmentAsState(
        if (sessionStarted) Alignment.BottomStart
        else Alignment.BottomCenter
    )
    val paddingStart by animateDpAsState(
        if (sessionStarted) 20.dp
        else 0.dp
    )
    val buttonPadding by animateDpAsState(
        if (!sessionStarted) 15.dp
        else 0.dp
    )
    val morphProgress by animateFloatAsState(
        if (sessionStarted) 1f
        else 0f,
        animationSpec = tween(500)
    )
    val buttonColor by animateColorAsState(
        colorResource(
            if (sessionStarted) R.color.stop_bkg
            else R.color.start_bkg
        )
    )
    val iconColor by animateColorAsState(
        if (sessionStarted) Color.White.copy(0.8f)
        else Color.White
    )

    Row(
        modifier = Modifier
            .align(align)
            .padding(bottom = 20.dp, start = paddingStart)
            .height(50.dp)
            .widthIn(min = 50.dp)
            .shadow(3.dp, shape = RoundedCornerShape(25.dp))
            .clip(RoundedCornerShape(25.dp))
            .clickable {
                if (sessionStarted) onStopClicked()
                else onStartClicked()
            }
            .background(buttonColor)
            .padding(horizontal = buttonPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        StartStopMorph(
            modifier = Modifier
                .size(25.dp)
            ,
            progress = morphProgress,
            color = iconColor
        )

        AnimatedVisibility(
            visible = !sessionStarted,
        ) {
            Text(
                modifier = Modifier
                    .padding(start = 10.dp)
                ,
                text = "Start".uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun StartStopMorph(
    progress: Float,
    color: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .drawBehind {
                val p = 1 - progress
                drawPath(
                    path = Path().apply {
                        lineTo(0f, size.height)
                        lineTo(size.width, size.height - (size.height / 2 * p))
                        lineTo(size.width, size.height / 2 * p)
                        close()
                    },
                    color = color
                )
            }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    state: MainUiState,
    modifier: Modifier = Modifier,
    onStartMockTracking: () -> Unit,
) {
    val barColors = TopAppBarDefaults.topAppBarColors()
    TopAppBar(
        modifier = modifier,
        colors = barColors,
        title = {
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .widthIn(max = 190.dp)
                    .padding(end = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.weight(1.5f),
                    painter = painterResource(R.drawable.unknot_logo_crop),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.appColors.unknotLogo)
                )

                VerticalDivider(
                    modifier = Modifier
                        .padding(horizontal = 15.dp)
                )

                SubLogo(
                    modifier = Modifier
                        .weight(1f)
                )
            }
        },
        actions = {
            if (state is DeviceIdState) {
                BarActionItem(R.drawable.baseline_phone_android_24, state.deviceId)
            }

            AnimatedNullableVisibility(
                value = if (state is MainUiState.Tracking && state.sessionId != null) state else null,
                enter = fadeIn() + scaleIn(spring(0.3f)) + expandHorizontally(clip = false),
                exit = fadeOut() + scaleOut() + shrinkHorizontally()
            ) { v ->
                BarActionItem(
                    modifier = Modifier
                        .modIf(v.testing) {
                            val dash = 20f
                            val gap = 10f
                            val trans = rememberInfiniteTransition()
                            val phase by trans.animateFloat(
                                0f, dash + gap,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                )
                            )
                            dashedBorder(4.dp, Color(0xFFcc5500), 5.dp, dash, gap, phase)
                        }
                    ,
                    icon = R.drawable.baseline_explore_24,
                    text = v.sessionId!!,
                    background = colorResource(R.color.start_bkg),
                    textColor = Color.White,
                    iconColor = Color.White
                )
            }

            var showMoreMenu by remember { mutableStateOf(false) }

            Box {
                IconButton(
                    onClick = { showMoreMenu = !showMoreMenu }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_more_vert_24),
                        contentDescription = null
                    )
                }

                val ctx = LocalContext.current
                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false },
                ) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                modifier = Modifier
                                    .width(20.dp),
                                painter = painterResource(R.drawable.unknot_plain_logo_crop),
                                tint = MaterialTheme.colorScheme.onBackground,
                                contentDescription = null
                            )
                        },
                        text = {
                            Text("Unknot.${BuildConfig.BRAND_NAME} v${getAppVersion(ctx)}")
                        },
                        onClick = { }
                    )

                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Mock Session") },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier
                                    .width(20.dp),
                                painter = painterResource(R.drawable.outline_bug_report_24),
                                tint = MaterialTheme.colorScheme.onBackground,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            onStartMockTracking()
                            showMoreMenu = false
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun BarActionItem(
    @DrawableRes icon: Int,
    text: String,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.secondaryContainer,
    textColor: Color = Color.Unspecified,
    iconColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Row(
        modifier = Modifier
            .padding(end = 10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(background)
            .then(modifier)
            .padding(top = 5.dp, bottom = 5.dp, start = 5.dp, end = 8.dp)
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(iconColor)
        )

        Text(
            text = text,
            fontSize = 21.sp,
            color = textColor
        )
    }
}

@Preview
@Composable
fun AppbarPreview() {
    val state = MainUiState.Tracking(
        deviceId = "1234",
        sessionId = "5678",
        testing = true
    )

    MapBoxTestTheme {
        AppBar(
            state,
            Modifier
                .fillMaxWidth()
        ) {}
    }
}