package net.maxsmr.toucheventbinder.keyadapter

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import net.maxsmr.commonutils.data.gson.GsonHelper
import net.maxsmr.commonutils.logger.BaseLogger
import net.maxsmr.commonutils.logger.holder.BaseLoggerHolder
import net.maxsmr.toucheventbinder.config.KeyConfig
import net.maxsmr.toucheventbinder.keyadapter.KeyEventListener.ExtendedKeyEventType
import net.maxsmr.toucheventbinder.keyadapter.trigger.KeyActionTrigger
import net.maxsmr.toucheventbinder.util.FileUtils
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class KeyBinder(private val keyEventListener: KeyEventListener) : KeyEventListener.ExtendedEventTypeListener {

    private val logger: BaseLogger = BaseLoggerHolder.getInstance().getLogger(KeyBinder::class.java)

    private val executor = Executors.newSingleThreadExecutor()

    private val gson = GsonBuilder().setLenient().serializeNulls().create()

    private val statesMap = mutableMapOf<Int, KeyState>()

    var isStarted: Boolean = false
        set(value) {
            if (field != value
            /*|| value && !wasStartedOnce*/) {
                field = value
                if (value) {
                    start()
                } else {
                    stop()
                }
            }
        }

    var isReleased: Boolean = false
        private set

    var currentConfigFile: File? = null
        set(value) {
            if (field != value) {
                field = value
                if (value != null) {
                    readConfigFromFileAsync(value)
                } else {
                    config = mapOf()
                }
            } else {
                logger.i("No config file path change")
            }
        }

    var keyStateChangeListener: ((Int, KeyState) -> Unit)? = null

    var keyConfigChangeListener: ((Map<Int, KeyConfig>) -> Unit)? = null

    var triggerKeyExecutor: KeyActionTrigger? = null

//    private var wasStartedOnce: Boolean = false

    private var config: Map<Int, KeyConfig> = mapOf()
        set(value) {
            if (field != value) {
                field = value
                keyConfigChangeListener?.invoke(value)
            } else {
                logger.i("No config change")
            }
        }

    init {
        currentConfigFile = File("configs", "key_config.json")
        keyEventListener.eventListener = this
    }

    override fun getPressingIntervalForKey(keyEvent: KeyEvent): Long =
            synchronized(statesMap) {
                config[keyEvent.code.code]?.interval ?: 0
            }

    override fun onTriggerEvent(type: ExtendedKeyEventType, keyEvent: KeyEvent) {
        val code = keyEvent.code.code
        synchronized(code) {
            val previousState = statesMap[code]
            if ((previousState == null && type != ExtendedKeyEventType.PRESSING)
                    || (previousState != null && (previousState.type != type || type == ExtendedKeyEventType.PRESSING))) {
                logger.d("event triggered: type $type, key code $code")
                var shouldDoAction = false
                val keyConfig: KeyConfig? = config[code]
                keyConfig?.let {
                    if (it.keyEventType == type) {
                        shouldDoAction = true
                        triggerKeyExecutor?.let { trigger ->
//                            executor.execute {
//                                trigger.doActionWithKey(code, it)
//                            }
                            trigger.doActionWithKey(code, it)
                        }
                    }
                }
                val currentState = KeyState(type, shouldDoAction)
                if (currentState != previousState) {
                    logger.d("State of key with code $code changed from $previousState to $currentState")
                    statesMap[code] = currentState
                    keyStateChangeListener?.invoke(code, currentState)
                }
                if (type == ExtendedKeyEventType.RELEASED) {
                    logger.d("Removing key with code $code from map due to ${ExtendedKeyEventType.RELEASED} state")
                    statesMap.remove(code)
                }
            }
        }
    }

    fun toggleState() {
        isStarted = !isStarted
    }

    fun release() {
        if (isReleased) {
            throw IllegalStateException("KeyBinder is already released")
        }
        logger.i("disposing...")
        stop()
        executor.shutdown()
        triggerKeyExecutor?.release()
        isReleased = true
    }

    fun reloadConfigFromCurrentFile() {
        currentConfigFile?.let {
            readConfigFromFileAsync(it)
        }
    }

    private fun start() {
        checkReleased()
        logger.i("starting...")
        keyEventListener.isStarted = true
    }

    private fun stop() {
        checkReleased()
        logger.i("stopping...")
        keyEventListener.isStarted = false
    }

    private fun readConfigFromFileAsync(file: File) {
        checkReleased()
        executor.execute {
            readConfigFromFile(file)
        }
    }

    private fun readConfigFromFile(file: File) {
        readConfigFromJson(FileUtils.readStringFromFile(file) ?: "")
    }

    private fun readConfigFromJson(json: String) {
        val stringConfig = GsonHelper.fromJsonObjectString(gson, json, object : TypeToken<Map<String, KeyConfig>>() {})
        if (stringConfig != null) {
            val mutableMap = mutableMapOf<Int, KeyConfig>()
            stringConfig.entries.forEach { e ->
                val keyCode: Int? =
                        try {
                            e.key.toInt()
                        } catch (e: NumberFormatException) {
                            null
                        }
                keyCode?.let {
                    mutableMap[it] = e.value
                }
            }
            config = mutableMap
        } else {
            config = mapOf()
        }
        logger.i("Config read: $config")
    }

    private fun checkReleased() {
        if (isReleased) {
            throw IllegalStateException("KeyBinder is released")
        }
    }

    data class KeyState(
            val type: ExtendedKeyEventType,
            val isTriggeringAction: Boolean
    )

    companion object {

        fun resolveKeyByCode(code: Int): KeyCode? =
                KeyCode.values().find { it.code == code }
    }
}