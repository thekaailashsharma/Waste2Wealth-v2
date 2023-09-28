package app.waste2wealth.com.collectwaste

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import app.waste2wealth.baseUI.constants.Screens
import app.waste2wealth.baseUI.theme.CardColor
import app.waste2wealth.baseUI.theme.CardTextColor
import app.waste2wealth.baseUI.theme.appBackground
import app.waste2wealth.baseUI.R
import app.waste2wealth.baseUI.animations.MovingText
import app.waste2wealth.baseUI.theme.TintType
import app.waste2wealth.com.firebase.firestore.WasteItem
import app.waste2wealth.com.location.LocationViewModel
import app.waste2wealth.com.maps.MapScreen
import app.waste2wealth.com.reportwaste.ReportWasteViewModel
import app.waste2wealth.com.tags.Tag
import app.waste2wealth.com.tags.TagItem
import app.waste2wealth.com.tags.allTags
import app.waste2wealth.baseUI.theme.monteBold
import app.waste2wealth.baseUI.theme.monteSB
import app.waste2wealth.baseUI.theme.textColor
import app.waste2wealth.baseUI.utils.ColumnWrapper
import app.waste2wealth.baseUI.utils.DefaultCard
import app.waste2wealth.baseUI.utils.DefaultIcon
import app.waste2wealth.baseUI.utils.DefaultText
import app.waste2wealth.baseUI.utils.DefaultTextField
import app.waste2wealth.baseUI.utils.WasteItemCard
import coil.compose.AsyncImage
import com.jet.firestore.JetFirestore
import com.jet.firestore.getListOfObjects
import java.util.concurrent.TimeUnit
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun CollectWaste(
    paddingValues: PaddingValues,
    navController: NavHostController,
    viewModel: LocationViewModel,
    reportWasteViewModel: ReportWasteViewModel = hiltViewModel()
) {
    val seconds by reportWasteViewModel.tagsSearch.collectAsState(initial = "")
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var allWastes by remember { mutableStateOf<List<WasteItem>?>(null) }
    var storedWastes by remember { mutableStateOf<List<WasteItem>?>(null) }
    var searchText by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val isAtTop by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 &&
                    lazyListState.firstVisibleItemScrollOffset == 0
        }
    }
    var isSearchVisible by remember { mutableStateOf(false) }
    var isTyping by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = isAtTop) {
        if (!isAtTop) {
            focusManager.clearFocus()
            isTyping = false
            searchText = ""
            try {
                lazyListState.animateScrollToItem(0)
            } catch (e: Exception) {

            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.getPlaces()
    }

    LaunchedEffect(key1 = reportWasteViewModel.selectedTags.size){
        if (reportWasteViewModel.selectedTags.size == 0){
            allWastes = storedWastes
        }
    }

    JetFirestore(path = {
        collection("AllWastes")
    }, onRealtimeCollectionFetch = { values, _ ->
        allWastes = values?.getListOfObjects()
        storedWastes = values?.getListOfObjects()

    }) {
        ColumnWrapper(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        focusManager.clearFocus()
                    }
                )
        ) {
            AnimatedVisibility(visible = !isSearchVisible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp, start = 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBackIos,
                        contentDescription = "",
                        tint = textColor,
                        modifier = Modifier
                            .padding(start = 15.dp)
                            .size(25.dp)
                            .clickable {
                                navController.popBackStack()
                            }
                    )
                    Row(
                        modifier = Modifier
                            .offset(x = (-10).dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DefaultText(
                            text = "Collect Waste",
                            fontSize = 25.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "",
                        tint = textColor,
                        modifier = Modifier
                            .padding(end = 20.dp)
                            .size(25.dp)
                            .clickable {
                                isSearchVisible = !isSearchVisible
                            }
                    )


                }
            }
            AnimatedVisibility(visible = isSearchVisible) {
                DefaultTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        isTyping = true
                        allWastes = if (it.isBlank()) {
                            storedWastes
                        } else {
                            allWastes?.filter { wasteItem ->
                                wasteItem.doesMatchSearchQuery(searchText)
                            }
                        }
                    },
                    label = {
                        if (!isTyping && searchText == "") {
                            MovingText(targetState = seconds, text = "Search")
                        }
                    },
                    leadingIcon = {
                        DefaultIcon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tintType = TintType.OnCard
                        )
                    },
                    trailingIcon = {
                        DefaultIcon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Search",
                            tintType = TintType.OnCard,
                            modifier = Modifier.clickable {
                                isSearchVisible = !isSearchVisible
                                allWastes = storedWastes
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 30.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Search
                    ),
                    focusRequester = focusRequester,
                )
            }

            val cList = listOf("List View", "Map View (Beta)")
            var tabIndex by remember { mutableStateOf(0) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 35.dp, end = 35.dp)
            ) {
                TabRow(
                    selectedTabIndex = tabIndex,
                    backgroundColor = appBackground,
                    contentColor = textColor,
                    divider = {
                        TabRowDefaults.Divider(
                            color = Color(0xFFF37952),
                            thickness = 1.dp
                        )
                    },
                ) {
                    cList.forEachIndexed { index, title ->
                        Tab(text = {
                            DefaultText(
                                title,
                                softWrap = false,
                                fontSize = 13.sp,
                            )
                        },
                            selected = tabIndex == index,
                            onClick = { tabIndex = index }
                        )
                    }

                }

            }
            if (tabIndex == 0) {
                Spacer(modifier = Modifier.height(30.dp))
                if (allWastes != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LazyRow(
                            contentPadding = PaddingValues(
                                start = 10.dp,
                                top = 10.dp,
                                end = 30.dp,
                                bottom = 10.dp
                            )
                        ) {
                            items(allTags) { item ->
                                DefaultCard(modifier = Modifier
                                    .padding(10.dp)
                                    .clickable {
                                        if (reportWasteViewModel.selectedTags.contains(item)) {
                                            reportWasteViewModel.selectedTags.remove(item)
                                        } else {
                                            reportWasteViewModel.selectedTags.clear()
                                            reportWasteViewModel.selectedTags.add(item)
                                        }
                                        allWastes = allWastes?.filter { wasteItem ->
                                            wasteItem.tag.contains(item.mapWithoutTips())
                                        }
                                    },
                                    backgroundColor = if (reportWasteViewModel.selectedTags.contains(
                                            item
                                        )
                                    ) {
                                        CardColor
                                    } else {
                                        appBackground
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            item.image,
                                            modifier = Modifier
                                                .size(50.dp)
                                                .padding(2.dp),
                                            contentScale = ContentScale.Crop,
                                            contentDescription = "",
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Column(verticalArrangement = Arrangement.Center) {
                                            Text(
                                                text = item.name.substringBefore(" "),
                                                color = if (reportWasteViewModel.selectedTags.contains(
                                                        item)
                                                ) {
                                                    CardTextColor
                                                } else {
                                                    textColor
                                                },
                                                fontSize = 12.sp,
                                                softWrap = true
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            if (item.name.contains(" ")) {
                                                Text(
                                                    text = item.name.substringAfter(" "),
                                                    color = if (reportWasteViewModel.selectedTags.contains(
                                                            item)
                                                    ) {
                                                        CardTextColor
                                                    } else {
                                                        textColor
                                                    },
                                                    fontSize = 12.sp,
                                                    softWrap = true
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(7.dp))
                                    }
                                }
                            }
                        }
                    }
                    if (allWastes?.isEmpty() == true){
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.empty_state),
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(130.dp),
                                alignment = Alignment.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                bottom = 150.dp,
                                top = 40.dp
                            ),
                            state = lazyListState
                        ) {
                            allWastes = allWastes?.sortedBy {
                                distance(
                                    viewModel.latitude,
                                    viewModel.longitude,
                                    it.latitude,
                                    it.longitude
                                )
                            }
                            itemsIndexed(allWastes ?: emptyList()) { index, wasteItem ->
                                WasteItemCard(
                                    modifier = Modifier.animateItemPlacement(),
                                    locationNo = "Location ${index + 1}",
                                    address = wasteItem.address,
                                    distance = "${
                                        convertDistance(
                                            distance(
                                                viewModel.latitude,
                                                viewModel.longitude,
                                                wasteItem.latitude,
                                                wasteItem.longitude
                                            )
                                        )
                                    } away",
                                    time = getTimeAgo(wasteItem.timeStamp),
                                    tags = wasteItem.tag.map {
                                        it.mapWithTips()
                                    },
                                ) {
                                    viewModel.locationNo.value = "Location ${index + 1}"
                                    viewModel.address.value = wasteItem.address
                                    viewModel.distance.value = "${
                                        convertDistance(
                                            distance(
                                                viewModel.latitude,
                                                viewModel.longitude,
                                                wasteItem.latitude,
                                                wasteItem.longitude
                                            )
                                        )
                                    } away"
                                    viewModel.time.value = getTimeAgo(wasteItem.timeStamp)
                                    viewModel.wastePhoto.value = wasteItem.imagePath
                                    viewModel.theirLatitude.value = wasteItem.latitude
                                    viewModel.theirLongitude.value = wasteItem.longitude
                                    viewModel.tags.value = wasteItem.tag.map {
                                        it.mapWithTips()
                                    }
                                    println("Collected time ${viewModel.time.value}")
                                    navController.navigate(Screens.CollectWasteInfo.route)
                                }

                            }
                        }
                    }
                }
            } else {
                MapScreen(
                    paddingValues = paddingValues,
                    viewModel = viewModel,
                )
            }

        }
    }

}


