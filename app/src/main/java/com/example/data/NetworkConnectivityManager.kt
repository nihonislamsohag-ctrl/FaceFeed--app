package com.example.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NetworkTransportType {
    WIFI,
    CELLULAR,
    OTHER,
    NONE
}

/**
 * Real-time Android ConnectivityManager monitor for FotoFilm.
 * Tracks Wi-Fi & Mobile Data (Cellular, including 6-SIM Free Mode) state so the app strictly gates
 * all photos, videos, reels, stories, live streams, and posts when offline —
 * matching Facebook's real internet requirement, while unlocking reliably whenever Data or Wi-Fi is ON.
 */
object NetworkConnectivityManager {

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _transportType = MutableStateFlow(NetworkTransportType.NONE)
    val transportType: StateFlow<NetworkTransportType> = _transportType.asStateFlow()

    private var isInitialized = false
    private var appContext: Context? = null
    private var defaultCallback: ConnectivityManager.NetworkCallback? = null
    private var internetRequestCallback: ConnectivityManager.NetworkCallback? = null

    fun initialize(context: Context) {
        val ctx = context.applicationContext
        appContext = ctx
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (cm == null) {
            _isConnected.value = false
            _transportType.value = NetworkTransportType.NONE
            return
        }

        evaluateNetworkState(cm)

        if (isInitialized) return
        isInitialized = true

        val cb1 = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                evaluateNetworkState(cm)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                evaluateNetworkState(cm)
            }

            override fun onLost(network: Network) {
                evaluateNetworkState(cm)
            }

            override fun onUnavailable() {
                evaluateNetworkState(cm)
            }
        }
        defaultCallback = cb1

        try {
            cm.registerDefaultNetworkCallback(cb1)
        } catch (_: Exception) {
            evaluateNetworkState(cm)
        }

        // Also observe any network with NET_CAPABILITY_INTERNET (catches 6-SIM zero-balance Free Mode
        // cellular connections before or without OS captive/validation promotion to activeNetwork).
        val cb2 = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                evaluateNetworkState(cm)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                evaluateNetworkState(cm)
            }

            override fun onLost(network: Network) {
                evaluateNetworkState(cm)
            }
        }
        internetRequestCallback = cb2

        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(request, cb2)
        } catch (_: Exception) {
            evaluateNetworkState(cm)
        }
    }

    fun refreshNow(context: Context? = appContext): Boolean {
        val ctx = context?.applicationContext ?: appContext ?: return _isConnected.value
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        return evaluateNetworkState(cm)
    }

    private fun extractUsableTransport(caps: NetworkCapabilities?): NetworkTransportType? {
        if (caps == null) return null
        val hasInternetCap = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (!hasInternetCap) return null
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkTransportType.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkTransportType.CELLULAR
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> NetworkTransportType.OTHER
            else -> null
        }
    }

    private fun evaluateNetworkState(cm: ConnectivityManager): Boolean {
        val activeCaps = try {
            cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
        } catch (_: Exception) {
            null
        }
        var detectedTransport = extractUsableTransport(activeCaps)

        if (detectedTransport == null) {
            @Suppress("DEPRECATION")
            val allNets = try {
                cm.allNetworks
            } catch (_: Exception) {
                emptyArray()
            }
            for (net in allNets) {
                val caps = try {
                    cm.getNetworkCapabilities(net)
                } catch (_: Exception) {
                    null
                }
                val candidate = extractUsableTransport(caps)
                if (candidate != null) {
                    detectedTransport = candidate
                    break
                }
            }
        }

        val connected = detectedTransport != null
        _isConnected.value = connected
        _transportType.value = detectedTransport ?: NetworkTransportType.NONE
        return connected
    }
}
