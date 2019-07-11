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
    private val defaultWhiteTabStyle = defaultTabStyle.plus(" -fx-text-base-color: white;")
    private val defaultBlackTabStyle = defaultTabStyle.plus(" -fx-text-base-color: Black;")

    private fun generateAnalyzeTab (tabPane: TabPane, filePaths: Array<Path>): Tab {
        val tab = Tab()
        tabCount += 1
        tab.text = "Tab$tabCount"
        val fxml = javaClass.getResource("fxml/NestTab.fxml")
        val aTabSpace: Pane = FXMLLoader.load(fxml)
        val filePathArea = aTabSpace.lookup("#FilePaths") as TextArea // FilePaths TextArea
        val messageBox = aTabSpace.lookup("#MessageBox") as HBox
        val resultArea = aTabSpace.lookup("#ResultArea") as TextArea
        val showIgnrBox = aTabSpace.lookup("#ShowIgnored") as CheckBox
        val showExedBox = aTabSpace.lookup("#ShowExtracted") as CheckBox
        val showDirBox = aTabSpace.lookup("#ShowDirectory") as CheckBox

        messageBox.border = Border(BorderStroke(Paint.valueOf("Red"),BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT))
        resultArea.border = Border(BorderStroke(Paint.valueOf("Green"),BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT))
        tab.content = aTabSpace

        filePathArea.text = filePaths.joinToString(separator = "\n")
        filePathArea.font = Font.font(null,FontWeight.NORMAL,14.0)

        aTabSpace.onDragOver = EventHandler { event ->
            val db = event.dragboard
            if (db.hasFiles())
                event.acceptTransferModes(TransferMode.COPY)
            event.consume()
        }
        aTabSpace.onDragDropped = EventHandler { event ->
            val db = event.dragboard
            val newFilePaths = db.files.map{it.toString()}.sorted()
            val newAnalyzeTab = generateAnalyzeTab(tabPane, newFilePaths.toTypedArray())
            event.consume()

            tabPane.tabs.add(newAnalyzeTab)
            tabPane.selectionModel.select(newAnalyzeTab)
            event.isDropCompleted = true
        }

        // Step: Check archive existence
        var rASV: Pair<MessageType, String>
        rASV = checkArchiveExistence(filePaths)
        addMessageLabel(messageBox, rASV.first, rASV.second)
        if (rASV.first != MessageType.NoProblem) {
            tab.text =  "No Archive"
            tab.style = defaultBlackTabStyle.plus("-fx-background-color: yellow")
            aTabSpace.style = "-fx-background-color: yellow"
            return tab
        }

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
            for ( filePath in filePaths ) {

                if (!filePath.isSingleVolume() && !filePath.isFirstVolume()) {
                    println(filePath)
                    continue
                }
                // Step 2: Select ItemID of extracting file
                Platform.runLater {
                    tab.text = "Open an Archive"
                }
                println("Open an Archive $filePath")
                val anANS = openArchive(filePath)
                if (anANS == null) {
                    Platform.runLater {
                        tab.text = "Fail to get ANS"
                    }
                } else {
                    println("Get ANS of $filePath")
                    val anArchive = Archive(anANS,filePath)

                    Platform.runLater {
                        tab.text = "Extracting"
                    }
                    // Step 3: Extract each items by ItemID
                    println("Extract $filePath")
                    rASV = anArchive.extractAll()
                    Platform.runLater {
                        addMessageLabel(messageBox, rASV.first, if (rASV.first == MessageType.NoProblem) "" else rASV.second)
                    }
                    if (rASV.first == MessageType.Critical) {
                        Platform.runLater {
                            tab.text = "Fail to extract every files"
                            tab.style = defaultBlackTabStyle.plus("-fx-background-color: red")
                            aTabSpace.style = "-fx-background-color: red"
                        }
                    } else if (rASV.first == MessageType.Bad) {
                        Platform.runLater {
                            tab.text = "Contains non-archive File"
                            tab.style = defaultBlackTabStyle.plus("-fx-background-color: red")
                            aTabSpace.style = "-fx-background-color: red"
                        }
                    } else {
                        println("Rename $filePath")
                        rASV = anArchive.renameAll()
                        Platform.runLater {
                            addMessageLabel(messageBox, rASV.first, if (rASV.first == MessageType.NoProblem) "" else rASV.second)
                        }
                        Platform.runLater {
                            if (rASV.first == MessageType.Critical) {
                                tab.text = "Fail to rename"
                                tab.style = defaultBlackTabStyle.plus("-fx-background-color: red")
                                aTabSpace.style = "-fx-background-color: red"
                            } else {
                                tab.text = "Success"
                                tab.style = defaultBlackTabStyle.plus("-fx-background-color: green")
                                aTabSpace.style = "-fx-background-color: green"
                            }
                        }
                        println("Close $filePath")
                        anArchive.closeArchive()
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
        val extractDropPoint= root.lookup("#ForExtract") as Rectangle // Extracting drop point
        val renameDropPoint = root.lookup("#ForRename") as Rectangle // Renaming drop point
        val closeAllButton = root.lookup("#CloseAllButton") as Button

        tabPane.tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS // or SELECTED_TAB, UNAVAILABLE

        extractDropPoint.heightProperty().bind(epPane.heightProperty().divide(32).multiply(19))

        renameDropPoint.yProperty().bind(extractDropPoint.yProperty().add(dropPane.heightProperty().divide(2)))
        renameDropPoint.heightProperty().bind(epPane.heightProperty().divide(8).multiply(3))

        primaryStage.scene = scene
        primaryStage.show()

        val extractDrop = extractDropPoint.fill
        val renameDrop = renameDropPoint.fill
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
            val newAnalyzeTab = generateAnalyzeTab(tabPane, sortedFilePaths.toTypedArray())
            event.consume()

            tabPane.tabs.add(newAnalyzeTab)
            tabPane.selectionModel.select(newAnalyzeTab)
            event.isDropCompleted = true
        }

        extractDropPoint.onDragEntered = EventHandler { event ->
            extractDropPoint.fill = selectedColor
            event.consume()
        }
        extractDropPoint.onDragOver = EventHandler { event ->
            val db = event.dragboard
            if (db.hasFiles())
                event.acceptTransferModes(TransferMode.COPY)
            event.consume()
        }
        extractDropPoint.onDragDropped = EventHandler { event ->
            val db = event.dragboard
            val sortedFilePaths = db.files.map{it.toString()}.sorted()
            val newAnalyzeTab = generateAnalyzeTab(tabPane, sortedFilePaths.toTypedArray())
            event.consume()

            tabPane.tabs.add(newAnalyzeTab)
            tabPane.selectionModel.select(newAnalyzeTab)
            event.isDropCompleted = true
        }
        extractDropPoint.onDragExited = EventHandler { event ->
            extractDropPoint.fill = extractDrop
            event.consume()
        }

        renameDropPoint.onDragEntered = EventHandler { event ->
            renameDropPoint.fill = selectedColor
            event.consume()
        }
        renameDropPoint.onDragOver = EventHandler { event ->
            val db = event.dragboard
            if (db.hasFiles())
                event.acceptTransferModes(TransferMode.COPY)
            event.consume()
        }
        renameDropPoint.onDragDropped = EventHandler { event ->
            val db = event.dragboard
            val archivePaths = db.files.map{it.toString()}
            event.consume()
            // TODO: Call Renamer
        }
        renameDropPoint.onDragExited = EventHandler { event ->
            renameDropPoint.fill = renameDrop
            event.consume()
        }

        closeAllButton.setOnAction {
            tabPane.tabs.last().style = ""
            tabPane.tabs.clear()
        }
    }
}
