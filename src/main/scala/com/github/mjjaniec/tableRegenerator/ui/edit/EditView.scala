package com.github.mjjaniec.tableRegenerator.ui.edit

import com.github.mjjaniec.tableRegenerator.logic.{TableData, TableDrawer}
import com.github.mjjaniec.tableRegenerator.ui.main.MainView
import com.vaadin.ui._
import com.vaadin.ui.themes.ValoTheme

import scala.collection.mutable

class EditView(mainView: MainView, data: TableData) extends VerticalLayout {

  private val lengths = TableDrawer.computeLengts(data)
  private val rowsLayout = new VerticalLayout()
  private val rows: mutable.Buffer[RowView] = mutable.Buffer.empty[RowView]

  private val deleteAction: RowView => Unit = r => {
    rows.remove(rows.indexOf(r))
    rowsLayout.removeComponent(r)
  }

  private val addAction1: RowView => RowView = r => {
    val index = rows.indexOf(r) + 1
    val newRow = new RowView(lengths.map(_ => ""), lengths, deleteAction)
    rows.insert(index, newRow)
    rowsLayout.addComponent(newRow, index)
    newRow
  }

  private val addAction2: RowView => Unit =
    addAction1.andThen(r => r.setAddAction(addAction1.andThen(_.setAddAction(r => addAction1(r)))))

  private val addAction3: Unit => RowView = _ => {
    val newRow = new RowView(lengths.map(_ => ""), lengths, deleteAction)
    rows.insert(0, newRow)
    rowsLayout.addComponent(newRow, 0)
    newRow
  }

  val addAction4: Unit => Unit =
    addAction3.andThen(r => r.setAddAction(addAction1.andThen(_.setAddAction(r => addAction1(r)))))


  private val header: HeaderView = new HeaderView(data.headers, lengths, addAction4)


  {
    data.rows.map { row =>
      val rv = new RowView(row, lengths, deleteAction)
      rv.setAddAction(addAction2)
      rowsLayout.addComponent(rv)
      rv
    }.foreach(rows += _)

    setSizeFull()
    val save = new Button("Save")
    save.addStyleName(ValoTheme.BUTTON_PRIMARY)
    save.addClickListener { _ =>
      val result = TableData(header.getHeaders, rows.map(_.getRow))
      mainView.setOutput(TableDrawer.drawTable(result, None))
      mainView.setInput(TableDrawer.drawTable(result, None))
      getUI.setContent(mainView)
    }

    val cancel = new Button("Cancel")
    cancel.addClickListener(_ => getUI.setContent(mainView))

    addComponent(header)
    val rowsPanel = new Panel()
    rowsPanel.setSizeFull()
    rowsPanel.addStyleName(ValoTheme.PANEL_BORDERLESS)
    rowsLayout.setMargin(false)
    rowsLayout.addComponents(rows: _*)
    rowsPanel.setContent(rowsLayout)

    addComponent(rowsPanel)

    val buttons = new HorizontalLayout(cancel, save)
    buttons.setSizeUndefined()

    addComponent(buttons)
    setExpandRatio(rowsPanel, 1)
    setExpandRatio(buttons, 0)
    setExpandRatio(header, 0)
    setComponentAlignment(buttons, Alignment.BOTTOM_RIGHT)
  }

}