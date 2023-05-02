package cz.upol.inf.jj2.martinbrablik.database.ui;

import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import cz.upol.inf.jj2.martinbrablik.database.models.Item;
import cz.upol.inf.jj2.martinbrablik.database.models.Receipt;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class ReceiptDialog extends JDialog {
	
	private static final long serialVersionUID = -6788079375051070105L;
	private JPanel buttonPanel = new JPanel();
	private JPanel inputPanel = new JPanel();
	private JButton btnDelete;
	private JButton btnEdit;
	private JButton btnOk;
	private JButton btnAddItem;
	private JButton btnCancel;
	
	private JTextField txtName;
	private JTextField txtItin;
	private JLabel lbName;
	private JLabel lbItin;
	
	private DefaultListModel<String> listModel;
	private JList<String> itemList;
	private JScrollPane scrollList;
	
	private List<Item> itemsToAdd = new ArrayList<Item>();
	private Receipt result = null;
	
	public ReceiptDialog(JFrame parentFrame, Receipt receipt) {
		super(parentFrame);
		this.setTitle("Pøidat úètenku");
		this.setPreferredSize(new Dimension(600, 300));
		
		lbName = new JLabel("Název:");
		lbItin = new JLabel("Itin:");
		txtName = new JTextField();
		txtItin = new JTextField();
		listModel = new DefaultListModel<String>();
		itemList = new JList<String>(listModel);
		scrollList = new JScrollPane(itemList);
		
		itemList.addListSelectionListener(l -> {
			btnEdit.setEnabled(itemList.getSelectedIndex() >= 0);
			btnDelete.setEnabled(itemList.getSelectedIndex() >= 0);
		});
		
		if(receipt != null) {
			txtName.setText(receipt.getName());
			txtItin.setText(receipt.getItin());
			itemsToAdd.addAll(receipt.getItems());
			receipt.getItems().forEach(i ->listModel.addElement(i.toHtml()));
		}
		result = receipt;
		
		GroupLayout layout = new GroupLayout(inputPanel);
		inputPanel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(lbName)
					.addComponent(lbItin))
				.addGroup(layout.createParallelGroup()
					.addComponent(txtName)
					.addComponent(txtItin))); 
		
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(lbName)
					.addComponent(txtName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(layout.createParallelGroup()
					.addComponent(lbItin)
					.addComponent(txtItin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)));
		
		inputPanel.add(lbName);
		inputPanel.add(txtName);
		
		GridLayout btnLayout = new GridLayout(1, 3);
		btnLayout.setHgap(5);
		btnLayout.setVgap(5);
		btnLayout.setVgap(5);
		
		btnOk = new JButton("Ok");
		btnOk.addActionListener((ActionEvent e) -> {
			result = makeResult();
			if(result != null) {
				setVisible(false);
			}
		});
		
		btnAddItem = new JButton("Pøidat");
		btnAddItem.addActionListener(this::openItemsDialog);
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener((ActionEvent e) -> {
			result = null;
			setVisible(false);
		});
		
		btnEdit = new JButton("Upravit");
		btnEdit.addActionListener(this::editItem);
				
		btnEdit.setEnabled(false);
		
		btnDelete = new JButton("Odstranit");
		btnDelete.addActionListener(this::deleteItem);
		btnDelete.setEnabled(false);
	
		buttonPanel.setLayout(btnLayout);
		buttonPanel.add(btnOk);
		buttonPanel.add(btnAddItem);
		buttonPanel.add(btnEdit);
		buttonPanel.add(btnDelete);
		buttonPanel.add(btnCancel);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(inputPanel, BorderLayout.NORTH);
		getContentPane().add(scrollList, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	  
		pack();
	}
	
	private void editItem(ActionEvent e) {
		openEditDialog();
		int selectedIndex = itemList.getSelectedIndex();
		listModel.set(selectedIndex, itemsToAdd.get(selectedIndex).toHtml());
	}
	
	private void deleteItem(ActionEvent e) {
		int selectedIndex = itemList.getSelectedIndex();
		if(result != null)
			result.getItems().get(selectedIndex).delete();
		itemsToAdd.remove(selectedIndex);
		listModel.remove(selectedIndex);
	}
	
	private void openEditDialog() {
		int selectedIndex = itemList.getSelectedIndex();
		ItemEditDialog dialog = new ItemEditDialog(this, itemsToAdd.get(selectedIndex));
		dialog.setModal(true);
		dialog.setVisible(true);	
	}
	
	private void openItemsDialog(ActionEvent e) {
		ItemsDialog dialog = new ItemsDialog(this);
		dialog.setModal(true);
		dialog.setVisible(true);
		itemsToAdd.addAll(dialog.getResult());
		dialog.getResult().forEach(i -> listModel.addElement(i.toHtml()));
		dialog.dispose();
	}
	
	
	private Receipt makeResult() {
		if(isValidInput()) {
			int total = 0;
			for(Item i : itemsToAdd) {
				total += i.getAmount() * i.getUnitPrice();
			}
			if(result == null)
				return new Receipt(total, txtName.getText(), txtItin.getText(), itemsToAdd);
			else {
				result.setTotal(total);
				result.setName(txtName.getText());
				result.setItin(txtItin.getText());
				result.setItems(itemsToAdd);
				return result;
			}
		}
		else {
			JOptionPane.showMessageDialog(this, "Vyplòte správnì všechna pole", "Chyba", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}
	
	private boolean isValidInput() {
		return !txtName.getText().equals("") && !txtItin.getText().equals("") && txtItin.getText().length() <= 10;
	}

	public Receipt getResult() {
		return result;
	}
}
