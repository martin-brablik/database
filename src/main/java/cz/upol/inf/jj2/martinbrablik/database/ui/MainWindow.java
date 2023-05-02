package cz.upol.inf.jj2.martinbrablik.database.ui;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import cz.upol.inf.jj2.martinbrablik.database.DatabaseClient;
import cz.upol.inf.jj2.martinbrablik.database.models.Item;

import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.awt.BorderLayout;

import java.awt.Dimension;

public class MainWindow extends JFrame {
	
	private static final long serialVersionUID = -6106254348715459663L;
	private final JPanel mainPanel = new JPanel();
	private final JMenuBar mainMenu = new JMenuBar();
	private final JMenu menuFile = new JMenu("Soubor");
	private final JMenuItem menuItemAdd = new JMenuItem("Pøidat úètenku");
	private final JMenuItem menuItemEdit = new JMenuItem("Upravit úètenku");
	private final JMenuItem menuItemDelete = new JMenuItem("Odstranit úètenku");
	
	private DefaultListModel<String> listModel = new DefaultListModel<String>();
	private final JList<String> receiptList = new JList<String>(listModel);
	
	public MainWindow() {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setPreferredSize(new Dimension(800, 600));
		
		DatabaseClient.allReceipts.forEach(r -> listModel.addElement(r.toHtml()));
		receiptList.addListSelectionListener((l) -> {
			menuItemEdit.setEnabled(receiptList.getSelectedIndex() >= 0);
			menuItemDelete.setEnabled(receiptList.getSelectedIndex() >= 0);
			}); 
		
		menuItemAdd.addActionListener(this::openAddDialog);
		menuItemEdit.addActionListener(this::openEditDialog);
		menuItemEdit.setEnabled(false);
		menuItemDelete.addActionListener(this::delete);
		menuItemDelete.setEnabled(false);
		menuFile.add(menuItemAdd);
		menuFile.add(menuItemEdit);
		menuFile.add(menuItemDelete);
		mainMenu.add(menuFile);
		this.setJMenuBar(mainMenu);
		
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(new JScrollPane(receiptList));
		this.setContentPane(mainPanel);
		this.pack();
	}
	
	private void delete(ActionEvent e) {
		int selectedIndex = receiptList.getSelectedIndex();
		if(selectedIndex == -1) {
			JOptionPane.showMessageDialog(this, "Vyberte úètenku", "Chyba", JOptionPane.ERROR_MESSAGE);
			return;
		}
		DatabaseClient.allReceipts.get(selectedIndex).delete();
		DatabaseClient.allReceipts.remove(selectedIndex);
		listModel.remove(selectedIndex);
		//receiptList.removeNotify();
	}
	
	private void openAddDialog(ActionEvent e) {
		openDialog(true, (d) -> {
			listModel.addElement(d.getResult().toHtml());
			DatabaseClient.allReceipts.add(d.getResult());
			d.getResult().insert();
		});
	}
	
	private void openEditDialog(ActionEvent e) {
		openDialog(false, (d) -> {
			listModel.set(receiptList.getSelectedIndex(), d.getResult().toHtml());
			DatabaseClient.allReceipts.set(receiptList.getSelectedIndex(), d.getResult());
			d.getResult().update();
			for(Item item : d.getResult().getItems()) {
				try (Connection con = DriverManager.getConnection(DatabaseClient.connectionURL, DatabaseClient.USERNAME, DatabaseClient.PASSWORD)) {
					if(!DatabaseClient.dbClient.hasItem(item, con)) {
						item.insert(d.getResult());
					}
					else
						item.update();
					con.close();
				}
				catch (SQLException e1) {
					e1.printStackTrace();
				}
				
			}
		});
	}
	private void openDialog(boolean addNew, Consumer<ReceiptDialog> c) {
		int selectedIndex = receiptList.getSelectedIndex();
		ReceiptDialog dialog;
		if(addNew)
			dialog = new ReceiptDialog(this, null);
		else {
			if(selectedIndex < 0) {
				JOptionPane.showMessageDialog(this, "Vyberte úètenku", "Chyba", JOptionPane.ERROR_MESSAGE);
				return;
			}
			dialog = new ReceiptDialog(this, DatabaseClient.allReceipts.get(selectedIndex));
		}
		dialog.setModal(true);
		dialog.setVisible(true);
		if(dialog.getResult() != null) {
			c.accept(dialog);
		}
		dialog.dispose();
	}
}
