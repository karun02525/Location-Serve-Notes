package `in`.antef.geonote.ui.screens.home

import ProductItem
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import `in`.antef.geonote.domain.model.ProjectModel
import `in`.antef.geonote.ui.components.CustomAlertDialog
import `in`.antef.geonote.ui.components.CustomBottomSheet
import `in`.antef.geonote.ui.components.CustomButton
import `in`.antef.geonote.ui.components.HomeTopBar
import `in`.antef.geonote.ui.components.MenuCustomBottomSheet
import `in`.antef.geonote.ui.components.NoDataMessage
import `in`.antef.geonote.ui.components.SearchComponents
import `in`.antef.geonote.ui.screens.navigation.Navigation
import `in`.antef.geonote.ui.theme.BG_COLOR
import `in`.antef.geonote.ui.viewmodel.HomeViewModel
import `in`.antef.geonote.ui.viewmodel.UIState
import `in`.antef.geonote.utils.toast
import org.koin.androidx.compose.koinViewModel


@Composable
fun HomeScreen(
    navStack: MutableList<Any>,
    viewModel: HomeViewModel = koinViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val createSheet = remember { mutableStateOf(false) }
    val editSheet = remember { mutableStateOf(false) }
    val projectId = remember { mutableLongStateOf(0L) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        viewModel.getProjects()
    }

    LaunchedEffect(uiState) {
        if (uiState.isSuccess) {
            navStack.add(Navigation.GoogleMapAction(projectId = uiState.projectId))
            viewModel.resetStatus()
        }
    }
    LaunchedEffect(uiState.isPendingFileUpload) {
        if (uiState.isPendingFileUpload) {
            context.toast("File upload is pending")
            viewModel.resetStatus()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
          //  HomeTopBar(onMenu = { viewModel.triggerManualSync() })
                 },
        bottomBar = {
            CustomButton {
                createSheet.value = true
                isEditing = false
                title = ""
                description = ""
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .background(BG_COLOR)
                .fillMaxSize()
                .padding(paddingValues),
        ) {


            SearchComponents(
                onSearchChanged = {
                    searchQuery = it
                })

            if (uiState.projectList.isEmpty()) {
                NoDataMessage(
                    message = "Oops! No projects here yet.Your workspace is waiting. Start your first project now!",
                )
            } else {
                ProductList(
                    uiState,
                    searchQuery = searchQuery,
                    onMenuClick = {
                        projectId.longValue = it.projectId
                        title = it.title
                        description = it.description
                        editSheet.value = true
                    },
                    onCardClick = {
                        navStack.add(Navigation.GoogleMapAction(projectId = it))
                    }
                )
            }
        }


        CustomBottomSheet(
            title = title,
            description = description,
            showBottomSheet = createSheet.value,
            onDismiss = { createSheet.value = false },
            onCreateClick = { newTitle, newDescription ->
                if (isEditing) {
                    viewModel.updateProject(projectId.longValue, newTitle, newDescription)
                } else {
                    viewModel.addProject(newTitle, newDescription)
                }
                createSheet.value = false
                isEditing = false
            })


        MenuCustomBottomSheet(
            showBottomSheet = editSheet.value,
            onDismiss = { editSheet.value = false },
            onDataClick = { navStack.add(Navigation.DataAction(projectId = projectId.longValue)) },
            onEditClick = {
                isEditing = true
                createSheet.value = true
            },
            onShareClick = {
                viewModel.resetStatus()
                viewModel.shareProject(projectId.longValue, context)
            },
            onDeleteClick = {
                showDeleteDialog = true
                editSheet.value = false
            }
        )

        CustomAlertDialog(
            showDialog = showDeleteDialog,
            title = "Are you sure?",
            message = "Do you want to delete this project?",
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteProject(projectId.longValue)
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )

    }
}


@Composable
fun ProductList(
    uiState: UIState,
    searchQuery: String,
    onMenuClick: (model: ProjectModel) -> Unit = {},
    onCardClick: (projectId: Long) -> Unit = {}
) {
    val filteredList = remember(uiState.projectList, searchQuery) {
        if (searchQuery.isBlank()) uiState.projectList
        else uiState.projectList.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
    ) {
        items(filteredList) {
            ProductItem(it, onMenuClick, onCardClick)
        }
    }
}
