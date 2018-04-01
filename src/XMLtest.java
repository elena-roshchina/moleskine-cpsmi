import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.FileOutputStream;
import java.io.IOException;


public class XMLtest extends Application{
    private Node root_contacts;
    private NodeList contacts;
    private Document doc;
    private int n_to_edit = 999;
    private int n_to_delete = 999;

    private static String stringPad(String str, int n, int align){
        // align =  0 center
        //       = -1 spaces to left
        //       = +1 spaces to right
        if (str == null) str="";
        int length = str.length();
        String buff = "";
        switch (align){
            case 0:
                int k = (n-length)/2;
                for (int i = 0; i < k; i++) buff = buff.concat(" ");
                str = buff.concat(str);
                for (int i = length + k; i < n; i++) str = str.concat(" ");
                break;
            case 1: for (int i = length; i < n; i++) str = str.concat(" "); break;
            case -1:
                for (int i = 0; i < n - length; i++) buff = buff.concat(" ");
                str = buff.concat(str);
                break;
        } //end of switch
        return str;
    } // end of stringPad

    private static void recursiveNodePrint(Node node, int level, int count){
        String s = "";
        for (int k = 0; k < level; k++) s = s.concat(" ");

        if (node.getNodeType() == 1) System.out.println(s + count + ", Node type: " + node.getNodeType() +
                    ", Node name: " + node.getNodeName());
        if (node.getNodeType() == 3) System.out.println(s + count + ", Node type: " + node.getNodeType() +
                    " Node value: " + node.getNodeValue() );

        NodeList nextnodes = node.getChildNodes();
        for (int i = 0; i < nextnodes.getLength(); i++) recursiveNodePrint(nextnodes.item(i), level+1, i);
    } // end of recursiveNodePrint

    private static String showStructure(Node node, int level, int count){
        String structure ="";
        String s = "";
        String nextlevelstr = "";
        for (int k = 0; k < level; k++) s = s.concat(" ");

        if (node.getNodeType() == 1)
            structure = s + Integer.toString(count) + ", Node type: " + node.getNodeType() +
                ", Node name: " + node.getNodeName() + "\n";
        if (node.getNodeType() == 3) structure = s + Integer.toString(count) + ", Node type: " + node.getNodeType() +
                " Node value: " + node.getNodeValue() + "\n";

        NodeList nextnodes = node.getChildNodes();
        for (int i = 0; i < nextnodes.getLength(); i++) nextlevelstr = showStructure(nextnodes.item(i), level+1, i);
        structure = structure.concat(nextlevelstr);
        return structure;
    } // showStructure

    private static void addNewPerson(Document document,
                                     String docFileName,
                                     String input_txt_name,
                                     String input_txt_birthday,
                                     String input_txt_address,
                                     String input_txt_phone) throws TransformerFactoryConfigurationError, DOMException {
        // Create root
        Node root = document.getDocumentElement();
        // Create new element
        Element person = document.createElement("Person");
        // Create element of Person - Name, BirthDay, Address, Phone
        Element name = document.createElement("Name");
        Element birthday = document.createElement("BirthDay");
        Element address = document.createElement("Address");
        Element phone = document.createElement("Phone");
        // set text content to person
        name.setTextContent(input_txt_name);
        birthday.setTextContent(input_txt_birthday);
        address.setTextContent(input_txt_address);
        phone.setTextContent(input_txt_phone);
        // add elements to person
        person.appendChild(name);
        person.appendChild(birthday);
        person.appendChild(address);
        person.appendChild(phone);
        // add person to root
        root.appendChild(person);
        
        saveDoc(document, docFileName);
    }// end of addNewPerson

    private static void saveDoc(Document document, String docFileName) throws TransformerFactoryConfigurationError {
        try {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(document);
            FileOutputStream fileoutput = new FileOutputStream(docFileName);
            StreamResult result = new StreamResult(fileoutput);
            tr.transform(source, result);
            fileoutput.close();
        } catch (TransformerException | IOException e) {
            e.printStackTrace(System.out);
        }
    } // end of saveDoc

