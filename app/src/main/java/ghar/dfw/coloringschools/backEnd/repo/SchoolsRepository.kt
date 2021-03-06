package ghar.dfw.coloringschools.backEnd.repo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ghar.dfw.coloringschools.constants.CoreConstants.Companion.MAX_TIME_OUT
import ghar.dfw.coloringschools.constants.CoreConstants.Companion.SCHOOLS_BASE_URL
import ghar.dfw.coloringschools.backEnd.model.SchoolScores
import ghar.dfw.coloringschools.backEnd.model.SchoolsBasicInfo
import ghar.dfw.coloringschools.di.DaggerComponentsGraph
import ghar.dfw.coloringschools.di.SchoolApi
import ghar.dfw.coloringschools.di.SchoolsModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import retrofit2.Retrofit
import javax.inject.Inject

class SchoolsRepository {

  private val _schoolsApiCallResponse = MutableLiveData<UIState>()
  val schoolsApiCallResponse: LiveData<UIState>
    get() = _schoolsApiCallResponse

  @Inject
  lateinit var retrofit: Retrofit

  var schoolsApi: SchoolApi
  private val schoolsGraph =
    DaggerComponentsGraph.builder().networkModule(schoolsModule = SchoolsModule(SCHOOLS_BASE_URL))
      .build()

  init {
    schoolsGraph.inject(this)
    schoolsApi = retrofit.create(SchoolApi::class.java)
  }

  suspend fun networkCall() {
    withContext(Dispatchers.IO) {
      withTimeout(MAX_TIME_OUT) {
        try {
          val schoolsInfo = schoolsApi.getSchools()
          val schoolScore = schoolsApi.getScores()
          if (!schoolsInfo.isNullOrEmpty() && schoolScore.isNotEmpty()) {
            Log.d("in-repo: ", "${schoolsInfo[5].schoolName}")
            _schoolsApiCallResponse.postValue(UIState.SuccessState(schools = schoolsInfo,
              scores = schoolScore))
          } else {
            _schoolsApiCallResponse.postValue(UIState.ErrorState("data retrieval error"))
          }

        } catch (exception: Exception) {
          exception.message.let {
            _schoolsApiCallResponse.postValue(UIState.ErrorState(it!!))
          }
        }
      }
    }
  }

  sealed class UIState {
    object EmptyState : UIState()
    class SuccessState(val schools: List<SchoolsBasicInfo>?, val scores: List<SchoolScores>?) : UIState()

    class ErrorState(val error: String) : UIState()
  }

}



