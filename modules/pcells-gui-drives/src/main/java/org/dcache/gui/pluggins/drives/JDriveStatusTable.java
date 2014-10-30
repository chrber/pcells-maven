package org.dcache.gui.pluggins.drives;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;



public class JDriveStatusTable extends JTable {
	
//	public class DriveTableRenderer extends DefaultTableCellRenderer {
//		
//	}
	
	public class DriveTableModel extends AbstractTableModel {

		private String [] title = {"Drive Name" , "Status" , "Cartridge" , "Action"};
		private ArrayList data;
	
		public int getRowCount() {
			return data == null ? 1 : data.size();
		}

		public int getColumnCount() {
			return title.length;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (data == null) {
				return "--";
			}
//			map data columun to table column
			int dataIndex = 0;
			switch (columnIndex) {
			case 0:
				dataIndex = 0;
				break;
			case 1:
				dataIndex = 1;
				break;
			case 2:
				dataIndex = 2;
				break;
			case 3:
				dataIndex = 5;
				break;
			default:
				break;
			}
			
			return ((String[]) data.get(rowIndex))[dataIndex];
		}

		public String getColumnName(int column) {
			return title[column];
		}

		public void update(ArrayList drives) {
			this.data = drives;
			fireTableDataChanged();
		}
		
	}

	private DriveTableModel model;
	
	public JDriveStatusTable() {
		super();
		this.model = new DriveTableModel();
		setModel(this.model);
		
	}
	
	public void updateTable(ArrayList drives) {
		model.update(drives);
	}
}
