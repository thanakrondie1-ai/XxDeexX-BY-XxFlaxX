package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AspectRatioOption
import com.example.data.model.IntelligenceMode
import com.example.data.model.VeoAspectRatio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app_name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("XxFlaxX Git Setup", appName)
    }

    @Test
    fun `verify aspect ratio options`() {
        val ratios = AspectRatioOption.values().map { it.ratioString }
        val required = listOf("1:1", "2:3", "3:2", "3:4", "4:3", "9:16", "16:9", "21:9")
        for (req in required) {
            assert(ratios.contains(req)) { "Missing required aspect ratio: $req" }
        }
    }

    @Test
    fun `verify veo 3 aspect ratios`() {
        val ratios = VeoAspectRatio.values().map { it.ratioString }
        assert(ratios.contains("16:9"))
        assert(ratios.contains("9:16"))
    }

    @Test
    fun `verify intelligence models`() {
        val highThinking = IntelligenceMode.HIGH_THINKING
        assertEquals("gemini-1.5-pro", highThinking.modelName)

        val searchGrounding = IntelligenceMode.SEARCH_GROUNDING
        assertEquals("gemini-1.5-flash", searchGrounding.modelName)

        val mapsGrounding = IntelligenceMode.MAPS_GROUNDING
        assertEquals("gemini-1.5-flash", mapsGrounding.modelName)

        val lowLatency = IntelligenceMode.LOW_LATENCY
        assertEquals("gemini-1.5-flash", lowLatency.modelName)
    }
}
