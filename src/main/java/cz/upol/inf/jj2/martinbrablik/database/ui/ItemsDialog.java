package cz.upol.inf.jj2.martinbrablik.database.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import cz.upol.inf.jj2.martinbrablik.database.models.Item;

public class ItemsDialog extends JDialog {
	
	private static final long serialVersionUID = 4347959881117656490L;
	private JPanel buttonPanel = new JPanel();
	private JPanel inputPanel = new JPanel();
	private JButton btnOk;
	private JButton btnAddItem;
	private JButton btnCancel;
	
	private JTextField txtName;
	private JSpinner txtPrice;
	private JSpinner txtAmount;
	private JLabel lbName;
	private JLabel lbPrice;
	private JLabel lbAmount;
	
	private DefaultListModel<String> listModel;
	private JList<String> itemList;
	private JScrollPane scrollList;
	
	private ArrayList<Item> result = new ArrayList<Item>();
	
	public ItemsDialog(JDialog parentFrame) {
		super(parentFrame);
		this.setTitle("Pøidat položky");
		this.setPreferredSize(new Dimension(400, 300));
		
		lbName = new JLabel("Název:");
		lbPrice = new JLabel("Cena za kus:");
		lbAmount = new JLabel("Množství:");
		txtName = new JTextField();
		txtPrice = new JSpinner();
		txtAmount = new JSpinner();
		listModel = new DefaultListModel<String>();
		itemList = new JList<String>(listModel);
		scrollList = new JScrollPane(itemList);
		
		GroupLayout layout = new GroupLayout(inputPanel);
		inputPanel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(lbName)
					.addComponent(lbPrice)
					.addComponent(lbAmount))
				.addGroup(layout.createParallelGroup()
					.addComponent(txtName)
					.addComponent(txtPrice)
					.addComponent(txtAmount))); 
		
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(lbName)
					.addComponent(txtName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(layout.createParallelGroup()
						.addComponent(lbPrice)
						.addComponent(txtPrice, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(layout.createParallelGroup()
					.addComponent(lbAmount)
					.addComponent(txtAmount, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)));
		
		
		
		inputPanel.add(lbName);
		inputPanel.add(txtName);
		
		GridLayout btnLayout = new GridLayout(1, 3);
		btnLayout.setHgap(5);
		btnLayout.setVgap(5);
		btnLayout.setVgap(5);
		
		btnOk = new JButton("Ok");
		btnOk.addActionListener((ActionEvent e) -> {
			setVisible(false);
		});
		
		btnAddItem = new JButton("Pøidat");
		btnAddItem.addActionListener((ActionEvent e) -> {
			addItem();
		});
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener((ActionEvent e) -> {
			result = new ArrayList<Item>();
			setVisible(false);
		});
	
		buttonPanel.setLayout(btnLayout);
		buttonPanel.add(btnOk);
		buttonPanel.add(btnAddItem);
		buttonPanel.add(btnCancel);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(inputPanel, BorderLayout.NORTH);
		getContentPane().add(scrollList, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	  
		pack();
	}
	
	private void addItem() {
		if(isValidInput()) {
			Item item = new Item((int)txtAmount.getValue(), (int)txtPrice.getValue(), txtName.getText().toString());
			listModel.addElement(item.toHtml());
			result.add(item);
		}
		else
			JOptionPane.showMessageDialog(this, "Byly zadány špatné hodnoty", "Chyba", JOptionPane.ERROR_MESSAGE);
	}
	
	private boolean isValidInput() {
		if(!txtName.getText().equals("") && txtPrice.getValue() != null && txtAmount.getValue() != null) {
			Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
			return pattern.matcher(txtPrice.getValue().toString()).matches() && pattern.matcher(txtAmount.getValue().toString()).matches();
		}
		return false;
	}
	
	public ArrayList<Item> getResult() {
		return this.result;
	}
}
