package com.happyhouse.challa.presentation.home

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.home.model.HomeRoomStatus
import com.happyhouse.challa.presentation.home.model.PrintState
import com.happyhouse.challa.presentation.home.model.RoomUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

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

        override fun onIntent(intent: HomeIntent) = Unit

        private fun loadHome() {
            viewModelScope.launch {
                updateState { copy(isLoading = true) }
                delay(1000L) // TODO JH: API 호출
                updateState {
                    copy(
                        isLoading = false,
                        // TODO JH: API 연동 시 실제 유저 정보로 대체
                        nickname = "나는야멋쟁이토마토",
                        profileImageUrl = "https://picsum.photos/250/250",
                        // 아래 이미지 URL들은 캐싱 방지를 위해 뒷 숫자를 1씩 증가시켜 유니크하게 유지
                        // TODO JH: API 연동 시 실제 방 목록으로 대체
                        //  방이 없는 상태(케이스 1)를 보려면 persistentListOf()로 교체
                        rooms =
                            persistentListOf(
                                RoomUiModel(
                                    id = "1",
                                    name = "친구들과 강릉 여행",
                                    participantCount = 1,
                                    status =
                                        HomeRoomStatus.Shooting(
                                            takenCount = 24,
                                            coverImageUrl = "https://picsum.photos/250/251",
                                        ),
                                ),
                                RoomUiModel(
                                    id = "2",
                                    name = "제주도 우정여행",
                                    participantCount = 4,
                                    status =
                                        HomeRoomStatus.Shooting(
                                            takenCount = 12,
                                            coverImageUrl = "https://picsum.photos/250/252",
                                        ),
                                ),
                                RoomUiModel(
                                    id = "3",
                                    name = "친구들과 강릉 여행",
                                    participantCount = 11,
                                    status =
                                        HomeRoomStatus.Completed(
                                            printState = PrintState.WAITING,
                                            photoImageUrls =
                                                persistentListOf(
                                                    "https://picsum.photos/250/253",
                                                    "https://picsum.photos/250/254",
                                                    "https://picsum.photos/250/255",
                                                    "https://picsum.photos/250/256",
                                                ),
                                            totalPhotoCount = 24,
                                        ),
                                ),
                                RoomUiModel(
                                    id = "4",
                                    name = "인화 완료 된 방이에요",
                                    participantCount = 7,
                                    status =
                                        HomeRoomStatus.Completed(
                                            printState = PrintState.COMPLETED,
                                            photoImageUrls =
                                                persistentListOf(
                                                    "https://picsum.photos/250/257",
                                                    "https://picsum.photos/250/258",
                                                    "https://picsum.photos/250/259",
                                                ),
                                            totalPhotoCount = 3,
                                        ),
                                ),
                            ),
                    )
                }
            }
        }
    }
