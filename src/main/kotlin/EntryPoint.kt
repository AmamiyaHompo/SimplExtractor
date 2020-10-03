import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.*
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import javafx.scene.shape.*
import javafx.scene.text.*
import javafx.stage.*

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import archive.*
import util.*


class EntryPoint : Application() {

    private var tabCount = 0
    private val defaultMessageLabelStyle = "-fx-stroke: white; -fx-padding: 6 6 6 6; -fx-font-size: 16px; -fx-font-weight: bold;"
    private val defaultWhiteMessageLabelStyle = defaultMessageLabelStyle.plus(" -fx-text-fill: white;")
    private val defaultBlackMessageLabelStyle = defaultMessageLabelStyle.plus(" -fx-text-fill: Black;")
    private val defaultTabStyle = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-focus-color: yellow; -fx-faint-focus-color: transparent;"
    private val defaultBlackTabStyle = defaultTabStyle.plus(" -fx-text-base-color: Black;")

    private fun generateResultTab (tabPane: TabPane, filePaths: Array<Path>): Tab {
        val tab = Tab()
        tabCount += 1
        tab.text = "Tab$tabCount"
        val fxml = javaClass.getResource("fxml/NestTab.fxml")
        val aTabSpace: Pane = FXMLLoader.load(fxml)
        val filePathArea = aTabSpace.lookup("#FilePaths") as TextArea // FilePaths TextArea
        val messageBox = aTabSpace.lookup("#MessageBox") as HBox
        val resultArea = aTabSpace.lookup("#ResultArea") as TextArea

        messageBox.border = Border(BorderStroke(Paint.valueOf("Red"),BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT))
        resultArea.border = Border(BorderStroke(Paint.valueOf("Green"),BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT))
        tab.content = aTabSpace

        filePathArea.text = filePaths.joinToString(separator = "\n")
        filePathArea.font = Font.font(null,FontWeight.NORMAL,14.0)

        aTabSpace.onDragDropped = EventHandler { event ->
            val db = event.dragboard
            val newFilePaths = db.files.map{it.toString()}.sorted()
            val newResultTab = generateResultTab(tabPane, newFilePaths.toTypedArray())
            event.consume()

            tabPane.tabs.add(newResultTab)
            tabPane.selectionModel.select(newResultTab)
            event.isDropCompleted = true
        }
        aTabSpace.onDragOver = EventHandler { event ->
            val db = event.dragboard
            if (db.hasFiles())
                event.acceptTransferModes(TransferMode.COPY)
            event.consume()
        }

        // Step: Check archive existence
        var rASV: Message
        rASV = checkArchiveVolume(filePaths)
        addMessageLabel(messageBox, rASV.first, rASV.second)
        when(rASV.first) {
            MessageType.Critical -> {
                tab.text = "No Archive"
                tab.style = defaultBlackTabStyle.plus("-fx-background-color: Red")
                aTabSpace.style = "-fx-background-color: LightCoral"
                return tab
            }
            MessageType.Warning -> {
                tab.text = "Too much Archive"
                tab.style = defaultBlackTabStyle.plus("-fx-background-color: LightSkyBlue")
                aTabSpace.style = "-fx-background-color: CornflowerBlue"
            }
            else -> {}
        }

        tab.text = "Preparing a task"
        tab.style = defaultBlackTabStyle

        val task = GlobalScope.launch {
            archLoop@for ( filePath in filePaths ) {

                // Step 1: Skip non-head volume
                if (!filePath.isSingleVolume() && !filePath.isFirstVolume()) continue

                // Step 2: Select ItemID of extracting file
                Platform.runLater {
                    tab.text = "Open an Archive"
                }
                println("Open an Archive $filePath")
                val anANS = openArchive(filePath)
                if (anANS == null) {
                    Platform.runLater {
                        tab.text = "Fail to get ANS"
                        addMessageLabel(messageBox, MessageType.Bad, "Fail to open archive\n$filePath")
                        filePathArea.text = filePathArea.text + "\nFail to open archive $filePath"
                    }
                    break@archLoop
                } else {
                    println("Get ANS of $filePath")
                    val anArchive = Archive(anANS,filePath)

                    Platform.runLater {
                        tab.text = "Extracting"
                        addMessageLabel(messageBox, MessageType.NoProblem, "Extracting")
                        filePathArea.text = filePathArea.text + "\nExtracting $filePath"
                    }
                    // Step 3: Extract each items by ItemID
                    println("Extract $filePath")
                    rASV = anArchive.extractAll()
                    when (rASV.first) {
                        MessageType.Critical -> {
                            Platform.runLater {
                                tab.text = "Fail to extract every files"
                                tab.style = defaultBlackTabStyle.plus("-fx-background-color: red")
                                aTabSpace.style = "-fx-background-color: red"
                                addMessageLabel(messageBox, MessageType.Critical, "Fail to extract\nevery files")
                                filePathArea.text = filePathArea.text + "\n" + rASV.second
                            }
                            println("Close $filePath")
                            anArchive.closeArchive()
                            break@archLoop
                        }
                        MessageType.NoProblem -> {
                            println("Rename $filePath")
                            Platform.runLater {
                                addMessageLabel(messageBox, rASV.first, "")
                                if (rASV.second.isEmpty())
                                    filePathArea.text = filePathArea.text + "\n" + rASV.second + " with $filePath"
                            }

                            rASV = anArchive.renameAll()
                            when (rASV.first) {
                                MessageType.Critical -> {
                                    Platform.runLater {
                                        tab.text = "Fail to rename"
                                        tab.style = defaultBlackTabStyle.plus("-fx-background-color: red")
                                        aTabSpace.style = "-fx-background-color: red"
                                        addMessageLabel(messageBox, rASV.first, "Fail to\nRename")
                                        filePathArea.text = filePathArea.text + "\n" + rASV.second
                                    }
                                    println("Close $filePath")
                                    anArchive.closeArchive()
                                    break@archLoop
                                }
                                MessageType.NoProblem -> {
                                    Platform.runLater {
                                        tab.text = "Success"
                                        tab.style = defaultBlackTabStyle.plus("-fx-background-color: green")
                                        aTabSpace.style = "-fx-background-color: green"
                                        addMessageLabel(messageBox, rASV.first, rASV.second)
                                        filePathArea.text = filePathArea.text + "\n" + rASV.second + " with $filePath"
                                    }
                                }
                            }
                            println("Close $filePath")
                            anArchive.closeArchive()
                        }
                    }
                }
            }
            println("End a task")
        }

        return tab
    }

