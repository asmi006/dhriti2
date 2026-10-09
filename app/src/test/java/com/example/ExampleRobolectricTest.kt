package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.GeminiIntelligenceService
import com.example.crypto.CryptoManager
import com.example.location.LiveLocationAndRouteService
import com.example.model.IncidentCategory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Dhriti", appName)
  }

  @Test
  fun `verify aadhaar client-side masking and hashing`() {
    val rawAadhaar = "5432 8765 1234"
    val masked = CryptoManager.maskAadhaar(rawAadhaar)
    assertEquals("XXXX-XXXX-1234", masked)

    val hash1 = CryptoManager.hashAadhaar(rawAadhaar)
    val hash2 = CryptoManager.hashAadhaar("5432-8765-1234")
    assertEquals(hash1, hash2)
    assertNotEquals(rawAadhaar, hash1)
    assertTrue(hash1.length == 64)
  }

  @Test
  fun `verify mock jwt generation and claims`() {
    val jwt = CryptoManager.generateMockJwt("test@gov.in", "Ananya Sharma")
    val parts = jwt.split(".")
    assertEquals(3, parts.size)
  }

  @Test
  fun `verify incident structuring model`() = runBlocking {
    val sampleText = "I was being followed by a man on a grey scooter near metro station"
    val structured = GeminiIntelligenceService.analyzeAndStructureIncident(sampleText)
    assertEquals(IncidentCategory.STALKING, structured.category)
    assertTrue(structured.vehicleDetails.contains("Scooter"))
  }

  @Test
  fun `verify live route service computation`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val routeService = LiveLocationAndRouteService(context)
    val routes = routeService.computeLiveRoutes(null, "Connaught Place")
    assertTrue(routes.safestRoute.distanceKm > 0)
    assertTrue(routes.safestRoute.safetyScore >= 90)
    assertTrue(routes.waypoints.isNotEmpty())
  }

  @Test
  fun `verify nearest police stations calculation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val routeService = LiveLocationAndRouteService(context)
    val stations = routeService.nearestPoliceStations.value
    assertTrue("Should have nearest police stations", stations.isNotEmpty())
    val first = stations.first()
    assertTrue("Distance should be calculated", first.distanceKm >= 0.0)
    assertTrue("Should have active helpline", first.emergencyHelpline == "112")
    assertTrue("Should have SHO or in-charge assigned", first.stationInCharge.isNotBlank())
    // Verify sorted by distance
    for (i in 0 until stations.size - 1) {
      assertTrue("Should be sorted ascending by distance", stations[i].distanceMeters <= stations[i + 1].distanceMeters)
    }
  }

  @Test
  fun `verify new logo drawable exists`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val drawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_dhriti_user_logo)
    val roundDrawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_dhriti_user_logo_round)
    assertTrue("User logo drawable should load successfully", drawable != null)
    assertTrue("Round user logo drawable should load successfully", roundDrawable != null)
  }
}
