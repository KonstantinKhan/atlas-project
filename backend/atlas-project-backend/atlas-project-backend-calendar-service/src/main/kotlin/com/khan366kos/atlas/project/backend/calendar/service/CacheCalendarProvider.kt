package com.khan366kos.atlas.project.backend.calendar.service

import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class CacheCalendarProvider(
    private val calendarRepo: IAtlasProjectTaskRepo
) {

    @Volatile
    private var cached: TimelineCalendar? = null
    private val mutex = Mutex()

    suspend fun current(): TimelineCalendar {
        return cached ?: mutex.withLock {
            cached ?: loadAndCache()
        }
    }

    private suspend fun loadAndCache(): TimelineCalendar {
        return withContext(Dispatchers.IO) {
            val calendar = calendarRepo.timelineCalendar()
            cached = calendar
            calendar
        }
    }

    suspend fun invalidate() {
        mutex.withLock {
            cached = null
        }
    }
}