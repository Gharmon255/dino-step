package com.gharmon255.dinostep.debug

import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.promo.PromoCatalog
import com.gharmon255.dinostep.promo.PromoRedemptionCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Locks down the on-device promo bookkeeping that makes codes one-time-use even when the player
 * never signs in (the local-first path). Encoding/decoding the redeemed set must be stable and
 * case-insensitive so a code can't be re-redeemed by changing capitalization or spacing.
 */
class PromoRedemptionTest {

    @Test
    fun catalogResolvesKnownCodesCaseAndWhitespaceInsensitively() {
        assertEquals(EggRarity.EPIC, PromoCatalog.rewardFor("epic20"))
        assertEquals(EggRarity.EPIC, PromoCatalog.rewardFor("  EPIC20 "))
        assertEquals(EggRarity.LEGENDARY, PromoCatalog.rewardFor("Legend20"))
        assertNull(PromoCatalog.rewardFor("not-a-code"))
        assertNull(PromoCatalog.rewardFor(""))
    }

    @Test
    fun knownCodesContainsCurrentPromos() {
        assertTrue(PromoCatalog.knownCodes().containsAll(setOf("epic20", "legend20")))
    }

    @Test
    fun emptyStorageParsesToEmptySet() {
        assertTrue(PromoRedemptionCodec.parse(null).isEmpty())
        assertTrue(PromoRedemptionCodec.parse("").isEmpty())
        assertTrue(PromoRedemptionCodec.parse("   ").isEmpty())
    }

    @Test
    fun markRedeemedIsIdempotentAndNormalizes() {
        var stored: String? = null
        stored = PromoRedemptionCodec.markRedeemed(stored, "EPIC20")
        assertTrue(PromoRedemptionCodec.hasRedeemed(stored, "epic20"))

        // Redeeming the same code again (different casing) doesn't create a duplicate entry.
        val afterSecond = PromoRedemptionCodec.markRedeemed(stored, "  epic20 ")
        assertEquals(setOf("epic20"), PromoRedemptionCodec.parse(afterSecond))
    }

    @Test
    fun multipleCodesEncodeSortedAndRoundTrip() {
        var stored: String? = null
        stored = PromoRedemptionCodec.markRedeemed(stored, "legend20")
        stored = PromoRedemptionCodec.markRedeemed(stored, "epic20")

        assertEquals("epic20,legend20", stored)
        assertEquals(setOf("epic20", "legend20"), PromoRedemptionCodec.parse(stored))
        assertTrue(PromoRedemptionCodec.hasRedeemed(stored, "EPIC20"))
        assertTrue(PromoRedemptionCodec.hasRedeemed(stored, "legend20"))
        assertFalse(PromoRedemptionCodec.hasRedeemed(stored, "mystery99"))
    }

    @Test
    fun encodeOfEmptySetIsNullSoWeDoNotStoreBlankStrings() {
        assertNull(PromoRedemptionCodec.encode(emptySet()))
    }

    @Test
    fun parseDedupesAndTrimsMessyStoredValues() {
        assertEquals(
            setOf("epic20", "legend20"),
            PromoRedemptionCodec.parse(" EPIC20 , legend20 ,epic20,"),
        )
    }
}
