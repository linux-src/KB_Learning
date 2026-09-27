package dev.kbwallet.app.profile.data

import dev.kbwallet.app.profile.domain.UserRepository
import dev.kbwallet.app.profile.presentation.ProfileState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

/**
 * Hand-written fake — see FakeCoinsRemoteDataSource for the project's testing
 * convention.
 *
 * Deliberately mirrors UserRepositoryImpl's lazy shape: [profileState] stays
 * silent until [getProfileState] has populated it. A fake backed by a plain
 * always-emitting StateFlow would hide exactly the class of bug where a
 * collector waits forever on a profile nobody loaded.
 */
class FakeUserRepository(
    private val initial: ProfileState = ProfileState(),
) : UserRepository {

    private val _profileState = MutableStateFlow<ProfileState?>(null)
    override val profileState: Flow<ProfileState> = _profileState.filterNotNull()

    override suspend fun getProfileState(): ProfileState =
        _profileState.value ?: initial.also { _profileState.value = it }

    override suspend fun saveProfileState(state: ProfileState) {
        _profileState.value = state
    }

    fun set(state: ProfileState) {
        _profileState.value = state
    }
}
