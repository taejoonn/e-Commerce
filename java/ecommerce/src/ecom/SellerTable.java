/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ecom;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class SellerTable {
    private final SimpleStringProperty productName;
    private final SimpleStringProperty productDescription;
    private final SimpleIntegerProperty stockQuantity;
    private final SimpleDoubleProperty price;
    private final SimpleIntegerProperty itemID;
    private final SimpleIntegerProperty sellerID;
    
    public SellerTable() {
        //default constructor
        this.productName = new SimpleStringProperty("");
        this.productDescription = new SimpleStringProperty("");
        this.stockQuantity = new SimpleIntegerProperty(0);
        this.price = new SimpleDoubleProperty(0);
        this.itemID = new SimpleIntegerProperty(0);
        this.sellerID = new SimpleIntegerProperty(0);
    }
    
    public SellerTable(String productNameArg, String productDescriptionArg, int stockQuantityArg, double priceArg, int itemIDArg, int sellerIDArg) {
        this.productName = new SimpleStringProperty(productNameArg);
        this.productDescription = new SimpleStringProperty(productDescriptionArg);
        this.stockQuantity = new SimpleIntegerProperty(stockQuantityArg);
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
    
    public int getStockQuantity() {
        return stockQuantity.get();
    }
    
    public void setStockQuantity(int stockQuantityArg) {
        stockQuantity.set(stockQuantityArg);
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