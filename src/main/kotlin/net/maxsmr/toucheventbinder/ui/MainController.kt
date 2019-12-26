package net.maxsmr.toucheventbinder.ui

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.TextAlignment
import net.maxsmr.toucheventbinder.MainAppDelegate
import net.maxsmr.toucheventbinder.keyadapter.KeyBinder.Companion.resolveKeyByCode
import net.maxsmr.toucheventbinder.keyadapter.KeyBinder.KeyState
import net.maxsmr.toucheventbinder.keyadapter.KeyEventListener
import net.maxsmr.toucheventbinder.util.TranslationsUtil

class MainController {

    @FXML
    lateinit var rootNode: BorderPane

    @FXML
    lateinit var contentScrollPane: ScrollPane

    @FXML
    lateinit var buttonsInfoBox: VBox

    private val buttonsMap = mutableMapOf<Int, HBox>()

    // calling when FXMLLoader#load
    fun initialize() {
        contentScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED)

        MainAppDelegate.keyBinder.keyStateChangeListener = { keyCode, keyState ->
            Platform.runLater {
                refreshButtonStateBox(keyCode, keyState)
            }
        }
    }

    fun refreshButtonStateBox(keyCode: Int, keyState: KeyState) {
        var buttonStateBox = buttonsMap[keyCode]
        val isPresent = buttonStateBox != null
        var currentKeyButton: Button? = null
        var currentStateLabel: Label? = null
        if (!isPresent) {
            with(HBox()) {
                buttonStateBox = this
                this.alignment = Pos.CENTER_LEFT
                this.padding = Insets(0.0, 12.0, 0.0, 12.0)
                this.spacing = 10.0
//                this.style = "-fx-background-color: #FF000000;"
                Button().let {
                    currentKeyButton = it
                    it.id = "currentKeyButton"
                    it.styleClass.add("button")
                    it.isWrapText = true
                    it.textAlignment = TextAlignment.JUSTIFY
//                    it.setPrefSize(50.0, 20.0)
                }
                Label().let {
                    currentStateLabel = it
                    HBox.setMargin(it, Insets(0.0, 0.0, 0.0, 2.0))
                    it.id = "currentStateLabel"
                    it.styleClass.add("label")
//                    it.style = "-fx-wrap-text: true"
                    it.isWrapText = true
                    it.textAlignment = TextAlignment.JUSTIFY
                }
                children.addAll(currentKeyButton, currentStateLabel)
                VBox.setMargin(this, Insets(18.0, 0.0, 0.0, 0.0))
                buttonsMap[keyCode] = this
            }
        }
        buttonStateBox?.let { hbox ->
            if (currentKeyButton == null) {
                currentKeyButton = hbox.childrenUnmodifiable.find { it.id == "currentKeyButton" } as? Button
            }
            if (currentStateLabel == null) {
                currentStateLabel = hbox.childrenUnmodifiable.find { it.id == "currentStateLabel" } as? Label
            }
            currentKeyButton?.let { btn ->
                btn.text = TranslationsUtil.getInstance().formatString("key_code_text", resolveKeyByCode(keyCode)?.getName(), keyCode.toString())
            }
            currentStateLabel?.let { label ->
                label.text = keyState.toString()
            }
            if (!isPresent) {
                buttonsInfoBox.children.add(hbox)
            } else {
                hbox.requestLayout()
            }
            if (keyState.type == KeyEventListener.ExtendedKeyEventType.RELEASED) {
                buttonsInfoBox.children.remove(hbox)
                buttonsMap.remove(keyCode)
            }
        }
    }
}