    private static String show_all_contacts(NodeList contact_list){
        String txt = "";
        Node contact_person;
        NodeList contact_person_details;
        Node contact_person_details_item;

        for (int i = 0; i < contact_list.getLength(); i++) {
            // look at contacts list selected item
            contact_person = contact_list.item(i);
            txt = txt.concat(stringPad(Integer.toString(i+1), 8,0));
            if (contact_person.getNodeType() != Node.TEXT_NODE) {
                contact_person_details = contact_person.getChildNodes();
                for (int j = 0; j < contact_person_details.getLength(); j++) {
                    contact_person_details_item = contact_person_details.item(j);
                    if (contact_person_details_item.getNodeType() != Node.TEXT_NODE) {
                        txt = txt.concat(contact_person_details_item.getChildNodes().item(0).getTextContent() + "   ");

                    }
                }
                txt = txt.concat("\n ---------------------------------------------------------------------------------- \n");
            }
        } // end of for i

        return txt;
    } // end of show all


    public static void main(String[] args){launch(args);}//end of main

    @Override
    public void start(Stage mainstage) throws Exception {
        mainstage.setTitle("Contacts");
        BorderPane root = new BorderPane();
        GridPane topmenu = new GridPane();
        GridPane bottommenu = new GridPane();

        Button button_show = new Button("Show all");
        Button button_add = new Button("Add new");
        Button button_edit = new Button("Edit");
        Button button_delete = new Button("Delete");
        //Button button_structure = new Button("Structure");
        Button button_close = new Button("Close");

        TextField txt_item_for_edit = new TextField("");
        TextField txt_item_for_delete = new TextField("");
        txt_item_for_edit.setMaxWidth(40);
        txt_item_for_delete.setMaxWidth(40);
        Label label_item_to_edit = new Label("№ to edit: ");
        Label label_item_to_delete = new Label("№ to delete: ");
        Label warning = new Label("msg");

        topmenu.setAlignment(Pos.TOP_LEFT);
        topmenu.setPadding(new Insets(5,5,5,5));
        topmenu.setVgap(8);
        topmenu.setHgap(8);

        topmenu.add(button_add, 0,0);
        topmenu.add(button_show, 1,0);
        topmenu.add(warning,2,0);



        bottommenu.setPadding(new Insets(5,5,5,5));
        bottommenu.setVgap(8);
        bottommenu.setHgap(8);
        //bottommenu.add(button_structure,0,0);
        bottommenu.add(label_item_to_edit, 1,0);
        bottommenu.add(txt_item_for_edit, 2,0);
        bottommenu.add(button_edit, 3,0);
        bottommenu.add(label_item_to_delete,4,0);
        bottommenu.add(txt_item_for_delete, 5,0);
        bottommenu.add(button_delete, 6,0);
        bottommenu.add(button_close,7,0);



        int scWidth = 500;
        int scHeight = 600;
        int txtWidth = scWidth-15;
        int txtHeight = scHeight-140;

        // Text area
        TextArea text = new TextArea();
        text.setWrapText(false);
        text.setText("");
        text.setMinSize(txtWidth,txtHeight);

        /* ************************** section DOC ************************ */

        Node person;
        NodeList person_details;
        Node person_details_item;
        Node person_to_remove;



        try {
            DocumentBuilder doc_builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = doc_builder.parse("moleskine.xml");
            root_contacts = doc.getDocumentElement();

            contacts = root_contacts.getChildNodes();

            System.out.println("The root element is " + root_contacts.getNodeName()
                    + " number of person = " + contacts.getLength());



            /* ******************** SHOW ALL*** *************************** */

            //String sss = show_all_contacts(contacts);

            /* ******************** NEW CONTACT *************************** */



            /* ******************** SHOW CONTACT NUMBER K ****************** */

            //contacts.item(k).getTextContent();

            /* ******************** SHOW STRUCTURE ************************* */

            //recursiveNodePrint(root,0, 0);

            /* ******************** DELETE ********************************* */

            //root_contacts.removeChild(contacts.item(k));


            /* ******************** EDIT ********************************* */

            /*
            */

            /* ******************** SAVE ********************************* */

            //saveDoc(doc, "moleskine.xml");


/*


            System.out.println("The root element is " + root_contacts.getNodeName()
                    + " number of person = " + contacts.getLength());

*/


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }



        /* *********************** end of section DOC ********************* */
        button_delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                boolean success = false;
                try {
                    n_to_delete = Integer.parseInt(txt_item_for_delete.getText())-1;
                    System.out.println(n_to_delete);
                    if (n_to_delete < contacts.getLength()) success = true;

                }
                catch(NumberFormatException e){
                    warning.setText("Enter CORRECT number of record to delete");

                }
                if (success) {

                    root_contacts.removeChild(contacts.item(n_to_delete));
                    saveDoc(doc,"moleskine.xml");

                }

            }
        }); // end of DELETE

        button_edit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // New window (Stage)
                Stage new_stage = new Stage();
                Label label_newroot = new Label("Adding new record");
                TextField txt_name = new TextField();
                TextField txt_birthday = new TextField();
                TextField txt_address = new TextField();
                TextField txt_phone = new TextField();



                Label label_name = new Label("Name*");
                Label label_birthday = new Label("Birthday");
                Label label_address = new Label("Address");
                Label label_phone = new Label("Phone");
                try {
                    n_to_edit = Integer.parseInt(txt_item_for_edit.getText())-1;

                }
                catch(NumberFormatException e){
                    label_newroot.setText("Enter number of record");
                }

                // load record item to text fields
                if (n_to_edit<=contacts.getLength()) {
                    NodeList fields = contacts.item(n_to_edit).getChildNodes();
                    txt_name.setText(fields.item(0).getTextContent());
                    txt_birthday.setText(fields.item(1).getTextContent());
                    txt_address.setText(fields.item(2).getTextContent());
                    txt_phone.setText(fields.item(3).getTextContent());
                }

                Button button_clear = new Button("Clear");
                button_clear.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        txt_name.setText("");
                        txt_birthday.setText("");
                        txt_address.setText("");
                        txt_phone.setText("");
                        label_newroot.setText("Editing of " + n_to_edit + " record");
                    }
                }); // end of clear

                Button button_submit = new Button("Submit");
                button_submit.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {

                        // read text fields to record

                        NodeList newfields = contacts.item(n_to_edit).getChildNodes();

                        newfields.item(0).setTextContent(txt_name.getText());
                        newfields.item(1).setTextContent(txt_birthday.getText());
                        newfields.item(2).setTextContent(txt_address.getText());
                        newfields.item(3).setTextContent(txt_phone.getText());

                        saveDoc(doc, "moleskine.xml");
                        label_newroot.setText("Contact " + (n_to_edit+1) + " edited");

                    } // end of handle
                }); // end of SUBMIT

                Button button_close_newwin = new Button("Close");
                button_close_newwin.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        new_stage.close();
                    }
                }); // end of CLOSE NEW WINDOW

                // layout of new window
                GridPane new_root = new GridPane();

                new_root.setMinSize(400, 200);
                new_root.setPadding(new Insets(10, 10, 10, 10) );
                //Setting the vertical and horizontal gaps between the columns
                new_root.setVgap(5);
                new_root.setHgap(5);
                //Setting the Grid alignment
                new_root.setAlignment(Pos.CENTER);

                new_root.add(label_name,0,0);
                new_root.add(txt_name,1,0);

                new_root.add(label_birthday,0,1);
                new_root.add(txt_birthday,1,1);

                new_root.add(label_address,0,2);
                new_root.add(txt_address,1,2);

                new_root.add(label_phone,0,3);
                new_root.add(txt_phone,1,3);


                new_root.add(button_clear,0,8);
                new_root.add(button_submit,1,8);
                new_root.add(button_close_newwin,2,8);

                new_root.add(label_newroot,1,9);

                Scene new_scene = new Scene(new_root);
                new_stage.setTitle("Add new contact");
                new_stage.setScene(new_scene);

                // Set position of second window, related to primary window.
                new_stage.setX(mainstage.getX() + 60);
                new_stage.setY(mainstage.getY() + 60);
                new_stage.show();

            }// end of handle
        }); //end of EDIT


         button_add.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                        // New window (Stage)
                        Stage new_stage = new Stage();
                        Label label_newroot = new Label("Adding new record");

                        TextField txt_name = new TextField("");
                        TextField txt_birthday = new TextField("");
                        TextField txt_address = new TextField("");
                        TextField txt_phone = new TextField("");

                        Label label_name = new Label("Name*");
                        Label label_birthday = new Label("Birthday");
                        Label label_address = new Label("Address");
                        Label label_phone = new Label("Phone");

                        Button button_clear = new Button("Clear");
                        button_clear.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                txt_name.setText(" ");
                                txt_birthday.setText(" ");
                                txt_address.setText(" ");
                                txt_phone.setText(" ");
                            }
                        }); // end of clear

                        Button button_submit = new Button("Submit");
                        button_submit.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                addNewPerson(doc, "moleskine.xml",
                                        txt_name.getText(),
                                        txt_birthday.getText(),
                                        txt_address.getText(),
                                        txt_phone.getText());
                                label_newroot.setText("New contact added");
                            } // end of handle
                        }); // end of SUBMIT

                        Button button_close_newwin = new Button("Close");
                        button_close_newwin.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                new_stage.close();
                            }
                        }); // end of CLOSE NEW WINDOW

                        // layout of new window
                        GridPane new_root = new GridPane();

                        new_root.setMinSize(400, 200);
                        new_root.setPadding(new Insets(10, 10, 10, 10) );
                        //Setting the vertical and horizontal gaps between the columns
                        new_root.setVgap(5);
                        new_root.setHgap(5);
                        //Setting the Grid alignment
                        new_root.setAlignment(Pos.CENTER);

                        new_root.add(label_name,0,0);
                        new_root.add(txt_name,1,0);

                        new_root.add(label_birthday,0,1);
                        new_root.add(txt_birthday,1,1);

                        new_root.add(label_address,0,2);
                        new_root.add(txt_address,1,2);

                        new_root.add(label_phone,0,3);
                        new_root.add(txt_phone,1,3);

                        new_root.add(button_clear,0,8);
                        new_root.add(button_submit,1,8);
                        new_root.add(button_close_newwin,2,8);

                        new_root.add(label_newroot,1,9);

                        Scene new_scene = new Scene(new_root);
                        new_stage.setTitle("Add new contact");
                        new_stage.setScene(new_scene);

                        // Set position of second window, related to primary window.
                        new_stage.setX(mainstage.getX() + 60);
                        new_stage.setY(mainstage.getY() + 60);
                        new_stage.show();

            }// end of handle
        }); // end of ADD NEW


        button_show.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
               contacts = root_contacts.getChildNodes();

               text.setText(show_all_contacts(contacts));

            }
        }); //end of show contacts

        /* button_structure.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                text.setText(showStructure(root_contacts,0,0));
            }
        }); //end of show structure */

        button_close.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                Platform.exit();
            }
        }); //end of main window close button







        root.setTop(topmenu);
        root.setCenter(text);
        root.setBottom(bottommenu);



        //set scene
        Scene myScene = new Scene(root,scWidth, scHeight);
        mainstage.setScene(myScene);
        //show
        mainstage.show();



    }// end of start

}// end of XML test