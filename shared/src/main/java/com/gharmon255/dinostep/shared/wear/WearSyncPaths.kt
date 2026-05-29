package com.gharmon255.dinostep.shared.wear

import android.net.Uri

object WearSyncPaths {
    /** Exact Data Layer path — must match on phone (put) and watch (listen). */
    const val CURRENT_CREATURE = "/dino_step/current_creature"

    fun matchesDataPath(path: String?): Boolean {
        if (path.isNullOrBlank()) {
            return false
        }
        val normalized = if (path.startsWith("/")) path else "/$path"
        if (normalized == CURRENT_CREATURE) {
            return true
        }
        return normalized.endsWith("dino_step/current_creature")
    }

    /** URI for querying synced items on watch (wear scheme, all nodes). */
    fun createDataItemsQueryUri(): Uri {
        return Uri.Builder()
            .scheme(WEAR_URI_SCHEME)
            .authority(WEAR_URI_AUTHORITY_ALL)
            .path(CURRENT_CREATURE)
            .build()
    }

    /** Prefix query to discover any dino_step data items on the watch. */
    fun createDataItemsPrefixUri(): Uri {
        return Uri.Builder()
            .scheme(WEAR_URI_SCHEME)
            .authority(WEAR_URI_AUTHORITY_ALL)
            .path("/dino_step")
            .build()
    }

    /** Broad query — all Data Layer items on all nodes (used on watch launch). */
    fun createAllDataItemsUri(): Uri {
        return Uri.Builder()
            .scheme(WEAR_URI_SCHEME)
            .authority(WEAR_URI_AUTHORITY_ALL)
            .build()
    }

    fun createDataItemUriForNode(nodeId: String): Uri {
        return Uri.Builder()
            .scheme(WEAR_URI_SCHEME)
            .authority(nodeId)
            .path(CURRENT_CREATURE)
            .build()
    }

    private const val WEAR_URI_SCHEME = "wear"
    private const val WEAR_URI_AUTHORITY_ALL = "*"
}
