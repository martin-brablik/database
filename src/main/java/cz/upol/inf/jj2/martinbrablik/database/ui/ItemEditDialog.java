package cz.upol.inf.jj2.martinbrablik.database.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.regex.Pattern;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import cz.upol.inf.jj2.martinbrablik.database.models.Item;


public class ItemEditDialog extends JDialog {
	
	private static final long serialVersionUID = 1484764036241193640L;
	private JPanel buttonPanel = new JPanel();
	private JPanel inputPanel = new JPanel();
	private JButton btnOk;
	private JButton btnCancel;
	
	private JTextField txtName;
	private JSpinner txtPrice;
	private JSpinner txtAmount;
	private JLabel lbName;
	private JLabel lbPrice;
	private JLabel lbAmount;
	
	private Item item;
	
	public ItemEditDialog(JDialog parentFrame, Item item) {		
		super(parentFrame);
		this.item = item;
		this.setTitle("Upravit položku");
		this.setPreferredSize(new Dimension(400, 300));
		
		lbName = new JLabel("Název:");
		lbPrice = new JLabel("Cena za kus:");
		lbAmount = new JLabel("Množství:");
		txtName = new JTextField();
		txtPrice = new JSpinner();
		txtAmount = new JSpinner();
		
		txtName.setText(item.getProductName());
		txtPrice.setValue(item.getUnitPrice());
		txtAmount.setValue(item.getAmount());
		
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
			applyChanges();
			setVisible(false);
		});
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener((ActionEvent e) -> {
			setVisible(false);
		});
	
		buttonPanel.setLayout(btnLayout);
		buttonPanel.add(btnOk);
		buttonPanel.add(btnCancel);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(inputPanel, BorderLayout.NORTH);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	  
		pack();
	}
	
	public void applyChanges() {
		if(isValidInput()) {
			item.setAmount((int)txtAmount.getValue());
			item.setUnitPrice((int)txtPrice.getValue());
			item.setProductName(txtName.getText());
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
}
