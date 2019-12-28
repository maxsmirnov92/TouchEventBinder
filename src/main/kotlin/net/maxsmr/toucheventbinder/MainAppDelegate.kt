package net.maxsmr.toucheventbinder

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.input.KeyCombination
import javafx.scene.layout.BorderPane
import javafx.stage.FileChooser
import javafx.stage.Stage
import net.maxsmr.commonutils.logger.BaseLogger
import net.maxsmr.commonutils.logger.holder.BaseLoggerHolder
import net.maxsmr.toucheventbinder.keyadapter.trigger.AdbKeyActionTrigger
import net.maxsmr.toucheventbinder.keyadapter.KeyBinder
import net.maxsmr.toucheventbinder.keyadapter.KeyEventListener
import net.maxsmr.toucheventbinder.util.FileUtils
import net.maxsmr.toucheventbinder.util.TranslationsUtil

object MainAppDelegate {

    private val logger = BaseLoggerHolder.getInstance().getLogger<BaseLogger>(MainAppDelegate::class.java)

    lateinit var keyBinder: KeyBinder
    
    fun start(stage: Stage) {
        logger.i("Starting Hello JavaFX and Maven demonstration application")
        stage.onCloseRequest = EventHandler {
            close()
        }
        val fxmlFile = "/fxml/hello.fxml"
        logger.d("Loading FXML for main view from: $fxmlFile");
        val loader = FXMLLoader()
        val scene: Scene
        val listener = KeyEventListener()
        keyBinder = KeyBinder(listener)
        with(loader.load<Parent>(javaClass.getResourceAsStream(fxmlFile))) {
            requestLayout()
            logger.d("Showing JFX scene")
            scene = Scene(this, 600.0, 400.0)
            scene.stylesheets.add("/styles/styles.css")
            stage.title = TranslationsUtil.getInstance().formatString("window_title_version_format", BuildConfig.VERSION_NAME)
            stage.scene = scene
            stage.show()
            stage.isResizable = true
            stage.sizeToScene()
            listener.scene = scene
            keyBinder.triggerKeyExecutor = AdbKeyActionTrigger
            keyBinder.isStarted = true
            if (this is BorderPane) {
                setupMenu(this, stage)
            }
        }
    }
    
    fun stop() {
        logger.i("Stopping Hello JavaFX and Maven demonstration application")
        keyBinder.isStarted = false
    }

    private fun setupMenu(rootNode: BorderPane, stage: Stage) {
        val menuBar = MenuBar()

        val fileMenu = Menu( TranslationsUtil.getInstance().getString("menu_file_text"))
        val helpMenu = Menu( TranslationsUtil.getInstance().getString("menu_help_text"))
        val startStopMenuItem = MenuItem()
        val openConfigMenuItem = MenuItem(TranslationsUtil.getInstance().getString("menu_item_open_config_text"))
        val reloadConfigMenuItem = MenuItem(TranslationsUtil.getInstance().getString("menu_item_reload_config_text"))
        val quitMenuItem = MenuItem(TranslationsUtil.getInstance().getString("menu_item_quit_text"))
        val aboutMenuItem = MenuItem(TranslationsUtil.getInstance().getString("menu_item_about_text"))

        fun refreshStartStopMenu() {
            startStopMenuItem.text = TranslationsUtil.getInstance().getString(if (keyBinder.isStarted) "menu_item_stop_text" else "menu_item_start_text")
        }

        startStopMenuItem.setOnAction {
            keyBinder.toggleState()
            refreshStartStopMenu()
        }
        refreshStartStopMenu()

        openConfigMenuItem.setOnAction {
            val fileChooser = FileChooser()
            fileChooser.title = "Open key binder config File"
            keyBinder.currentConfigFile?.let { currentFile ->
                if (FileUtils.isFileExists(currentFile)) {
                    fileChooser.initialDirectory = currentFile.parentFile
                    fileChooser.initialFileName = currentFile.name
                }
            }
            fileChooser.initialDirectory
            fileChooser.extensionFilters.addAll(
                    FileChooser.ExtensionFilter("Json Files", "*.json")
            )
            val selectedFile = fileChooser.showOpenDialog(stage)
            if (selectedFile != null) {
                keyBinder.currentConfigFile = selectedFile
            }
        }
        reloadConfigMenuItem.setOnAction {
            keyBinder.reloadConfigFromCurrentFile()
        }
        quitMenuItem.accelerator = KeyCombination.keyCombination("Ctrl+X")
        quitMenuItem.setOnAction {
            close()
        }

//        aboutMenuItem.accelerator = KeyCombination.keyCombination("F1")
        aboutMenuItem.setOnAction {
            showAboutAlertDialog()
        }

        fileMenu.items.addAll(startStopMenuItem, openConfigMenuItem, reloadConfigMenuItem, quitMenuItem)
        helpMenu.items.add(aboutMenuItem)
        menuBar.menus.addAll(fileMenu, helpMenu)
        rootNode.top = menuBar
    }

    private fun showAboutAlertDialog() {
        val alert = Alert(AlertType.INFORMATION)
        alert.title = TranslationsUtil.getInstance().getString("dialog_about_title")
        alert.headerText =  TranslationsUtil.getInstance().formatString("window_title_version_format", BuildConfig.VERSION_NAME)
        alert.contentText = TranslationsUtil.getInstance().getString("dialog_about_message")
        alert.showAndWait()
    }

    private fun close() {
        keyBinder.release()
        Platform.exit()
        System.exit(0)
    }
}