private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val theta = lon1 - lon2
    var dist = (sin(deg2rad(lat1))
            * sin(deg2rad(lat2))
            + (cos(deg2rad(lat1))
            * cos(deg2rad(lat2))
            * cos(deg2rad(theta))))
    dist = acos(dist)
    dist = rad2deg(dist)
    dist *= 60 * 1.1515
    return dist
}

private fun deg2rad(deg: Double): Double {
    return deg * Math.PI / 180.0
}

private fun rad2deg(rad: Double): Double {
    return rad * 180.0 / Math.PI
}

fun convertDistance(km: Double): String {
    return if (km < 1) {
        "${(km * 1000).toInt()} mtr"
    } else {
        "${"%.2f".format(km)} km"
    }
}

fun getTimeAgo(timeInMillis: Long): String {
    val currentTime = System.currentTimeMillis()
    val elapsedTime = currentTime - timeInMillis

    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime)
    val hours = TimeUnit.MILLISECONDS.toHours(elapsedTime)
    val days = TimeUnit.MILLISECONDS.toDays(elapsedTime)
    val months = TimeUnit.MILLISECONDS.toDays(elapsedTime) / 30
    val years = TimeUnit.MILLISECONDS.toDays(elapsedTime) / 365

    return when {
        years >= 1 -> "$years years ago"
        months >= 1 -> "$months months ago"
        days >= 1 -> "$days days ago"
        hours >= 1 -> "$hours hours ago"
        else -> "$minutes minutes ago"
    }
}