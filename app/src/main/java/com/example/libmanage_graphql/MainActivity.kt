package com.example.libmanage_graphql

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.LibManage_graphQlTheme.AllAuthorWithNameAndIdOnlyQuery
import com.example.LibManage_graphQlTheme.AuthorQuery
import com.example.LibManage_graphQlTheme.BookListWithAuthorQuery
import com.example.LibManage_graphQlTheme.GenreQuery
import com.example.LibManage_graphQlTheme.type.LibmanageBooksAvailabilityStatusChoices
import com.example.libmanage_graphql.ui.theme.LibManage_graphQlTheme

class MainActivity : ComponentActivity()
{
    private val libraryViewModel = LibraryViewModel()

    override fun onCreate(savedInstanceState: Bundle?)
    {

        super.onCreate(savedInstanceState)
        setContent {
            val selectedSection = remember {
                mutableStateOf("")
            }
            val hideFAB = remember {
                mutableStateOf(false)
            }
            val selectedItem = remember {
                mutableStateOf<SelectedItem?>(null)
            }
            val context = LocalContext.current
            LibManage_graphQlTheme(darkTheme = false) {
                Scaffold(floatingActionButton = {
                    if (!hideFAB.value) FloatingActionButton(onClick = {
                        if (selectedSection.value.isEmpty())
                        {
                            Toast.makeText(context, "select section ", Toast.LENGTH_SHORT).show()
                            return@FloatingActionButton
                        }
                        hideFAB.value = !hideFAB.value
                        selectedItem.value = null
                    }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
                    }
                }, content = { paddingValues ->
                    MainPage(Modifier.padding(paddingValues), selectedSection, hideFAB, selectedItem)
                })


            }
        }
    }

    data class SelectedItem(val authorDetails: AuthorQuery.AllAuthor? = null,
        val bookListWithAuthorQuery: BookListWithAuthorQuery.AllBook? = null, val genre: GenreQuery.AllGenre? = null)

    data class SelectedItemForBookCreation(var authorDetails: AllAuthorWithNameAndIdOnlyQuery.AllAuthor? = null,
        var genre: GenreQuery.AllGenre? = null)

    @Composable
    fun MainPage(modifier: Modifier = Modifier, selectedSection: MutableState<String>, hideFAB: MutableState<Boolean>,
        selectedItem: MutableState<SelectedItem?>)
    {

        val sectionItemList = mutableListOf("Books", "Authors", "Genres")


        Column(modifier = modifier.fillMaxSize().padding(top = 10.dp)) {
            LazyRow(modifier = Modifier.fillMaxWidth()) {
                items(sectionItemList) {
                    Text(text = it, modifier = Modifier.padding(2.dp)
                        .background(if (selectedSection.value == it) Color.LightGray else Color.Transparent).padding(10.dp).clickable {
                            selectedSection.value = it
                            libraryViewModel.retrieveData(it)
                        })
                }
            }
            val allBooksWithAuthor = libraryViewModel.allBooksWithNameFlow.collectAsState()
            val allAuthorList = libraryViewModel.allAuthorListFlow.collectAsState()
            val allGenreList = libraryViewModel.allGenreListFlow.collectAsState()


            if (selectedSection.value.isEmpty())
            {
                Text(text = "Select Section to retrieve List", modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    textAlign = TextAlign.Center)
            }
            else if (allAuthorList.value.isEmpty() && allBooksWithAuthor.value.isEmpty() && allGenreList.value.isEmpty())
            {
                Text(text = "No data available", modifier = Modifier.fillMaxWidth().padding(top = 10.dp), textAlign = TextAlign.Center)
            }

            if (allAuthorList.value.isNotEmpty() || allBooksWithAuthor.value.isNotEmpty()) LazyColumn {
                items(items = allBooksWithAuthor.value) {
                    BooksWithAuthorItem(it)
                }

                items(items = allAuthorList.value) {
                    AuthorDetails(it, onItemDeletedDone = {
                        libraryViewModel.retrieveData(selectedSection.value)
                    }, onItemSelected = { selectedAuthor ->
                        selectedItem.value = SelectedItem(authorDetails = selectedAuthor)
                        hideFAB.value = true
                    })
                }
            }
            else if (allGenreList.value.isNotEmpty())
            {
                LazyVerticalGrid(columns = GridCells.Fixed(3)) {
                    items(allGenreList.value) {
                        Text(text = it.name,
                            modifier = Modifier.padding(5.dp).border(1.dp, Color.Black, RoundedCornerShape(1)).padding(10.dp).clickable {
                                selectedItem.value = SelectedItem(genre = it)
                                hideFAB.value = true
                            })
                    }
                }
            }

        }

        if (hideFAB.value)
        {

            CreateNewEntryData(selectedSection.value, hideFAB, selectedItem)


        }
    }