    private fun addMessageLabel (mb: HBox, mt: MessageType, msg: String) {
        val messageLabel = Label(msg)
        messageLabel.style = when(mt) {
            MessageType.Critical -> defaultWhiteMessageLabelStyle.plus("-fx-background-color: red;")
            MessageType.Bad -> defaultBlackMessageLabelStyle.plus("-fx-background-color: yellow;")
            MessageType.Warning -> defaultWhiteMessageLabelStyle.plus("-fx-background-color: blue")
            MessageType.NoProblem -> defaultWhiteMessageLabelStyle.plus("-fx-background-color: green")
        }
        mb.children.add(0, messageLabel)
    }

    override fun start(primaryStage: Stage) {
        primaryStage.title = "EntryPoint"
        primaryStage.isAlwaysOnTop = true
        val fxml = javaClass.getResource("fxml/EntryPoint.fxml")
        val root: Parent = FXMLLoader.load(fxml)
        val scene = Scene(root)

        val epPane= root.lookup("#EPPane") as AnchorPane // Entry Point Pane
        val dropPane= root.lookup("#DropPane") as AnchorPane // Drop Pane
        val tabPane= root.lookup("#TabPane") as TabPane // Tab Pane
        val closeSameOnlyButton = root.lookup("#CloseSameOnlyButton") as Button
        val closeAllButton = root.lookup("#CloseAllButton") as Button

        tabPane.tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS // or SELECTED_TAB, UNAVAILABLE

        primaryStage.scene = scene
        primaryStage.show()

        val selectedColor = Paint.valueOf("Green")

        tabPane.onDragOver = EventHandler { event ->
            val db = event.dragboard
            if (db.hasFiles())
                event.acceptTransferModes(TransferMode.COPY)
            event.consume()
        }
        tabPane.onDragDropped = EventHandler { event ->
            val db = event.dragboard
            val sortedFilePaths = db.files.map{it.toString()}.sorted()
            val newResultTab = generateResultTab(tabPane, sortedFilePaths.toTypedArray())
            event.consume()

            tabPane.tabs.add(newResultTab)
            tabPane.selectionModel.select(newResultTab)
            event.isDropCompleted = true
        }

        closeAllButton.setOnAction {
            tabPane.tabs.last().style = ""
            tabPane.tabs.clear()
        }

        closeSameOnlyButton.setOnAction {
            val tabList = mutableListOf<Tab>()
            tabPane.tabs.forEach { if (it.style.endsWith("green;")) tabList.add(it) }
            tabList.reverse()
            for (aTab in tabList) {
                tabPane.tabs.remove(aTab)
            }
        }
    }
}
