/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ecom;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author sb
 */
public class CustomerTable {
    private final SimpleStringProperty productName;
    private final SimpleStringProperty productDescription;
    private final SimpleStringProperty companyName;
    private final SimpleBooleanProperty inStock;
    private final SimpleDoubleProperty price;
    private final SimpleIntegerProperty itemID;
    private final SimpleIntegerProperty sellerID;
    
    public CustomerTable() {
        //default constructor
        this.productName = new SimpleStringProperty("");
        this.productDescription = new SimpleStringProperty("");
        this.companyName = new SimpleStringProperty("");
        this.inStock = new SimpleBooleanProperty(true);
        this.price = new SimpleDoubleProperty(0);
        this.itemID = new SimpleIntegerProperty(0);
        this.sellerID = new SimpleIntegerProperty(0);
    }
    
    public CustomerTable(String productNameArg, String productDescriptionArg, String companyNameArg, boolean inStockArg, double priceArg, int itemIDArg, int sellerIDArg) {
        this.productName = new SimpleStringProperty(productNameArg);
        this.productDescription = new SimpleStringProperty(productDescriptionArg);
        this.companyName = new SimpleStringProperty(companyNameArg);
        this.inStock = new SimpleBooleanProperty(inStockArg);
        this.price = new SimpleDoubleProperty(priceArg);
        this.itemID = new SimpleIntegerProperty(itemIDArg);
        this.sellerID = new SimpleIntegerProperty(sellerIDArg);
    }
    
    public String getProductName() {
        return productName.get();
    }
    
    public void setProductName(String productNameArg) {
        productName.set(productNameArg);
    }
    
    public String getProductDescription() {
        return productDescription.get();
    }
    
    public void setProductDescription(String productDescriptionArg) {
        productDescription.set(productDescriptionArg);
    }
    
    public String getCompanyName() {
        return companyName.get();
    }
    
    public void setCompanyName(String companyNameArg) {
        companyName.set(companyNameArg);
    }
    
    public boolean getInStock() {
        return inStock.get();
    }
    
    public void setInStock(boolean inStockArg) {
        inStock.set(inStockArg);
    }
    
    public double getPrice() {
        return price.get();
    }
    
    public void setPrice(double priceArg){
        price.set(priceArg);
    }
    
    public int getItemID() {
        return itemID.get();
    }
    
    public void setItemID(int itemIDArg) {
        itemID.set(itemIDArg);
    }
    
    public int getSellerID() {
        return sellerID.get();
    }
    
    public void setSellerID(int sellerIDArg) {
        sellerID.set(sellerIDArg);
    }
}