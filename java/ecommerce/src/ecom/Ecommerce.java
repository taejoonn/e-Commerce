/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ecom;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.Scanner;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

/**
 *
 * @author sb
 */
public class Ecommerce extends Application {

    static TableView<CustomerTable> cartTable = new TableView();
    static ObservableList<CustomerTable> cartData = FXCollections.observableArrayList();
    static SQLHandler sql = new SQLHandler();
    User user = new User();
    Seller seller = new Seller();
    Customers customer = new Customers();
    BillingInfo billing = new BillingInfo();
    Item item = new Item();
    static ArrayList<Inventory> inventory = new ArrayList<>();
    Category category = new Category();
    Reviews review = new Reviews();
    ShoppingCart cart = new ShoppingCart();
    ArrayList<Orders> order = new ArrayList<>();
    ArrayList<Ordered> ordered = new ArrayList<>();
    Shipment shipping = new Shipment();

    @Override
    public void start(Stage primaryStage) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        // Read SQL script and create relations
        String parse = new String();
        String query = new String();

        sql.openConnection();
        File file = new File("src/SQLScript.sql");
        // parses queries and executes them in db (assumes script is written properly)
        try (Scanner scan = new Scanner(file)) {
            while (scan.hasNextLine()) {
                parse = scan.nextLine();
                query += parse.replaceAll("\t+", " ");
                if (parse.contains(";")) {
                    query = query.replace(";", "");
                    sql.modifyData(query);
                    query = "";
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("SQL Script file not found.");
        }
        sql.closeConnection();
        boolean looper = true;
        while (looper == true) {
            boolean existingAccount = false;
            boolean newAccount = false;

            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Welcome!");
            alert.setHeaderText(null);
            alert.setContentText("Pick an option!");

            ButtonType buttonTypeExisting = new ButtonType("Existing user login");
            ButtonType buttonTypeNewUsers = new ButtonType("Create new account");
            ButtonType buttonTypeCancel = new ButtonType("Cancel");

            alert.getDialogPane().setMinWidth(500);
            alert.getButtonTypes().setAll(buttonTypeExisting, buttonTypeNewUsers, buttonTypeCancel);

            Optional<ButtonType> confirmationResult = alert.showAndWait();
            if (confirmationResult.get() == buttonTypeExisting) {
                existingAccount = true;
            } else if (confirmationResult.get() == buttonTypeNewUsers) {
                newAccount = true;
            } else {
                System.exit(0);
            }

            if (existingAccount) {
                Dialog<Pair<String, String>> dialog = new Dialog<>();
                dialog.setTitle("Login Dialog");
                dialog.setHeaderText(null);

                // Set the button types.
                ButtonType loginButtonType = new ButtonType("Login", ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

                // Create the username and password labels and fields.
                GridPane grid = new GridPane();
                grid.setAlignment(Pos.CENTER);
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(50, 50, 50, 50));

                TextField username = new TextField();
                username.setPromptText("Username");
                PasswordField password = new PasswordField();
                password.setPromptText("Password");

                grid.add(new Label("Username:"), 0, 0);
                grid.add(username, 1, 0);
                grid.add(new Label("Password:"), 0, 1);
                grid.add(password, 1, 1);

                // Enable/Disable login button depending on whether a username was entered.
                Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
                loginButton.setDisable(true);

                // Do some validation (using the Java 8 lambda syntax).
                username.textProperty().addListener((observable, oldValue, newValue) -> {
                    loginButton.setDisable(newValue.trim().isEmpty());
                });

                dialog.getDialogPane().setContent(grid);

                // Request focus on the username field by default.
                Platform.runLater(() -> username.requestFocus());

                // Convert the result to a username-password-pair when the login button is clicked.
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == loginButtonType) {
                        return new Pair<>(username.getText(), password.getText());
                    } else {
                        System.exit(0);
                    }
                    return null;
                });

                Optional<Pair<String, String>> result = dialog.showAndWait();

                result.ifPresent(usernamePassword -> {
                    System.out.println("Username=" + usernamePassword.getKey() + ", Password=" + usernamePassword.getValue());
                });

                user.setUsername(result.get().getKey());
                user.setPassword(result.get().getValue());

                sql.closeConnection();
                sql.openConnection();
                sql.getTable("SELECT * FROM User WHERE Username = '" + new String(user.getUsername())
                        + "' AND Password = '" + new String(user.getPassword()) + "'");
                if (sql.next()) {
                    user = sql.getUser();
                } else {
                    continue;
                }
                sql.closeConnection();
                sql.openConnection();

                if (user.getCustomerID() > 0) {
                    sql.getTable("SELECT * FROM Customers WHERE Id = " + user.getCustomerID());
                    if (sql.next()) {
                        customer = sql.getCustomer();
                    }
                    sql.closeConnection();
                    sql.openConnection();
                    sql.getTable("SELECT * FROM BillingInfo WHERE Id = " + customer.getBillingInfoID());
                    if (sql.next()) {
                        billing = sql.getBillingInfo();
                    }
                    sql.closeConnection();
                    sql.openConnection();
                    sql.getTable("SELECT * FROM ShoppingCart WHERE CustomerId = " + customer.getID());
                    while (sql.next()) {
                        cart = sql.getShoppingCart();
                        cartData.add(new CustomerTable("", "", "", true, cart.getPrice(), cart.getItemID(), cart.getSellerID()));
                    }
                    sql.closeConnection();
                    sql.openConnection();

                    for (int i = 0; i < cartData.size(); i++) {
                        sql.getTable("SELECT * FROM Item WHERE ItemId = " + cartData.get(i).getItemID());
                        if (sql.next()) {
                            item = sql.getItem();
                            cartData.get(i).setProductName(new String(item.getItemName()));
                            cartData.get(i).setProductDescription(new String(item.getDescription()));
                        }
                        sql.closeConnection();
                        sql.openConnection();
                        sql.getTable("SELECT * FROM Inventory WHERE ItemId = " + cartData.get(i).getItemID()
                                + " AND SellerId = " + cartData.get(i).getSellerID());
                        if (sql.next()) {
                            Inventory inv = sql.getInventory();
                            if (inv.getQuantity() <= 0) {
                                cartData.get(i).setInStock(false);
                            }
                        }
                        sql.closeConnection();
                        sql.openConnection();
                        sql.getTable("SELECT * FROM Seller WHERE Id = " + cartData.get(i).getSellerID());
                        if (sql.next()) {
                            seller = sql.getSeller();
                            cartData.get(i).setCompanyName(new String(seller.getCompanyName()));
                        }
                        sql.closeConnection();
                        sql.openConnection();
                    }

                    BorderPane root = new BorderPane();
                    Button placedOrdersButton = new Button("Placed Orders");
                    placedOrdersButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            //System.out.println("placed orders button was pressed!");

                            Alert alert = new Alert(AlertType.INFORMATION);
                            alert.setTitle("Placed Orders");
                            alert.setHeaderText(null);
                            String contentText = "";

                            sql.closeConnection();
                            sql.openConnection();
                            sql.getTable("SELECT * FROM Orders WHERE CustomerId = " + customer.getID());
                            while (sql.next()) {
                                order.add(sql.getOrders());
                            }
                            sql.closeConnection();
                            sql.openConnection();
                            for (int i = 0; i < order.size(); i++) {
                                sql.getTable("SELECT * FROM Ordered WHERE OrderId = " + order.get(i).getID());
                                while (sql.next()) {
                                    ordered.add(sql.getOrdered());
                                }
                                sql.closeConnection();
                                sql.openConnection();
                            }
                            for (int i = 0; i < order.size(); i++) {
                                int index = 0;
                                sql.getTable("SELECT * FROM Item WHERE Id = " + ordered.get(i).getItemID());
                                if (sql.next()) {
                                    item = sql.getItem();
                                }
                                sql.closeConnection();
                                sql.openConnection();
                                for (int j = 0; j < order.size(); j++) {
                                    if (order.get(j).getID() == ordered.get(i).getOrderID()) {
                                        index = j;
                                        break;
                                    }
                                }
                                String orderIDColon = "Order ID: ";
                                String orderID = Integer.toString(ordered.get(i).getOrderID());
                                String itemIDColon = "Item ID: ";
                                String itemID = Integer.toString(ordered.get(i).getItemID());
                                String itemNameColon = "Item Name: ";
                                String itemName = new String(item.getItemName());
                                String sellerIDColon = "Seller ID: ";
                                String sellerID = Integer.toString(ordered.get(i).getSellerID());
                                String priceColon = "Price: ";
                                String price = Double.toString(ordered.get(i).getPrice());
                                String itemAmt = "Quantity: ";
                                String amt = Integer.toString(ordered.get(i).getQuantity());
                                String orderedDateColon = "Orderd Date: ";
                                String orderedDate = df.format(order.get(index).getOrderDate());

                                contentText = contentText + orderIDColon + orderID + "\n" + itemIDColon + itemID + "\n" + itemNameColon + itemName + "\n" + sellerIDColon + sellerID + "\n"
                                        + priceColon + price + "\n" + itemAmt + amt + "\n" + orderedDateColon + orderedDate;

                                if (i == cartData.size() - 1) {
                                    //Do nothing
                                } else {
                                    contentText = contentText + "\n\n\n";
                                }
                                System.out.println(contentText);

                            }
                            alert.setContentText(contentText);

                            alert.showAndWait();
                        }
                    });
                    Button accountButton = new Button("Account Info");
                    accountButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            //System.out.println("account button was pressed!");
                            /**
                             * *************************************************************
                             * This is where ACCOUNT INFORMATION window shows up
                             * ************************************************************
                             */
                            Alert alert = new Alert(AlertType.INFORMATION);
                            alert.setTitle("Account Info");
                            alert.setHeaderText(null);
                            String firstNameColon = "First Name: ";
                            String customerFirstName = new String(customer.getFirstName());
                            String lastNameColon = "Second Name: ";
                            String customerLastName = new String(customer.getLastName());
                            String emailColon = "Email: ";
                            String customerEmail = new String(customer.getEmail());
                            String contentText = firstNameColon + customerFirstName + "\n" + lastNameColon + customerLastName + "\n" + emailColon + customerEmail;
                            alert.setContentText(contentText);

                            alert.showAndWait();
                        }
                    });
                    Button infoButton = new Button("Billing Info");
                    infoButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            //System.out.println("info button was pressed!");
                            /**
                             * *************************************************************
                             * This is where BILLING INFORMATION window shows up
                             * ************************************************************
                             */
                            Alert alert = new Alert(AlertType.INFORMATION);
                            alert.setTitle("Billing Info");
                            alert.setHeaderText(null);
                            String addressColon = "Address: ";
                            String address = new String(billing.getAddress());
                            String cardTypeColon = "CardType: ";
                            String cardType = new String(billing.getCardType());
                            String cardNumberColon = "Card Number: ";
                            long cardNumberInt = billing.getCardNumber();
                            String cardNumber = Double.toString(cardNumberInt);
                            String cardExpiryDateColon = "Card Expiry Date: ";
                            String cardExpiryDate = new String(billing.getCardExpiryDate());
                            String phoneNumberColon = "Phone Number: ";
                            long phoneNumberLong = billing.getPhoneNumber();
                            String phoneNumber = Long.toString(phoneNumberLong);
                            String contentText = addressColon + address + "\n" + cardTypeColon + cardType + "\n" + cardNumberColon + cardNumber + "\n" + cardExpiryDateColon + cardExpiryDate + "\n" + phoneNumberColon + phoneNumber;
                            alert.setContentText(contentText);

                            alert.showAndWait();
                        }
                    });
                    ToolBar toolbar = new ToolBar(
                            placedOrdersButton,
                            accountButton,
                            infoButton
                    );

                    TableView<CustomerTable> availableTable = new TableView();
                    ObservableList<CustomerTable> availableData = FXCollections.observableArrayList();
                    availableTable.setEditable(true);

                    TableColumn productNameTableColumn = new TableColumn("Product Name");
                    productNameTableColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
                    productNameTableColumn.prefWidthProperty().bind(availableTable.widthProperty().divide(6));

                    TableColumn productDescriptionTableColumn = new TableColumn("Product Description");
                    productDescriptionTableColumn.setCellValueFactory(new PropertyValueFactory<>("productDescription"));
                    productDescriptionTableColumn.prefWidthProperty().bind(availableTable.widthProperty().divide(6));

                    TableColumn companyNameTableColumn = new TableColumn("Company Name");
                    companyNameTableColumn.setCellValueFactory(new PropertyValueFactory<>("companyName"));
                    companyNameTableColumn.prefWidthProperty().bind(availableTable.widthProperty().divide(6));

                    TableColumn inStockTableColumn = new TableColumn("In stock?");
                    inStockTableColumn.setCellValueFactory(new PropertyValueFactory<>("inStock"));
                    inStockTableColumn.prefWidthProperty().bind(availableTable.widthProperty().divide(6));

                    TableColumn priceTableColumn = new TableColumn("Price ($)");
                    priceTableColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
                    priceTableColumn.prefWidthProperty().bind(availableTable.widthProperty().divide(6));

                    TableColumn buttonTableColumn = new TableColumn("Button");
                    buttonTableColumn.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));
                    buttonTableColumn.prefWidthProperty().bind(availableTable.widthProperty().divide(6));

                    //TableView<CustomerTable> cartTable = new TableView();
                    //ObservableList<CustomerTable> cartData = FXCollections.observableArrayList();
                    TableColumn productNameCartColumn = new TableColumn("Product Name");
                    productNameCartColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
                    productNameCartColumn.prefWidthProperty().bind(cartTable.widthProperty().divide(3));

                    TableColumn productDescriptionCartColumn = new TableColumn("Product Description");
                    productDescriptionCartColumn.setCellValueFactory(new PropertyValueFactory<>("productDescription"));
                    productDescriptionCartColumn.prefWidthProperty().bind(cartTable.widthProperty().divide(3));

                    TableColumn priceCartColumn = new TableColumn("Price ($)");
                    priceCartColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
                    priceCartColumn.prefWidthProperty().bind(cartTable.widthProperty().divide(3));

                    Callback<TableColumn<CustomerTable, String>, TableCell<CustomerTable, String>> cellFactory
                            = //
                            new Callback<TableColumn<CustomerTable, String>, TableCell<CustomerTable, String>>() {
                        @Override
                        public TableCell call(final TableColumn<CustomerTable, String> param) {
                            final TableCell<CustomerTable, String> cell = new TableCell<CustomerTable, String>() {

                                final Button btn = new Button("Add to Cart");

                                @Override
                                public void updateItem(String item, boolean empty) {
                                    super.updateItem(item, empty);
                                    if (empty) {
                                        setGraphic(null);
                                        setText(null);
                                    } else {
                                        btn.setOnAction(event -> {
                                            /**
                                             * *********************************************
                                             * This is where ITEMS TO THE CART
                                             * are added
                                             * ********************************************
                                             */
                                            CustomerTable addToCart = getTableView().getItems().get(getIndex());
                                            cartData.add(addToCart);
                                            //availableData.remove(addToCart);
                                            //System.out.println(person.getFirstName() + "   " + person.getLastName());
                                            //CustomerTable item = getTableView().getItems().get(getIndex());
                                        });
                                        setGraphic(btn);
                                        setText(null);
                                    }
                                }
                            };
                            return cell;
                        }
                    };
                    buttonTableColumn.setCellFactory(cellFactory);

                    availableTable.getColumns().addAll(productNameTableColumn, productDescriptionTableColumn, companyNameTableColumn, inStockTableColumn, priceTableColumn, buttonTableColumn);
                    availableTable.setItems(availableData);
                    cartTable.getColumns().addAll(productNameCartColumn, productDescriptionCartColumn, priceCartColumn);
                    cartTable.setItems(cartData);
                    sql.getTable("SELECT * FROM Item");
                    while (sql.next()) {
                        item = sql.getItem();
                        availableData.add(new CustomerTable(new String(item.getItemName()), new String(item.getDescription()), "", true, item.getPrice(), item.getID(), item.getSellerID()));
                    }
                    sql.closeConnection();
                    sql.openConnection();

                    for (int i = 0; i < availableData.size(); i++) {
                        sql.getTable("SELECT * FROM Inventory WHERE ItemId = " + availableData.get(i).getItemID()
                                + " AND SellerId = " + availableData.get(i).getSellerID());
                        if (sql.next()) {
                            Inventory inv = sql.getInventory();
                            if (inv.getQuantity() <= 0) {
                                availableData.get(i).setInStock(false);
                            }
                        }
                        sql.closeConnection();
                        sql.openConnection();
                        sql.getTable("SELECT * FROM Seller WHERE Id = " + availableData.get(i).getSellerID());
                        if (sql.next()) {
                            seller = sql.getSeller();
                            availableData.get(i).setCompanyName(new String(seller.getCompanyName()));
                        }
                        sql.closeConnection();
                        sql.openConnection();
                    }

                    Button placeOrderButton = new Button();
                    placeOrderButton.setText("Place Order!");
                    placeOrderButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            Orders or;
                            Ordered od;
                            or = new Orders();
                            or.setCustomerID(customer.getID());
                            or.setBillingInfoID(billing.getID());
                            sql.insertData(or);
                            sql.closeConnection();
                            sql.openConnection();
                            sql.getTable("SELECT * FROM Orders WHERE CustomerId = " + customer.getID());
                            while (sql.next()) {
                                or = sql.getOrders();
                            }
                            sql.closeConnection();
                            sql.openConnection();
                            while (cartData.size() > 0) {
                                cart = new ShoppingCart(customer.getID(), cartData.get(0).getItemID(), cartData.get(0).getSellerID(), 1, cartData.get(0).getPrice(), cartData.get(0).getPrice());
                                sql.getTable("SELECT * FROM ShoppingCart WHERE CustomerId = " + customer.getID() + " AND ItemId = "
                                        + cartData.get(0).getItemID());
                                if (sql.next()) {
                                    cart = sql.getShoppingCart();
                                }
                                sql.closeConnection();
                                sql.openConnection();
                                sql.modifyData("UPDATE Inventory SET Quantity = Quantity - " + cart.getQuantity()
                                        + " WHERE ItemId = " + cart.getItemID() + " AND SellerId = " + cart.getSellerID());

                                od = new Ordered(or.getID(), cart.getItemID(), cart.getSellerID(), cart.getQuantity(), cart.getPrice());
                                sql.insertData(od);
                                // delete cart
                                sql.modifyData("DELETE FROM ShoppingCart WHERE CustomerId = " + customer.getID() + " AND ItemId = "
                                        + cartData.get(0).getItemID());
                                sql.closeConnection();
                                sql.openConnection();
                                cartData.remove(0);
                            }
                        }
                    });

                    HBox hbox = new HBox();
                    hbox.setAlignment(Pos.CENTER_RIGHT);
                    hbox.getChildren().add(placeOrderButton);

                    VBox vbox = new VBox();
                    vbox.setSpacing(5);
                    vbox.setPadding(new Insets(10, 10, 10, 10));

                    Label availableLabel = new Label("Items available for purchase");
                    Font font = new Font("Arial", 20);
                    availableLabel.setFont(font);
                    Label cartLabel = new Label("Shopping Cart");
                    cartLabel.setFont(new Font("Arial", 20));
                    Separator separator = new Separator();
                    separator.setOrientation(Orientation.HORIZONTAL);
                    separator.setPadding(new Insets(10, 0, 0, 0));
                    vbox.getChildren().addAll(availableLabel, availableTable, separator, cartLabel, cartTable, hbox);

                    root.setTop(toolbar);
                    root.setCenter(vbox);

                    Scene scene = new Scene(root, 300, 250);

                    //This stores the screen size
                    Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
                    primaryStage.setTitle("Customer View!");
                    primaryStage.setWidth(primaryScreenBounds.getWidth());
                    primaryStage.setHeight(primaryScreenBounds.getHeight());
                    primaryStage.setScene(scene);
                    primaryStage.show();
                } else if (user.getSellerID() > 0) {
                    sql.getTable("SELECT * FROM Seller WHERE Id = " + user.getSellerID());
                    if (sql.next()) {
                        seller = sql.getSeller();
                    }
                    sql.closeConnection();
                    sql.openConnection();
                    sql.getTable("SELECT * FROM BillingInfo WHERE Id = " + seller.getBillingInfoID());
                    if (sql.next()) {
                        billing = sql.getBillingInfo();
                    }
                    sql.closeConnection();
                    sql.openConnection();
                    item.setSellerID(seller.getID());

                    BorderPane root = new BorderPane();
                    primaryStage.setTitle("Seller View");
                    primaryStage.setWidth(925);
                    primaryStage.setHeight(600);

                    final Label label = new Label("Seller Table");
                    label.setPadding(new Insets(10, 10, 10, 10));
                    label.setFont(new Font("Arial", 20));
                    TableView<SellerTable> table = new TableView();
                    table.setEditable(true);

                    TableColumn<SellerTable, String> productNameCol = new TableColumn("Product Name");
                    productNameCol.prefWidthProperty().bind(table.widthProperty().divide(4));
                    productNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
                    productNameCol.setCellFactory(TextFieldTableCell.<SellerTable>forTableColumn());
                    productNameCol.setOnEditCommit(
                            (CellEditEvent<SellerTable, String> t) -> {
                                ((SellerTable) t.getTableView().getItems().get(t.getTablePosition().getRow())).setProductName(t.getNewValue());
                                sql.closeConnection();
                                sql.openConnection();
                                sql.modifyData("UPDATE Item SET ItemName = " + t.getTableView().getItems().get(t.getTablePosition().getRow()).getProductName()
                                        + " WHERE ItemId = " + t.getTableView().getItems().get(t.getTablePosition().getRow()).getItemID());
                                sql.closeConnection();
                                sql.openConnection();
                            });

                    TableColumn<SellerTable, String> productDescriptionCol = new TableColumn("Product Description");
                    productDescriptionCol.prefWidthProperty().bind(table.widthProperty().divide(4));
                    productDescriptionCol.setCellValueFactory(new PropertyValueFactory<SellerTable, String>("productDescription"));
                    productDescriptionCol.setCellFactory(TextFieldTableCell.<SellerTable>forTableColumn());
                    productDescriptionCol.setOnEditCommit(
                            (CellEditEvent<SellerTable, String> t) -> {
                                ((SellerTable) t.getTableView().getItems().get(t.getTablePosition().getRow())).setProductDescription(t.getNewValue());
                                sql.closeConnection();
                                sql.openConnection();
                                sql.modifyData("UPDATE Item SET Description = " + t.getTableView().getItems().get(t.getTablePosition().getRow()).getProductDescription()
                                        + " WHERE ItemId = " + t.getTableView().getItems().get(t.getTablePosition().getRow()).getItemID());
                                sql.closeConnection();
                                sql.openConnection();
                            });

                    TableColumn<SellerTable, Integer> stockQuantityCol = new TableColumn("Stock Quantity");
                    stockQuantityCol.prefWidthProperty().bind(table.widthProperty().divide(4));
                    stockQuantityCol.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
                    stockQuantityCol.setCellFactory(TextFieldTableCell.<SellerTable, Integer>forTableColumn(new IntegerStringConverter()));
                    stockQuantityCol.setOnEditCommit(
                            (CellEditEvent<SellerTable, Integer> t) -> {
                                ((SellerTable) t.getTableView().getItems().get(t.getTablePosition().getRow())).setStockQuantity(t.getNewValue());
                                sql.closeConnection();
                                sql.openConnection();
                                sql.modifyData("UPDATE Inventory SET Quantity = " + t.getTableView().getItems().get(t.getTablePosition().getRow()).getStockQuantity()
                                        + " WHERE ItemId = " + t.getTableView().getItems().get(t.getTablePosition().getRow()).getItemID());
                                sql.closeConnection();
                                sql.openConnection();
                            });

                    TableColumn<SellerTable, Double> priceCol = new TableColumn("Price ($)");
                    priceCol.prefWidthProperty().bind(table.widthProperty().divide(4));
                    priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
                    priceCol.setCellFactory(TextFieldTableCell.<SellerTable, Double>forTableColumn(new DoubleStringConverter()));
                    priceCol.setOnEditCommit((CellEditEvent<SellerTable, Double> t) -> {
                        ((SellerTable) t.getTableView().getItems().get(t.getTablePosition().getRow())).setPrice(t.getNewValue());
                        sql.closeConnection();
                        sql.openConnection();
                        sql.modifyData("UPDATE Inventory SET Price = " + t.getTableView().getItems().get(t.getTablePosition().getRow()).getPrice()
                                + " WHERE ItemId = " + t.getTableView().getItems().get(t.getTablePosition().getRow()).getItemID());
                        sql.modifyData("UPDATE Item SET Price = " + t.getTableView().getItems().get(t.getTablePosition().getRow()).getPrice()
                                + " WHERE ItemId = " + t.getTableView().getItems().get(t.getTablePosition().getRow()).getItemID());
                        sql.closeConnection();
                        sql.openConnection();
                    });

                    ObservableList<SellerTable> data = FXCollections.observableArrayList();
                    table.getColumns().addAll(productNameCol, productDescriptionCol, stockQuantityCol, priceCol);
                    table.setItems(data);

                    final TextField addProductName = new TextField();
                    addProductName.setPromptText("Product Name");
                    addProductName.setMaxWidth(120);
                    final TextField addProductDescription = new TextField();
                    addProductDescription.setMaxWidth(120);
                    addProductDescription.setPromptText("Product Description");
                    final TextField addStockQuantity = new TextField();
                    addStockQuantity.setMaxWidth(105);
                    addStockQuantity.setPromptText("Stock Quantity");
                    final TextField addPrice = new TextField();
                    addPrice.setMaxWidth(105);
                    addPrice.setPromptText("Price ($)");

                    /**
                     * *********************************************************************
                     * This where clicking the add button updates the table with
                     * new item
                     * *******************************************************************
                     */
                    final Button addButton = new Button("Add Item");
                    addButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent e) {
                            data.add(new SellerTable(
                                    addProductName.getText(),
                                    addProductDescription.getText(),
                                    Integer.parseInt(addStockQuantity.getText()),
                                    Double.parseDouble(addPrice.getText()), 0, 0
                            ));

                            item.setItemName(addProductName.getText());
                            item.setDescription(addProductDescription.getText());
                            item.setPrice(Double.parseDouble(addPrice.getText()));
                            sql.insertData(item);
                            sql.closeConnection();
                            sql.openConnection();
                            sql.getTable("SELECT * FROM Item WHERE SellerId = " + item.getSellerID() + " AND Description = '"
                                    + new String(item.getDescription()) + "' AND ItemName = '" + new String(item.getItemName()) + "'");
                            if (sql.next()) {
                                item = sql.getItem();
                            }
                            sql.closeConnection();
                            sql.openConnection();
                            inventory.add(new Inventory(item.getID(), item.getSellerID(), Integer.parseInt(addStockQuantity.getText()), item.getPrice()));
                            sql.insertData(inventory.get(inventory.size() - 1));

                            addProductName.clear();
                            addProductDescription.clear();
                            addStockQuantity.clear();
                            addPrice.clear();
                        }
                    });

                    HBox hb = new HBox();
                    hb.getChildren().addAll(addProductName, addProductDescription, addStockQuantity, addPrice, addButton);
                    hb.setSpacing(3);

                    final VBox vbox = new VBox();
                    vbox.setSpacing(5);
                    vbox.setPadding(new Insets(10, 10, 10, 10));
                    vbox.getChildren().addAll(table, hb);
                    root.setTop(label);
                    root.setCenter(vbox);

                    //((Group) scene.getRoot()).getChildren().addAll(vbox);
                    Scene scene = new Scene(root);

                    primaryStage.setScene(scene);
                    primaryStage.show();

                    sql.closeConnection();
                    sql.openConnection();
                    sql.getTable("SELECT * FROM Inventory WHERE SellerId = " + seller.getID());
                    while (sql.next()) {
                        inventory.add(sql.getInventory());
                    }
                    sql.closeConnection();
                    sql.openConnection();

                    for (int i = 0; i < inventory.size(); i++) {
                        sql.getTable("SELECT * FROM Item WHERE Id = " + inventory.get(i).getItemID());
                        if (sql.next()) {
                            item = sql.getItem();
                        }
                        sql.closeConnection();
                        sql.openConnection();
                        data.add(new SellerTable(new String(item.getItemName()), new String(item.getDescription()), inventory.get(i).getQuantity(), item.getPrice(), item.getID(), seller.getID()));
                    }
                }
                looper = false;
            } else if (newAccount) {
                boolean isCustomer = false;
                boolean isSeller = false;

                ButtonType buttonTypeCustomer = new ButtonType("Customer");
                ButtonType buttonTypeSeller = new ButtonType("Seller");

                alert.setTitle("What type of a user are you?");
                alert.setHeaderText(null);
                alert.setContentText("Are you a customer or a seller?");

                alert.getButtonTypes().clear();
                alert.getButtonTypes().addAll(buttonTypeCustomer, buttonTypeSeller);

                confirmationResult = alert.showAndWait();
                if (confirmationResult.get() == buttonTypeCustomer) {
                    isCustomer = true;
                } else if (confirmationResult.get() == buttonTypeSeller) {
                    isSeller = true;
                }

                Alert newAccountDialog = new Alert(AlertType.CONFIRMATION);

                ButtonType buttonTypeOK = new ButtonType("OK", ButtonData.OK_DONE);
                newAccountDialog.getDialogPane().getButtonTypes().clear();
                newAccountDialog.getDialogPane().getButtonTypes().addAll(buttonTypeCancel, buttonTypeOK);

                newAccountDialog.setTitle("Enter Details");
                newAccountDialog.setHeaderText(null);
                newAccountDialog.setContentText(null);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 20, 20, 20));

                if (isCustomer) {
                    TextField firstNameTextField = new TextField();
                    firstNameTextField.setPromptText("First Name");
                    TextField lastNameTextField = new TextField();
                    lastNameTextField.setPromptText("Last name");
                    TextField emailTextField = new TextField();
                    emailTextField.setPromptText("Email");
                    TextField usernameTextField = new TextField();
                    usernameTextField.setPromptText("Username");
                    PasswordField passwordField = new PasswordField();
                    passwordField.setPromptText("Password");
                    TextField addressTextField = new TextField();
                    addressTextField.setPromptText("Address");
                    TextField cardTypeTextField = new TextField();
                    cardTypeTextField.setPromptText("Card Type");
                    TextField cardNumberTextField = new TextField();
                    cardNumberTextField.setPromptText("Card Number");
                    TextField cardExpiryDateTextField = new TextField();
                    cardExpiryDateTextField.setPromptText("Card Expiry Date");
                    TextField phoneNumberTextField = new TextField();
                    phoneNumberTextField.setPromptText("Phone Number");

                    grid.add(new Label("First Name"), 0, 0);
                    grid.add(firstNameTextField, 1, 0);
                    grid.add(new Label("Last Name"), 0, 1);
                    grid.add(lastNameTextField, 1, 1);
                    grid.add(new Label("Email"), 0, 2);
                    grid.add(emailTextField, 1, 2);
                    grid.add(new Label("User"), 0, 3);
                    grid.add(usernameTextField, 1, 3);
                    grid.add(new Label("Password"), 0, 4);
                    grid.add(passwordField, 1, 4);
                    grid.add(new Label("Address"), 0, 5);
                    grid.add(addressTextField, 1, 5);
                    grid.add(new Label("Cardtype"), 0, 6);
                    grid.add(cardTypeTextField, 1, 6);
                    grid.add(new Label("Card Number"), 0, 7);
                    grid.add(cardNumberTextField, 1, 7);
                    grid.add(new Label("Card Expiry Date"), 0, 8);
                    grid.add(cardExpiryDateTextField, 1, 8);
                    grid.add(new Label("Phone Number"), 0, 9);
                    grid.add(phoneNumberTextField, 1, 9);

                    newAccountDialog.getDialogPane().setContent(grid);

                    Optional<ButtonType> newAccountResult = newAccountDialog.showAndWait();
                    if (newAccountResult.get() == buttonTypeOK) {
                        customer.setFirstName(firstNameTextField.getText());
                        customer.setLastName(lastNameTextField.getText());
                        customer.setEmail(emailTextField.getText());
                        while (true) {
                            user.setUsername(usernameTextField.getText());
                            sql.closeConnection();
                            sql.openConnection();
                            sql.getTable("SELECT * FROM User WHERE Username = " + new String(user.getUsername()));
                            if (sql.next()) {
                                // alert on ui that username is taken
                                continue;
                            }
                            break;
                        }
                        user.setPassword(passwordField.getText());
                        billing.setAddress(addressTextField.getText());
                        billing.setCardType(cardTypeTextField.getText());
                        billing.setCardNumber(Long.parseLong(cardNumberTextField.getText()));
                        billing.setCardExpiryDate(cardExpiryDateTextField.getText());
                        billing.setPhoneNumber(Long.parseLong(phoneNumberTextField.getText()));

                        sql.closeConnection();
                        sql.openConnection();
                        sql.insertData(billing);
                        sql.closeConnection();
                        sql.openConnection();
                        sql.getTable("SELECT * FROM BillingInfo WHERE CardNumber = "
                                + billing.getCardNumber() + " AND Address = '" + new String(billing.getAddress()) + "'");
                        if (sql.next()) {
                            billing = sql.getBillingInfo();
                            customer.setBillingInfoID(billing.getID());
                            sql.closeResultSet();
                        }
                        sql.closeConnection();
                        sql.openConnection();
                        sql.insertData(customer);
                        sql.closeConnection();
                        sql.openConnection();
                        sql.getTable("SELECT * FROM Customers WHERE BillingInfoId = "
                                + billing.getID());
                        if (sql.next()) {
                            customer = sql.getCustomer();
                            user.setCustomerID(customer.getID());
                            sql.closeResultSet();
                        }
                        sql.closeConnection();
                        sql.openConnection();
                        sql.insertData(user);
                        sql.closeConnection();
                        sql.openConnection();
                    }
                } else if (isSeller) {

                    TextField companyNameTextField = new TextField();
                    companyNameTextField.setPromptText("Company Name");
                    TextField usernameTextField = new TextField();
                    usernameTextField.setPromptText("Username");
                    PasswordField passwordField = new PasswordField();
                    passwordField.setPromptText("Password");
                    TextField addressTextField = new TextField();
                    addressTextField.setPromptText("Address");
                    TextField cardTypeTextField = new TextField();
                    cardTypeTextField.setPromptText("Cardtype");
                    TextField cardNumberTextField = new TextField();
                    cardNumberTextField.setPromptText("Card Number");
                    TextField cardExpiryDateTextField = new TextField();
                    cardExpiryDateTextField.setPromptText("Card Expiry Date");
                    TextField phoneNumberTextField = new TextField();
                    phoneNumberTextField.setPromptText("Phone Number");

                    grid.add(new Label("Company Name"), 0, 0);
                    grid.add(companyNameTextField, 1, 0);
                    grid.add(new Label("Username"), 0, 1);
                    grid.add(usernameTextField, 1, 1);
                    grid.add(new Label("Password:"), 0, 2);
                    grid.add(passwordField, 1, 2);
                    grid.add(new Label("Address"), 0, 3);
                    grid.add(addressTextField, 1, 3);
                    grid.add(new Label("Cardtype"), 0, 4);
                    grid.add(cardTypeTextField, 1, 4);
                    grid.add(new Label("Card Number"), 0, 5);
                    grid.add(cardNumberTextField, 1, 5);
                    grid.add(new Label("Card Expiry Date"), 0, 6);
                    grid.add(cardExpiryDateTextField, 1, 6);
                    grid.add(new Label("Phone Number"), 0, 7);
                    grid.add(phoneNumberTextField, 1, 7);

                    newAccountDialog.getDialogPane().setContent(grid);

                    Optional<ButtonType> newAccountResult = newAccountDialog.showAndWait();
                    if (newAccountResult.get() == buttonTypeOK) {
                        seller.setCompanyName(companyNameTextField.getText());
                        while (true) {
                            user.setUsername(usernameTextField.getText());
                            sql.closeConnection();
                            sql.openConnection();
                            sql.getTable("SELECT * FROM User WHERE Username = " + new String(user.getUsername()));
                            if (sql.next()) {
                                // username already exists - alert on ui to try a different one
                                continue;
                            }
                            sql.closeConnection();
                            sql.openConnection();
                            break;
                        }
                        user.setPassword(passwordField.getText());
                        billing.setAddress(addressTextField.getText());
                        billing.setCardType(cardTypeTextField.getText());
                        billing.setCardNumber(Long.parseLong(cardNumberTextField.getText()));
                        billing.setCardExpiryDate(cardExpiryDateTextField.getText());
                        billing.setPhoneNumber(Long.parseLong(phoneNumberTextField.getText()));

                        sql.insertData(billing);
                        sql.closeConnection();
                        sql.openConnection();
                        sql.getTable("SELECT * FROM BillingInfo WHERE CardNumber = "
                                + billing.getCardNumber() + " AND Address = '" + new String(billing.getAddress()) + "'");
                        if (sql.next()) {
                            billing = sql.getBillingInfo();
                            seller.setBillingInfoID(billing.getID());
                            sql.closeResultSet();
                        }
                        sql.closeConnection();
                        sql.openConnection();
                        sql.insertData(seller);
                        sql.closeConnection();
                        sql.openConnection();
                        sql.getTable("SELECT * FROM Seller WHERE BillingInfoId = "
                                + seller.getBillingInfoID());
                        if (sql.next()) {
                            seller = sql.getSeller();
                            user.setSellerID(seller.getID());
                            sql.closeResultSet();
                        }
                        sql.closeConnection();
                        sql.openConnection();
                        sql.insertData(user);
                        sql.closeConnection();
                        sql.openConnection();
                    }
                }
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
