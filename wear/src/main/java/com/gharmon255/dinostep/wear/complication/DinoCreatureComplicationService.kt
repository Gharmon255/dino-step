package com.gharmon255.dinostep.wear.complication

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.gharmon255.dinostep.wear.WearApplication

/**
 * Watch-face complication for Samsung / Wear OS faces.
 * Add via watch face customize → complications → Dino Step.
 */
class DinoCreatureComplicationService : SuspendingComplicationDataSourceService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val repository = (application as WearApplication).watchStateRepository
        val state = repository.ensureStateForComplication()
        return DinoComplicationDataFactory.build(
            context = applicationContext,
            type = request.complicationType,
            state = state,
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return DinoComplicationDataFactory.preview(applicationContext, type)
    }
}
