package `in`.antef.geonote.ui.screens.data

import DataItem
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import `in`.antef.geonote.domain.model.Coordinate
import `in`.antef.geonote.ui.components.CustomAlertDialog
import `in`.antef.geonote.ui.components.CustomBottomSheet
import `in`.antef.geonote.ui.components.DataTopBar
import `in`.antef.geonote.ui.components.NoDataMessage
import `in`.antef.geonote.ui.components.SearchComponents
import `in`.antef.geonote.ui.screens.navigation.Navigation
import `in`.antef.geonote.ui.theme.BG_COLOR
import `in`.antef.geonote.ui.viewmodel.CoordinatesViewModel
import `in`.antef.geonote.ui.viewmodel.LocationUiState
import `in`.antef.geonote.ui.viewmodel.LocationViewModel
import org.koin.androidx.compose.koinViewModel

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DataPageScreen(
    navStack: MutableList<Any> = mutableListOf(),
    projectId: Long = 0L,
    viewModel: LocationViewModel = koinViewModel(),
    viewModelCoordinates: CoordinatesViewModel = koinViewModel(),
) {

    val uiState by viewModel.locationState.collectAsState()
    val createSheet = remember { mutableStateOf(false) }
    val editSheet = remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var coordinateId by remember { mutableLongStateOf(0L) }
    var searchQuery by remember { mutableStateOf("") }


    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        println("ProjectId:  $projectId")
        viewModel.getProject(projectId)
        viewModel.getAllCoordinates(projectId)
    }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            DataTopBar(
                text = uiState.projectName,
                onBack = {
                    navStack.clear()
                    navStack.add(Navigation.HomeAction)
                },
                goToMap = {
                    navStack.add(Navigation.GoogleMapAction(projectId = projectId))
                })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .background(BG_COLOR)
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            SearchComponents(onSearchChanged = {
                searchQuery = it
            })

            if (uiState.coordinates.isEmpty()) {
                NoDataMessage(
                    message = "Oops! No Points here yet. \nYour workspace is waiting. Add your first point now!",
                    )
            } else {
                DataList(
                    uiState,
                    searchQuery = searchQuery,
                    onMenuClick = {
                        coordinateId = it.id
                        title = it.title
                        description = it.description
                        editSheet.value = true
                    },
                )
            }
        }


        //Update Values
        CustomBottomSheet(
            header = "Update Details",
            title = title,
            description = description,
            showBottomSheet = createSheet.value,
            onDismiss = { createSheet.value = false },
            onCreateClick = { newTitle, newDescription ->
                viewModelCoordinates.updateCoordinate(coordinateId, newTitle, newDescription)
                createSheet.value = false
                isEditing = false
            })


        //Menu Click
        MenuCustomBottomSheet(
            showBottomSheet = editSheet.value,
            onDismiss = { editSheet.value = false },
            onEditClick = {
                isEditing = true
                createSheet.value = true
            },
            onDeleteClick = {
                showDeleteDialog = true
                editSheet.value = false
            }
        )

        CustomAlertDialog(
            showDialog = showDeleteDialog,
            title = "Are you sure?",
            message = "Do you want to delete this Point?",
            onConfirm = {
                showDeleteDialog = false
                 viewModelCoordinates.deleteCoordinate(coordinateId)
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )

    }
}


@Composable
fun DataList(
    uiState: LocationUiState,
    searchQuery: String,
    onMenuClick: (model: Coordinate) -> Unit = {},
    onCardClick: (id: Long) -> Unit = {}
) {
    val filteredList = remember(uiState.coordinates, searchQuery) {
        if (searchQuery.isBlank()) uiState.coordinates
        else uiState.coordinates.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
    ) {
        items(filteredList) {
            DataItem(it, onMenuClick, onCardClick)
        }
    }
}
