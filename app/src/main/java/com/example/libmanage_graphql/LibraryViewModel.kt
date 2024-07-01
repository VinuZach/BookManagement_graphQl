package com.example.libmanage_graphql

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.example.LibManage_graphQlTheme.AllAuthorWithNameAndIdOnlyQuery
import com.example.LibManage_graphQlTheme.AuthorQuery
import com.example.LibManage_graphQlTheme.BookListWithAuthorQuery
import com.example.LibManage_graphQlTheme.CreateBooksMutation
import com.example.LibManage_graphQlTheme.CreateGenreMutation
import com.example.LibManage_graphQlTheme.CreateNewAuthorMutation
import com.example.LibManage_graphQlTheme.DeleteUserByIDMutation
import com.example.LibManage_graphQlTheme.GenreQuery
import com.example.LibManage_graphQlTheme.UpdateAuthorDetailsMutation
import com.example.LibManage_graphQlTheme.type.LibmanageBooksAvailabilityStatusChoices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val BOOKS = "Books"
const val AUTHORS = "Authors"
const val GENRES = "Genres"

class LibraryViewModel : ViewModel()
{
    private val apolloClient = ApolloClient.Builder().serverUrl("http://192.168.1.39:8000/libs_mag/author").build()

    private val allBooksWithAuthorStateFlow =
        MutableStateFlow<List<BookListWithAuthorQuery.AllBook>>(emptyList()) // private mutable state flow
    val allBooksWithNameFlow = allBooksWithAuthorStateFlow.asStateFlow()

    private val allAuthorsList = MutableStateFlow<List<AuthorQuery.AllAuthor>>(emptyList()) // private mutable state flow
    val allAuthorListFlow = allAuthorsList.asStateFlow()

    private val allGenreList = MutableStateFlow<List<GenreQuery.AllGenre>>(emptyList()) // private mutable state flow
    val allGenreListFlow = allGenreList.asStateFlow()


    private fun getAllBooksWithAuthor()
    {
        viewModelScope.launch {
            Log.d("asdsadsad", "hhhh: 11")

            try
            {
                val data = apolloClient.query(BookListWithAuthorQuery()).execute().data?.allBooks
                data?.let {
                    allBooksWithAuthorStateFlow.value = it
                }

            } catch (e: Exception)
            {
                e.printStackTrace()
            }
        }


    }


    private fun getAllAuthorsList()
    {
        viewModelScope.launch {
            Log.d("asdsadsad", "hhhh: 11")

            try
            {
                val data = apolloClient.query(AuthorQuery()).execute().data?.allAuthors
                data?.let {
                    allAuthorsList.value = it
                }

            } catch (e: Exception)
            {
                e.printStackTrace()
            }
        }


    }

    fun getAllGenreList(onDone: (() -> Unit)?)
    {
        viewModelScope.launch {
            Log.d("asdsadsad", "hhhh: 11")

            try
            {
                val data = apolloClient.query(GenreQuery()).execute().data?.allGenre
                data?.let {
                    allGenreList.value = it
                }

            } catch (e: Exception)
            {
                e.printStackTrace()
            }
        }.invokeOnCompletion {
            onDone?.invoke()
        }


    }

    fun retrieveData(value: String)
    {
        Log.d("asdsd", "retrieveData: $value")
        allBooksWithAuthorStateFlow.value = emptyList()
        allAuthorsList.value = emptyList()
        allGenreList.value = emptyList()
        when (value)
        {
            "Books"   -> getAllBooksWithAuthor()
            "Authors" -> getAllAuthorsList()
            "Genres"  -> getAllGenreList(null)
        }
    }

    fun createNewAuthor(authorName: String, authorRating: Float, onDone: () -> Unit)
    {
        viewModelScope.launch {
            try
            {
                apolloClient.mutation(CreateNewAuthorMutation(authorName, authorRating.toInt())).execute()
            } catch (e: Exception)
            {
                e.printStackTrace()
            }
        }.invokeOnCompletion {
            onDone.invoke()
        }

    }

    fun createNewGenre(genreName: String, onDone: () -> Unit)
    {
        viewModelScope.launch {
            try
            {
                apolloClient.mutation(CreateGenreMutation(genreName)).execute()
            } catch (e: Exception)
            {
                e.printStackTrace()
            }
        }.invokeOnCompletion {
            onDone.invoke()
        }

    }

    fun createNewBook(bookName: String, selectedAuthor: AllAuthorWithNameAndIdOnlyQuery.AllAuthor, selectedGenre: GenreQuery.AllGenre,
        onDone: () -> Unit)
    {
        viewModelScope.launch {
            try
            {
                apolloClient.mutation(CreateBooksMutation(selectedAuthor.id.toInt(), LibmanageBooksAvailabilityStatusChoices.UP.name,
                    selectedGenre.id.toInt(), bookName)).execute()
            } catch (e: Exception)
            {
                e.printStackTrace()
            }
        }.invokeOnCompletion {
            onDone.invoke()
        }

    }

    fun updateAuthor(authorId: String, authorName: String, authorRating: Int, onDone: () -> Unit)
    {
        viewModelScope.launch {
            try
            {
                apolloClient.mutation(
                    UpdateAuthorDetailsMutation(idValue = Optional.present(authorId), authorRatingValue = authorRating, authorName))
                    .execute()
            } catch (e: Exception)
            {
                e.printStackTrace()
            }
        }.invokeOnCompletion {
            onDone.invoke()
        }

    }

    fun deleteAuthor(id: String, onDone: () -> Unit)
    {
        viewModelScope.launch {
            try
            {
                apolloClient.mutation(DeleteUserByIDMutation(idValue = Optional.present(id))).execute()
            } catch (e: Exception)
            {
                e.printStackTrace()
            }
        }.invokeOnCompletion {
            onDone.invoke()
        }
    }


    private val allAuthorNameAndID =
        MutableStateFlow<List<AllAuthorWithNameAndIdOnlyQuery.AllAuthor>?>(emptyList()) // private mutable state flow
    val allAuthorNameAndIDFlow = allAuthorNameAndID.asStateFlow()

    fun retrieveAuthorsWithNameAndID(onDone: () -> Unit)
    {
        viewModelScope.launch {
            try
            {
                allAuthorNameAndID.value = apolloClient.query(AllAuthorWithNameAndIdOnlyQuery()).execute().data?.allAuthors
            } catch (e: Exception)
            {
                e.printStackTrace()
            }
        }.invokeOnCompletion {
            onDone.invoke()
        }
    }
}