    @Composable
    private fun CreateNewEntryData(selectedSection: String, hideFAB: MutableState<Boolean>, selectedItem: MutableState<SelectedItem?>)
    {

        val interactionSource = remember { MutableInteractionSource() }

        Box(modifier = Modifier.fillMaxSize().clickable(interactionSource = interactionSource, indication = null) {

        }, Alignment.BottomStart) {
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
                .background(MaterialTheme.colorScheme.tertiary).padding(10.dp)) {
                Icon(imageVector = Icons.Filled.Clear, contentDescription = "close", modifier = Modifier.clickable {
                    hideFAB.value = false
                }.padding(10.dp).align(Alignment.End))


                var onDataSubmit: (() -> Unit)? = null
                val isButtonEnable = remember {
                    mutableStateOf(true)
                }
                val titleView: @Composable (String) -> Unit = { title ->
                    Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.padding(10.dp))
                }
                Column {
                    when (selectedSection)
                    {
                        AUTHORS ->
                        {
                            val authorName = remember {
                                mutableStateOf("")
                            }
                            val authorRating = remember {

                                mutableFloatStateOf(0.0f)
                            }
                            if (!isButtonEnable.value) CircularProgressIndicator(
                                modifier = Modifier.align(alignment = Alignment.CenterHorizontally).padding(10.dp))
                            val title = remember {
                                mutableStateOf("")
                            }
                            LaunchedEffect(key1 = false) {
                                title.value = selectedItem.value?.let { item ->
                                    Log.e("asdasdsad", "CreateNewEntryData: ")
                                    item.authorDetails?.let {
                                        authorName.value = it.name
                                        authorRating.floatValue = it.starRating.toFloat()
                                    }
                                    "Edit Author"

                                } ?: run { "Create New Author" }
                            }

                            titleView.invoke(title.value)

                            StarRatingBar(rating = authorRating.floatValue) {
                                authorRating.floatValue = it
                            }
                            Spacer(modifier = Modifier.padding(10.dp))
                            TextField(value = authorName.value, onValueChange = {
                                authorName.value = it
                            }, label = {
                                Text(text = "Author Name")
                            }, modifier = Modifier.fillMaxWidth().padding(10.dp), singleLine = true)

                            val context = LocalContext.current
                            onDataSubmit = {
                                if (authorName.value.isEmpty()) Toast.makeText(context, "Author name Required", Toast.LENGTH_SHORT).show()
                                else
                                {
                                    isButtonEnable.value = false
                                    selectedItem.value?.authorDetails?.let {
                                        libraryViewModel.updateAuthor(it.id, authorName.value, authorRating.floatValue.toInt()) {
                                            isButtonEnable.value = true
                                            hideFAB.value = false
                                            libraryViewModel.retrieveData(selectedSection)
                                        }
                                    } ?: run {
                                        libraryViewModel.createNewAuthor(authorName.value, authorRating.floatValue) {
                                            isButtonEnable.value = true
                                            hideFAB.value = false
                                            libraryViewModel.retrieveData(selectedSection)
                                        }
                                    }


                                }
                            }
                        }

                        GENRES  ->
                        {
                            val genreTitle = remember {
                                mutableStateOf("")
                            }
                            val title = remember {
                                mutableStateOf("")
                            }
                            LaunchedEffect(key1 = true) {
                                title.value = selectedItem.value?.let { item ->
                                    item.genre?.let {
                                        genreTitle.value = it.name
                                    }

                                    "Create new Genre"

                                } ?: run {
                                    "Edit  Genre"
                                }
                            }

                            titleView.invoke(title.value)

                            TextField(value = genreTitle.value, onValueChange = {
                                genreTitle.value = it
                            }, modifier = Modifier.fillMaxWidth(), label = {
                                Text(text = "Genre Name")
                            })
                            Spacer(modifier = Modifier.padding(10.dp))
                            val context = LocalContext.current
                            onDataSubmit = {
                                if (genreTitle.value.isEmpty())
                                {
                                    Toast.makeText(context, "genre Name required", Toast.LENGTH_SHORT).show()
                                }
                                else
                                {
                                    isButtonEnable.value = false
                                    libraryViewModel.createNewGenre(genreTitle.value) {
                                        isButtonEnable.value = true
                                        hideFAB.value = false
                                        libraryViewModel.retrieveData(selectedSection)

                                    }
                                }
                            }
                        }

                        BOOKS   ->
                        {
                            val selectedItemForBookCreation = remember {
                                mutableStateOf<SelectedItemForBookCreation>(SelectedItemForBookCreation())
                            }
                            titleView.invoke("Create New Book")
                            Text(text = "Select Author")
                            val retrievingData = remember {
                                mutableStateOf(true)
                            }
                            libraryViewModel.retrieveAuthorsWithNameAndID {
                                libraryViewModel.getAllGenreList {
                                    retrievingData.value = false
                                }

                            }
                            Log.d("asdasdsad", "CreateNewEntryData: $selectedItemForBookCreation")
                            if (!retrievingData.value)
                            {
                                LazyRow {
                                    libraryViewModel.allAuthorNameAndIDFlow.value?.let { allAuthors ->
                                        items(allAuthors) {
                                            val isItemSelected = selectedItemForBookCreation.value.authorDetails?.let { selectedAuthor ->
                                                Log.d("asdasdsad", "CreateNewEntryData: $selectedAuthor")
                                                if (it == selectedAuthor) true
                                                else false
                                            } ?: run {
                                                false
                                            }
                                            val color = if (isItemSelected) Color.Cyan else Color.Black
                                            Text(text = it.name,
                                                modifier = Modifier.padding(10.dp).border(1.dp, color, RoundedCornerShape(10.dp))
                                                    .padding(10.dp).clickable {

                                                        selectedItemForBookCreation.value =
                                                            SelectedItemForBookCreation(genre = selectedItemForBookCreation.value.genre,
                                                                authorDetails = it)
                                                    }, color = color)
                                        }
                                    }
                                }
                                Text(text = "Select Genre")
                                LazyRow {
                                    libraryViewModel.allGenreListFlow.value.let {
                                        items(it) {
                                            val isItemSelected = selectedItemForBookCreation.value.genre?.let { selectedGenre ->
                                                Log.d("asdasdsad", "CreateNewEntryData: $selectedGenre")
                                                if (it == selectedGenre) true
                                                else false
                                            } ?: run {
                                                false
                                            }
                                            val color = if (isItemSelected) Color.Cyan else Color.Black
                                            Text(text = it.name,
                                                modifier = Modifier.padding(10.dp).border(1.dp, color, RoundedCornerShape(10.dp))
                                                    .padding(10.dp).clickable {
                                                        selectedItemForBookCreation.value = SelectedItemForBookCreation(genre = it,
                                                            authorDetails = selectedItemForBookCreation.value.authorDetails)
                                                    }, color = color)
                                        }
                                    }
                                }
                            }
                            else
                            {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(alignment = Alignment.CenterHorizontally).padding(10.dp))
                            }
                            val bookName = remember {
                                mutableStateOf("")
                            }
                            Text(text = "Book Name")
                            TextField(value = bookName.value, onValueChange = {
                                bookName.value = it
                            }, modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text), singleLine = true)
                            val context = LocalContext.current
                            onDataSubmit = {
                                if (bookName.value.isNotEmpty() && selectedItemForBookCreation.value.authorDetails != null && selectedItemForBookCreation.value.genre != null)
                                {
                                    libraryViewModel.createNewBook(bookName.value,selectedItemForBookCreation.value.authorDetails!!,selectedItemForBookCreation.value.genre!!)
                                    {
                                        Toast.makeText(context, "Book created", Toast.LENGTH_SHORT).show()
                                        hideFAB.value=false
                                        libraryViewModel.retrieveData(selectedSection)
                                    }
                                }
                                else

                                    if (bookName.value.isEmpty())
                                    {
                                        Toast.makeText(context, "Book Name required", Toast.LENGTH_SHORT).show()

                                    }

                            }
                        }
                    }
                    Button(enabled = isButtonEnable.value, onClick = { onDataSubmit?.invoke() },
                        modifier = Modifier.align(Alignment.End).padding(horizontal = 10.dp, vertical = 5.dp)) {
                        Text(text = "Submit")
                    }
                }
            }
        }

    }


    @Composable
    fun AuthorDetails(authorDetails: AuthorQuery.AllAuthor, onItemDeletedDone: () -> Unit, onItemSelected: (AuthorQuery.AllAuthor) -> Unit)
    {
        val showDeleteDialog = remember {
            mutableStateOf(false)
        }
        Card(modifier = Modifier.padding(2.dp).clickable {
            onItemSelected.invoke(authorDetails)
        }) {
            Column(Modifier.fillMaxWidth().padding(10.dp)) {
                if (showDeleteDialog.value) AlertDialog(onDismissRequest = { }, confirmButton = {
                    Button(onClick = {
                        showDeleteDialog.value = false
                        libraryViewModel.deleteAuthor(authorDetails.id) {
                            onItemDeletedDone.invoke()
                        }
                    }) {
                        Text(text = "Delete")
                    }
                }, title = {
                    Text(text = "Delete Author")
                }, text = {
                    Text(text = "Continue to delete \n author :${authorDetails.name}")
                }, dismissButton = {
                    Button(onClick = { showDeleteDialog.value = false }) {
                        Text(text = "Cancel")
                    }
                })
                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete", modifier = Modifier.clickable {
                    showDeleteDialog.value = true
//
                }.align(Alignment.End))
                Text(text = authorDetails.name.uppercase(), fontSize = 23.sp, modifier = Modifier.padding(bottom = 10.dp))
                StarRatingBar(rating = authorDetails.starRating.toFloat()) {

                }
                Spacer(modifier = Modifier.padding(5.dp))
            }
        }
    }

    @Composable
    fun BooksWithAuthorItem(allBooksWithAuthor: BookListWithAuthorQuery.AllBook)
    {

        Card(shape = RoundedCornerShape(8), modifier = Modifier.padding(top = 5.dp, end = 5.dp, start = 5.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(15.dp)) {

                val availabilityStatus = when (allBooksWithAuthor.availabilityStatus)
                {
                    LibmanageBooksAvailabilityStatusChoices.NA -> "Not Available"
                    LibmanageBooksAvailabilityStatusChoices.A  -> "Available"
                    LibmanageBooksAvailabilityStatusChoices.L  -> "already lent"
                    LibmanageBooksAvailabilityStatusChoices.R  -> "Reserved"
                    LibmanageBooksAvailabilityStatusChoices.UP -> "Upcoming"
                    else                                       -> "N/A"
                }
                Text(text = availabilityStatus.uppercase(), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = allBooksWithAuthor.title, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = allBooksWithAuthor.genre.name, fontWeight = FontWeight.ExtraLight)
                }

                Text(text = allBooksWithAuthor.author.name, modifier = Modifier.padding(top = 4.dp, start = 3.dp),
                    fontWeight = FontWeight.Light)

            }
        }

    }

    @Composable
    fun StarRatingBar(maxStars: Int = 5, rating: Float, onRatingChanged: (Float) -> Unit)
    {
        val density = LocalDensity.current.density
        val starSize = (12f * density).dp
        val starSpacing = (0.5f * density).dp

        Row(modifier = Modifier.selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
            for (i in 1..maxStars)
            {
                val isSelected = i <= rating
                val icon = if (isSelected) Icons.Filled.Star else Icons.Default.Star
                val iconTintColor = if (isSelected) Color(0xFFFFC700) else Color(0x20FFFFFF)
                Icon(imageVector = icon, contentDescription = null, tint = iconTintColor,
                    modifier = Modifier.selectable(selected = isSelected, onClick = {
                        onRatingChanged(i.toFloat())
                    }).width(starSize).height(starSize))

                if (i < maxStars)
                {
                    Spacer(modifier = Modifier.width(starSpacing))
                }
            }
        }
    }
}

