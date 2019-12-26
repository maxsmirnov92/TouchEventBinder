package net.maxsmr.toucheventbinder.keyadapter

import com.google.gson.annotations.SerializedName
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.Scene
import javafx.scene.input.KeyEvent
import net.maxsmr.commonutils.logger.BaseLogger
import net.maxsmr.commonutils.logger.holder.BaseLoggerHolder
import net.maxsmr.tasksutils.ScheduledThreadPoolExecutorManager
import net.maxsmr.tasksutils.ScheduledThreadPoolExecutorManager.ScheduleMode.*
import net.maxsmr.tasksutils.runnable.RunnableInfoRunnable
import net.maxsmr.tasksutils.storage.ids.IdHolder
import net.maxsmr.tasksutils.taskexecutor.RunnableInfo
import net.maxsmr.toucheventbinder.util.RunLaterExceptionHandler

class KeyEventListener {

    private val logger: BaseLogger = BaseLoggerHolder.getInstance().getLogger(KeyEventListener::class.java)

    private val registeredHandlers = mutableMapOf<EventType<KeyEvent>, TypeEventHandler>()

    private val executor = ScheduledThreadPoolExecutorManager("key_pressing_notifier", RunLaterExceptionHandler())

    private val pressingTasks = mutableSetOf<PressingRunnableTask>()

//    private val pressingTasksIdHolder = IdHolder(1)

    lateinit var scene: Scene

    var synchronousPressedKeysCount = 10
        set(value) {
            if (value != field) {
                field = value
                if (executor.isRunning) {
                    executor.restart(value)
                }
            }
        }

    var eventListener: ExtendedEventTypeListener? = null

    var eventTypesToIgnore = setOf<ExtendedKeyEventType>()

    var isStarted: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (value) {
                    startListening()
                } else {
                    stopListening()
                }
            }
        }

    private fun startListening() {
        logger.i("starting listening...")
        val pressedHandler = TypeEventHandler(KeyEvent.KEY_PRESSED)
        val releasedHandler = TypeEventHandler(KeyEvent.KEY_RELEASED)
        scene.addEventHandler(KeyEvent.KEY_PRESSED, pressedHandler)
        scene.addEventHandler(KeyEvent.KEY_RELEASED, releasedHandler)
        registeredHandlers[KeyEvent.KEY_PRESSED] = pressedHandler
        registeredHandlers[KeyEvent.KEY_RELEASED] = releasedHandler
        executor.start(synchronousPressedKeysCount)
    }

    private fun stopListening() {
        logger.i("stopping listening...")
        registeredHandlers.forEach {
            scene.removeEventHandler(it.key, it.value)
        }
        executor.stop()
    }

    private fun findPressingTaskByCode(keyEvent: KeyEvent): PressingRunnableTask? =
            pressingTasks.find {
                it.keyEvent.code == keyEvent.code
            }

    private fun removePressingTaskByCode(keyEvent: KeyEvent): Boolean =
            pressingTasks.removeIf { it.keyEvent.code == keyEvent.code }


    enum class ExtendedKeyEventType {
        @SerializedName("PRESSED")
        PRESSED,
        @SerializedName("PRESSING")
        PRESSING,
        @SerializedName("RELEASED")
        RELEASED;

        companion object {

            fun resolve(type: EventType<KeyEvent>): ExtendedKeyEventType? {
                return when (type) {
                    KeyEvent.KEY_PRESSED -> PRESSED
                    KeyEvent.KEY_RELEASED -> RELEASED
                    else -> null
                }
            }
        }
    }

    interface ExtendedEventTypeListener {

        /**
         * @return >0 if between pressed and released states
         * needed to trigger events with this interval
         */
        fun getPressingIntervalForKey(keyEvent: KeyEvent): Long

        fun onTriggerEvent(type: ExtendedKeyEventType, keyEvent: KeyEvent)
    }

    inner class TypeEventHandler(val type: EventType<KeyEvent>) : EventHandler<KeyEvent> {

        override fun handle(keyEvent: KeyEvent) {
            eventListener?.let { listener ->
                val extendedEventType = ExtendedKeyEventType.resolve(type)
                val pressingTask = findPressingTaskByCode(keyEvent)
                if (pressingTask != null && extendedEventType == ExtendedKeyEventType.PRESSED) {
                    return
                }
                if (eventTypesToIgnore.contains(extendedEventType)) {
                    logger.w("Event type $extendedEventType is in ignore list!")
                    return
                }
                extendedEventType?.let {
                    listener.onTriggerEvent(it, keyEvent)
                }
                when (type) {
                    KeyEvent.KEY_PRESSED -> {
                        if (pressingTask == null) {
                            val interval = listener.getPressingIntervalForKey(keyEvent)
                            if (interval > 0) {
                                val task = PressingRunnableTask(keyEvent, RunnableInfo(keyEvent.code.code))
                                executor.addRunnableTask(task, ScheduledThreadPoolExecutorManager.RunOptions(0, interval, FIXED_DELAY))
                                pressingTasks.add(task)
                            } else {
                                // do nothing
                            }
                        } else {
                            // do nothing
                        }
                    }
                    KeyEvent.KEY_RELEASED -> {
                        pressingTask?.let {
                            executor.removeRunnableTask(it)
                            removePressingTaskByCode(keyEvent)
                        }
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
        }
    }

    private inner class PressingRunnableTask(
            val keyEvent: KeyEvent,
            rInfo: RunnableInfo
    ) : RunnableInfoRunnable<RunnableInfo>(rInfo) {

        override fun run() {
            eventListener?.onTriggerEvent(ExtendedKeyEventType.PRESSING, keyEvent)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PressingRunnableTask

            if (keyEvent.code != other.keyEvent.code) return false

            return true
        }

        override fun hashCode(): Int {
            return keyEvent.code.hashCode()
        }

        override fun toString(): String {
            return "PressingRunnableTask(keyEvent=$keyEvent)"
        }
    }
}