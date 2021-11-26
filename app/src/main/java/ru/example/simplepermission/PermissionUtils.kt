package ru.example.simplepermission

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import ru.example.simplepermission.PermissionStatus.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun Fragment.requestPermissionLauncher(listener: (PermissionStatus) -> Unit
): ReadOnlyProperty<Fragment, ActivityResultLauncher<Array<String>>> =
    PermissionResultDelegate(this, listener)

class PermissionResultDelegate(
    private val fragment: Fragment, listener: (PermissionStatus) -> Unit
) :
    ReadOnlyProperty<Fragment, ActivityResultLauncher<Array<String>>> {

    private var permissionResult: ActivityResultLauncher<Array<String>>? = null

    init {
        fragment.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_CREATE) {
                    fragment.apply {
                        permissionResult = registerForActivityResult(
                            ActivityResultContracts.RequestMultiplePermissions()
                        ) { permissions -> checkPermissions(permissions) }
                    }
                } else if (event == Lifecycle.Event.ON_DESTROY) {
                    fragment.lifecycle.removeObserver(this)
                    permissionResult = null
                }
            }

            private fun Fragment.checkPermissions(permissions: MutableMap<String, Boolean>) {
                val statuses = permissions.entries.map {(name, isGranted) ->
                    when {
                        isGranted -> Granted
                        shouldShowRequestPermissionRationale(name) -> ShowRationale
                        else -> Denied
                    }
                }

                when {
                    //if we have all permissions granted, that's ok,
                    statuses.all { it is Granted } -> listener(Granted)
                    //if we have ANY permission Denied, need to show Denied message
                    statuses.any { it is Denied } -> listener(Denied)
                    //if we have ANY permission Rationale, need to show rationale message
                    statuses.any { it is ShowRationale } -> listener(ShowRationale)
                    else -> listener(Denied)
                }
            }

        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): ActivityResultLauncher<Array<String>> {
        permissionResult?.let { return it }

        error("Failed to Initialize Permission")
    }
}

sealed class PermissionStatus {
    object Granted : PermissionStatus()

    object Denied : PermissionStatus()

    object ShowRationale : PermissionStatus()
}