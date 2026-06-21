package com.happyhouse.challa.presentation.home

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.home.model.Room
import com.happyhouse.challa.presentation.home.model.RoomStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@HiltViewModel
class HomeViewModel
    @Inject
    constructor() :
    BaseViewModel<HomeState, HomeIntent, HomeSideEffect>(
            initialState = HomeState(isLoading = true),
        ) {
        init {
            loadHome()
        }

        override fun onIntent(intent: HomeIntent) {
            viewModelScope.launch {
                when (intent) {
                    HomeIntent.ClickCreateRoom -> HomeSideEffect.RoomCreationRequested
                    HomeIntent.ClickInviteCode -> HomeSideEffect.InviteCodeEntryRequested
                    is HomeIntent.ClickRoom -> HomeSideEffect.RoomSelected(intent.room)
                }.also { sideEffect ->
                    sendEffect(sideEffect)
                }
            }
        }

        private fun loadHome() {
            viewModelScope.launch {
                updateState { copy(isLoading = true) }
                delay(1000L) // TODO JH: API 호출
                val mock = mockHomeData()
                updateState {
                    copy(
                        isLoading = false,
                        userName = mock.userName,
                        rooms = mock.rooms,
                    )
                }
            }
        }

        private data class MockHome(
            val userName: String,
            val rooms: ImmutableList<Room>,
        )

        private fun mockHomeData(): MockHome =
            MockHome(
                userName = "윤서연",
                rooms =
                    persistentListOf(
                        Room(
                            id = "1",
                            name = "오사카 졸업여행",
                            status = RoomStatus.Shooting(taken = 12, total = 24),
                        ),
                        Room(
                            id = "2",
                            name = "제주 워크샵",
                            status =
                                RoomStatus.Waiting(
                                    dDay = 0,
                                    remaining = 2.hours + 47.minutes,
                                ),
                        ),
                        Room(
                            id = "3",
                            name = "다낭 4박5일",
                            status = RoomStatus.Opened,
                        ),
                        Room(
                            id = "4",
                            name = "부산 1박",
                            status = RoomStatus.Expiring(dDay = 2),
                        ),
                    ),
            )
    }
